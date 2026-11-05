package com.group6.campus.controller;

import com.group6.campus.common.*;
import com.group6.campus.entity.User;
import com.group6.campus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 用户模块：注册 / 登录 / 当前用户（对应 API 文档第 2 章） */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public record RegisterReq(String username, String password, String role, String nickname, String studentNo) {}
    public record LoginReq(String username, String password) {}

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterReq req) {
        if (req.username() == null || !req.username().matches("[A-Za-z0-9]{4,20}")) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "用户名须为 4~20 位字母或数字");
        }
        if (req.password() == null || !req.password().matches("(?=.*[A-Za-z])(?=.*\\d).{8,32}")) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "密码须为 8~32 位且同时包含字母和数字");
        }
        String role = req.role() == null ? "student" : req.role();
        if (!role.equals("student") && !role.equals("organizer")) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "角色仅支持 student / organizer");
        }
        if (userRepository.existsByUsernameAndIsDeleted(req.username(), 0)) {
            throw BizException.of(ErrorCode.CONFLICT, "用户名已存在");
        }
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(PasswordUtil.hash(req.password()));
        u.setRole(role);
        u.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username() : req.nickname());
        u.setStudentNo(req.studentNo());
        userRepository.save(u);
        return Result.ok(Map.of("userId", u.getId(), "username", u.getUsername(), "role", u.getRole()));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        User u = userRepository.findByUsernameAndIsDeleted(req.username(), 0)
                .orElseThrow(() -> BizException.of(ErrorCode.PARAM_INVALID, "用户名或密码错误"));
        if (!u.getPassword().equals(PasswordUtil.hash(req.password()))) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "用户名或密码错误");
        }
        String token = jwtUtil.issue(u.getId(), u.getRole());
        return Result.ok(Map.of("token", token, "userId", u.getId(),
                "role", u.getRole(), "expiresIn", 7200));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User u = userRepository.findById(userId)
                .orElseThrow(() -> BizException.of(ErrorCode.NOT_FOUND, "用户不存在"));
        return Result.ok(Map.of("userId", u.getId(), "username", u.getUsername(),
                "role", u.getRole(), "nickname", u.getNickname()));
    }
}
