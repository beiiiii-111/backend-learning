package com.group6.campus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    /** student / organizer / admin */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 50)
    private String nickname;

    /** 学号（学生角色填写） */
    @Column(name = "student_no", length = 20)
    private String studentNo;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;
}
