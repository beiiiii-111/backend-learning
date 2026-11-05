package com.group6.campus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(name = "cover_url", nullable = false)
    private String coverUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 100)
    private String location;

    /** lecture / sports / volunteer / contest */
    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false)
    private Integer capacity;

    /** 已报名人数：只允许条件更新扣减，保证不超卖 */
    @Column(nullable = false)
    private Integer taken = 0;

    @Column(name = "signup_deadline", nullable = false)
    private LocalDateTime signupDeadline;

    @Column(nullable = false, columnDefinition = "VARCHAR(2000)")
    private String description;

    /** pending / approved / rejected / offline / finished */
    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;
}
