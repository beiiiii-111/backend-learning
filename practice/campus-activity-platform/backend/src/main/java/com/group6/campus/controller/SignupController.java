package com.group6.campus.controller;

import com.group6.campus.common.*;
import com.group6.campus.entity.Activity;
import com.group6.campus.entity.Signup;
import com.group6.campus.entity.User;
import com.group6.campus.repository.ActivityRepository;
import com.group6.campus.repository.SignupRepository;
import com.group6.campus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 报名模块：报名 / 取消 / 我的报名 / 名单查看 / 导出（对应 API 文档第 4 章） */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SignupController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SignupRepository signupRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @PostMapping("/activities/{activityId}/signups")
    @Transactional
    public Result<Map<String, Object>> signup(@PathVariable Long activityId, HttpServletRequest request) {
        requireRole(request, "student");
        Long userId = (Long) request.getAttribute("userId");

        Activity a = activityRepository.findByIdAndIsDeleted(activityId, 0)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        if (!a.getStatus().equals("approved")) {
            throw BizException.of(ErrorCode.CONFLICT, "活动当前不可报名");
        }
        if (LocalDateTime.now().isAfter(a.getSignupDeadline())) {
            throw BizException.of(ErrorCode.CONFLICT, "报名已截止");
        }
        Optional<Signup> existing = signupRepository.findByUserIdAndActivityId(userId, activityId);
        if (existing.isPresent() && existing.get().getStatus().equals("signed")) {
            throw BizException.of(ErrorCode.CONFLICT, "请勿重复报名");
        }

        // 核心：条件更新扣减名额，行锁保证原子性，返回 0 = 已满（防超卖的最后防线）
        int updated = activityRepository.increaseTaken(activityId);
        if (updated == 0) {
            throw BizException.of(ErrorCode.CONFLICT, "报名人数已满");
        }

        Signup s = existing.orElseGet(() -> {
            Signup n = new Signup();
            n.setUserId(userId);
            n.setActivityId(activityId);
            return n;
        });
        s.setStatus("signed");
        s.setSignupTime(LocalDateTime.now());
        signupRepository.save(s);

        Activity fresh = activityRepository.findById(activityId).orElse(a);
        return Result.ok(Map.of("signupId", s.getId(), "activityId", activityId,
                "signedCount", fresh.getTaken()));
    }

    @DeleteMapping("/signups/{signupId}")
    @Transactional
    public Result<Map<String, Object>> cancel(@PathVariable Long signupId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Signup s = signupRepository.findById(signupId)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "报名记录不存在"));
        if (!s.getUserId().equals(userId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "只能取消本人的报名");
        }
        if (!s.getStatus().equals("signed")) {
            throw BizException.of(ErrorCode.CONFLICT, "该报名已取消");
        }
        Activity a = activityRepository.findById(s.getActivityId())
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        if (LocalDateTime.now().isAfter(a.getSignupDeadline())) {
            throw BizException.of(ErrorCode.CONFLICT, "报名已截止，不可取消");
        }
        s.setStatus("cancelled");
        s.setUpdateTime(LocalDateTime.now());
        signupRepository.save(s);
        activityRepository.decreaseTaken(a.getId());   // 名额立即释放
        return Result.ok(Map.of("signupId", s.getId(), "status", "cancelled"));
    }

    @GetMapping("/signups/me")
    public Result<Map<String, Object>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Signup> p = signupRepository.findByUserIdAndStatusOrderByIdDesc(userId, "signed",
                PageRequest.of(Math.max(page - 1, 0), Math.min(size, 100)));

        Set<Long> ids = new HashSet<>();
        p.getContent().forEach(s -> ids.add(s.getActivityId()));
        Map<Long, Activity> acts = new HashMap<>();
        activityRepository.findAllById(ids).forEach(a -> acts.put(a.getId(), a));

        List<Map<String, Object>> list = p.getContent().stream()
                .map(s -> {
                    Activity a = acts.get(s.getActivityId());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("signupId", s.getId());
                    m.put("activityId", s.getActivityId());
                    m.put("title", a == null ? "-" : a.getTitle());
                    m.put("startTime", a == null ? "-" : a.getStartTime().format(FMT));
                    m.put("location", a == null ? "-" : a.getLocation());
                    m.put("signupTime", s.getSignupTime().format(FMT));
                    m.put("status", s.getStatus());
                    return m;
                }).toList();
        return Result.ok(Map.of("list", list, "total", p.getTotalElements(),
                "page", page, "size", size));
    }

    @GetMapping("/activities/{activityId}/signups")
    public Result<Map<String, Object>> roster(@PathVariable Long activityId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              HttpServletRequest request) {
        requireOrganizerOrAdmin(activityId, request);
        Page<Signup> p = signupRepository.findByActivityIdAndStatusOrderByIdAsc(activityId, "signed",
                PageRequest.of(Math.max(page - 1, 0), Math.min(size, 100)));

        Set<Long> uids = new HashSet<>();
        p.getContent().forEach(s -> uids.add(s.getUserId()));
        Map<Long, User> users = new HashMap<>();
        userRepository.findAllById(uids).forEach(u -> users.put(u.getId(), u));

        List<Map<String, Object>> list = p.getContent().stream()
                .map(s -> {
                    User u = users.get(s.getUserId());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", s.getUserId());
                    m.put("nickname", u == null ? "-" : u.getNickname());
                    m.put("studentNo", u == null || u.getStudentNo() == null ? "-" : u.getStudentNo());
                    m.put("signupTime", s.getSignupTime().format(FMT));
                    return m;
                }).toList();
        return Result.ok(Map.of("list", list, "total", p.getTotalElements(),
                "page", page, "size", size));
    }

    /** 报名名单导出：CSV（带 BOM，Excel 直接打开中文不乱码） */
    @GetMapping("/activities/{activityId}/signups/export")
    public ResponseEntity<byte[]> export(@PathVariable Long activityId, HttpServletRequest request) {
        requireOrganizerOrAdmin(activityId, request);
        List<Signup> all = signupRepository.findByActivityIdAndStatusOrderByIdAsc(activityId, "signed",
                PageRequest.of(0, 10_000)).getContent();

        Set<Long> uids = new HashSet<>();
        all.forEach(s -> uids.add(s.getUserId()));
        Map<Long, User> users = new HashMap<>();
        userRepository.findAllById(uids).forEach(u -> users.put(u.getId(), u));

        StringBuilder csv = new StringBuilder("\uFEFF姓名,学号,报名时间\r\n");
        for (Signup s : all) {
            User u = users.get(s.getUserId());
            csv.append(u == null ? "-" : u.getNickname()).append(',')
               .append(u == null || u.getStudentNo() == null ? "-" : u.getStudentNo()).append(',')
               .append(s.getSignupTime().format(FMT)).append("\r\n");
        }
        String filename = URLEncoder.encode("报名名单.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    // ---------- 私有方法 ----------

    private void requireRole(HttpServletRequest request, String role) {
        if (!role.equals(request.getAttribute("role"))) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权限执行该操作");
        }
    }

    private void requireOrganizerOrAdmin(Long activityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean admin = "admin".equals(request.getAttribute("role"));
        Activity a = activityRepository.findByIdAndIsDeleted(activityId, 0)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        if (!admin && !a.getOrganizerId().equals(userId)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "仅活动创建者与管理员可见");
        }
    }
}
