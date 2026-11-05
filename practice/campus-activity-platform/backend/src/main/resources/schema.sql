-- 校园活动管理平台建表脚本（对应《数据库模型设计》评审通过的 ER 图）
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(20)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,           -- SHA-256(salt+pwd)，课程演示用
    role         VARCHAR(20)  NOT NULL,           -- student / organizer / admin
    nickname     VARCHAR(50)  NOT NULL,
    student_no   VARCHAR(20),                   -- 学号（学生角色）
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS activity (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(50)  NOT NULL,
    cover_url       VARCHAR(255) NOT NULL,
    start_time      TIMESTAMP    NOT NULL,
    end_time        TIMESTAMP    NOT NULL,
    location        VARCHAR(100) NOT NULL,
    category        VARCHAR(20)  NOT NULL,        -- lecture/sports/volunteer/contest
    capacity        INT          NOT NULL,
    taken           INT          NOT NULL DEFAULT 0,
    signup_deadline TIMESTAMP    NOT NULL,
    description     VARCHAR(2000) NOT NULL,
    status          VARCHAR(20)  NOT NULL,        -- pending/approved/rejected/offline/finished
    reject_reason   VARCHAR(200),
    organizer_id    BIGINT       NOT NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      TINYINT      NOT NULL DEFAULT 0,
    -- 列表页按状态+时间倒序查询；分类筛选
    idx_status_time  VARCHAR(1),                   -- 占位注释：H2 兼容，见下方真实索引
    CONSTRAINT fk_activity_organizer FOREIGN KEY (organizer_id) REFERENCES users (id)
);
CREATE INDEX IF NOT EXISTS idx_activity_status_time ON activity (status, start_time DESC);
CREATE INDEX IF NOT EXISTS idx_activity_category    ON activity (category);

CREATE TABLE IF NOT EXISTS signup (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    activity_id BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,             -- signed / cancelled
    signup_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  TINYINT     NOT NULL DEFAULT 0,
    -- 组合唯一：同一用户同一活动仅一条有效记录（取消改状态不删行，可再次报名）
    CONSTRAINT uk_signup_user_activity UNIQUE (user_id, activity_id),
    CONSTRAINT fk_signup_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_signup_activity FOREIGN KEY (activity_id) REFERENCES activity (id)
);
CREATE INDEX IF NOT EXISTS idx_signup_activity_time ON signup (activity_id, signup_time);
