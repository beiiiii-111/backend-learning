-- 每次启动都会重建表并重写这份示例数据（schema.sql 开头有 DROP TABLE）
DELETE FROM t_user;

-- 4 个可查到的示例用户，其中 admin / zhangsan / wangwu 满足「age > 18 且 vip = true」
INSERT INTO t_user (username, password, nickname, gender, age, member_level, points, balance, score,
                    phone, email, avatar, intro, tags, birthday, work_time, vip, status,
                    last_login_time, create_time, update_time, deleted, version)
VALUES ('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '管理员', 1, 30, 'GOLD',
        100, 1000.00, 4.8, '13800000001', 'admin@example.com', '/avatar/admin.png', '平台管理员',
        '["Java","架构"]', '1995-01-15', '09:00:00', 1, 1, '2026-10-01 09:00:00', '2026-10-01 09:00:00',
        '2026-10-01 09:00:00', 0, 0),
       ('zhangsan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张三', 1, 20, 'SILVER',
        80, 200.50, 4.5, '13800000002', 'zhangsan@example.com', '/avatar/zhangsan.png', '普通用户',
        '["Java","篮球"]', '2005-06-20', '09:30:00', 1, 1, '2026-10-02 10:00:00', '2026-10-02 10:00:00',
        '2026-10-02 10:00:00', 0, 0),
       ('lisi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', 2, 17, 'BRONZE',
        20, 50.00, 4.0, '13800000003', 'lisi@example.com', '/avatar/lisi.png', '未成年用户',
        '["游戏"]', '2008-03-08', '10:00:00', 0, 1, '2026-10-03 11:00:00', '2026-10-03 11:00:00',
        '2026-10-03 11:00:00', 0, 0),
       ('wangwu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王五', 1, 25, 'GOLD',
        90, 500.75, 4.9, '13800000004', 'wangwu@example.com', '/avatar/wangwu.png', '优质用户',
        '["Java","足球"]', '2000-11-11', '08:30:00', 1, 1, '2026-10-04 12:00:00', '2026-10-04 12:00:00',
        '2026-10-04 12:00:00', 0, 0),
       -- 这条 deleted = 1，逻辑删除后查询会过滤掉它，但物理行还在
       ('zhaoliu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '赵六', 1, 22, 'SILVER',
        60, 100.00, 4.2, '13800000005', 'zhaoliu@example.com', '/avatar/zhaoliu.png', '已注销用户',
        '["音乐"]', '2003-07-07', '09:15:00', 1, 0, '2026-10-05 13:00:00', '2026-10-05 13:00:00',
        '2026-10-05 13:00:00', 1, 0);

-- ==================== 课后进阶 1：课程示例数据 ====================
DELETE FROM t_course;

INSERT INTO t_course (name, teacher, credit, create_time, update_time)
VALUES ('Java 程序设计', '张老师', 4.0, '2026-10-06 08:00:00', '2026-10-06 08:00:00'),
       ('数据结构', '李老师', 3.5, '2026-10-06 09:00:00', '2026-10-06 09:00:00'),
       ('数据库原理', '王老师', 2.5, '2026-10-06 10:00:00', '2026-10-06 10:00:00');

-- ==================== 课后挑战：商品 / 订单示例数据 ====================
DELETE FROM t_product;
DELETE FROM t_order;

-- Java 编程思想定价 99.50 库存 10：够下单成功，也方便把 quantity 传 999 演示库存不足回滚
INSERT INTO t_product (name, price, stock, create_time, update_time)
VALUES ('Java 编程思想', 99.50, 10, '2026-10-06 08:00:00', '2026-10-06 08:00:00'),
       ('重构', 79.00, 5, '2026-10-06 09:00:00', '2026-10-06 09:00:00');
