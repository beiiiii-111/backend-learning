package com.group6.campus.controller;

import com.group6.campus.common.*;
import com.group6.campus.entity.Activity;
import com.group6.campus.repository.ActivityRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 活动模块：发布 / 编辑 / 下架 / 列表 / 详情 / 审核（对应 API 文档第 3 章） */
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> CATEGORIES = Set.of("lecture", "sports", "volunteer", "contest");

    private final ActivityRepository activityRepository;

    public record ActivityReq(String title, String coverUrl, String startTime, String endTime,
                              String location, String category, Integer capacity,
                              String signupDeadline, String description) {}
    public record AuditReq(String action, String reason) {}

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody ActivityReq req, HttpServletRequest request) {
        requireRole(request, "organizer");
        Activity a = validateAndBuild(req);
        a.setOrganizerId(currentUserId(request));
        a.setStatus("pending");   // 创建后进入待审核，审核通过才在学生端展示
        activityRepository.save(a);
        return Result.ok(Map.of("activityId", a.getId(), "status", a.getStatus()));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody ActivityReq req,
                                              HttpServletRequest request) {
        Activity a = requireOwnerOrAdmin(id, request);
        Activity updated = validateAndBuild(req);
        updated.setId(a.getId());
        updated.setOrganizerId(a.getOrganizerId());
        updated.setStatus(a.getStatus());
        updated.setTaken(a.getTaken());
        activityRepository.save(updated);
        return Result.ok(Map.of("activityId", a.getId(), "status", a.getStatus()));
    }

    @PutMapping("/{id}/offline")
    public Result<Map<String, Object>> offline(@PathVariable Long id, HttpServletRequest request) {
        Activity a = requireOwnerOrAdmin(id, request);
        a.setStatus("offline");
        activityRepository.save(a);
        return Result.ok(Map.of("activityId", a.getId(), "status", a.getStatus()));
    }

    /** 分页列表：分类/关键词/仅可报名筛选，支持 status=pending（仅管理员，用于审核中心） */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean onlyOpen,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        if ("pending".equals(status)) {
            requireRole(request, "admin");
        }
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "startTime"));

        Specification<Activity> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            if (status != null && !status.isBlank()) {
                ps.add(cb.equal(root.get("status"), status));
            } else {
                ps.add(cb.equal(root.get("status"), "approved"));   // 学生端只见已过审活动
            }
            if (category != null && !category.isBlank()) {
                ps.add(cb.equal(root.get("category"), category));
            }
            if (keyword != null && !keyword.isBlank()) {
                ps.add(cb.like(root.get("title"), "%" + keyword + "%"));
            }
            if (Boolean.TRUE.equals(onlyOpen)) {
                ps.add(cb.equal(root.get("status"), "approved"));
                ps.add(cb.greaterThan(root.get("signupDeadline"), LocalDateTime.now()));
                ps.add(cb.lessThan(root.get("taken"), root.get("capacity")));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Activity> p = activityRepository.findAll(spec, pageable);
        List<Map<String, Object>> list = p.getContent().stream().map(this::toCard).toList();
        return Result.ok(Map.of("list", list, "total", p.getTotalElements(),
                "page", page, "size", size));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        Activity a = activityRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        boolean manager = isOwnerOrAdmin(a, request);
        if (!a.getStatus().equals("approved") && !manager) {
            throw BizException.of(ErrorCode.NOT_FOUND, "活动不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>(toCard(a));
        data.put("description", a.getDescription());
        data.put("signupDeadline", a.getSignupDeadline().format(FMT));
        data.put("endTime", a.getEndTime().format(FMT));
        data.put("status", a.getStatus());
        return Result.ok(data);
    }

    @PutMapping("/{id}/audit")
    @Transactional
    public Result<Map<String, Object>> audit(@PathVariable Long id, @RequestBody AuditReq req,
                                             HttpServletRequest request) {
        requireRole(request, "admin");
        Activity a = activityRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        if (!a.getStatus().equals("pending")) {
            throw BizException.of(ErrorCode.CONFLICT, "该活动不在待审核状态");
        }
        if ("approve".equals(req.action())) {
            a.setStatus("approved");
        } else if ("reject".equals(req.action())) {
            if (req.reason() == null || req.reason().isBlank()) {
                throw BizException.of(ErrorCode.PARAM_INVALID, "驳回必须填写原因");
            }
            a.setStatus("rejected");
            a.setRejectReason(req.reason());
        } else {
            throw BizException.of(ErrorCode.PARAM_INVALID, "action 仅支持 approve / reject");
        }
        activityRepository.save(a);
        return Result.ok(Map.of("activityId", a.getId(), "status", a.getStatus()));
    }

    // ---------- 私有方法 ----------

    private Activity validateAndBuild(ActivityReq req) {
        if (req.title() == null || req.title().isBlank() || req.title().length() > 50) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "标题须为 1~50 字");
        }
        if (!CATEGORIES.contains(req.category())) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "分类不合法");
        }
        if (req.capacity() == null || req.capacity() < 1) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "人数上限须 ≥ 1");
        }
        try {
            LocalDateTime start = LocalDateTime.parse(req.startTime(), FMT);
            LocalDateTime end = LocalDateTime.parse(req.endTime(), FMT);
            LocalDateTime deadline = LocalDateTime.parse(req.signupDeadline(), FMT);
            if (!end.isAfter(start)) {
                throw BizException.of(ErrorCode.PARAM_INVALID, "结束时间必须晚于开始时间");
            }
            if (!deadline.isBefore(start)) {
                throw BizException.of(ErrorCode.PARAM_INVALID, "报名截止时间不能晚于活动开始时间");
            }
            Activity a = new Activity();
            a.setTitle(req.title());
            a.setCoverUrl(req.coverUrl() == null ? "" : req.coverUrl());
            a.setStartTime(start);
            a.setEndTime(end);
            a.setLocation(req.location() == null ? "" : req.location());
            a.setCategory(req.category());
            a.setCapacity(req.capacity());
            a.setSignupDeadline(deadline);
            a.setDescription(req.description() == null ? "" : req.description());
            return a;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "时间格式须为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private Map<String, Object> toCard(Activity a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("activityId", a.getId());
        m.put("title", a.getTitle());
        m.put("coverUrl", a.getCoverUrl());
        m.put("category", a.getCategory());
        m.put("startTime", a.getStartTime().format(FMT));
        m.put("location", a.getLocation());
        m.put("capacity", a.getCapacity());
        m.put("signedCount", a.getTaken());
        m.put("remaining", Math.max(a.getCapacity() - a.getTaken(), 0));
        m.put("status", a.getStatus());
        return m;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String currentRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    private void requireRole(HttpServletRequest request, String role) {
        if (!role.equals(currentRole(request))) {
            throw BizException.of(ErrorCode.FORBIDDEN, "无权限执行该操作");
        }
    }

    private boolean isOwnerOrAdmin(Activity a, HttpServletRequest request) {
        return "admin".equals(currentRole(request)) || a.getOrganizerId().equals(currentUserId(request));
    }

    private Activity requireOwnerOrAdmin(Long id, HttpServletRequest request) {
        Activity a = activityRepository.findByIdAndIsDeleted(id, 0)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "活动不存在"));
        if (!isOwnerOrAdmin(a, request)) {
            throw BizException.of(ErrorCode.FORBIDDEN, "仅活动创建者或管理员可操作");
        }
        return a;
    }
}
