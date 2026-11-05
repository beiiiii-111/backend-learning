-- 初始数据：三个角色的演示账号，密码均为 11111111（SHA-256(salt+pwd)，salt=campus）
MERGE INTO users (id, username, password, role, nickname, student_no) KEY (id) VALUES
  (1, 'admin',     '2e51b6403fbd9498e02ff7e7e2280d953b3a0625117b61444aa065f7cc885faf', 'admin',     '系统管理员', NULL),
  (2, 'organizer', '2e51b6403fbd9498e02ff7e7e2280d953b3a0625117b61444aa065f7cc885faf', 'organizer', '篮球社负责人', NULL),
  (3, 'student',   '2e51b6403fbd9498e02ff7e7e2280d953b3a0625117b61444aa065f7cc885faf', 'student',   '张同学', '2024001');

MERGE INTO activity (id, title, cover_url, start_time, end_time, location, category, capacity, taken,
                     signup_deadline, description, status, organizer_id) KEY (id) VALUES
  (1001, '校园篮球联赛', 'https://picsum.photos/seed/ball/400/200',
   '2026-11-20 14:00:00', '2026-11-20 17:00:00', '体育馆', 'sports', 100, 1,
   '2026-11-18 23:59:59', '一年一度的校园篮球联赛，欢迎各学院组队观赛。', 'approved', 2),
  (1002, 'AI 时代的学习方法讲座', 'https://picsum.photos/seed/ai/400/200',
   '2026-11-15 19:00:00', '2026-11-15 21:00:00', '图书馆报告厅', 'lecture', 200, 0,
   '2026-11-14 23:59:59', '邀请计算机学院老师分享 AI 工具在大学学习中的正确用法。', 'approved', 2),
  (1003, '校园歌手大赛（待审核示例）', 'https://picsum.photos/seed/song/400/200',
   '2026-11-25 18:30:00', '2026-11-25 21:30:00', '大学生活动中心', 'sports', 300, 0,
   '2026-11-23 23:59:59', '第十二届校园歌手大赛报名开始，请在截止时间前报名。', 'pending', 2);

MERGE INTO signup (id, user_id, activity_id, status) KEY (id) VALUES
  (5001, 3, 1001, 'signed');
