package top.yixz.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.yixz.mybatis.entity.Course;
import top.yixz.mybatis.entity.Score;
import top.yixz.mybatis.entity.Student;
import top.yixz.mybatis.mapper.CourseMapper;
import top.yixz.mybatis.mapper.ScoreMapper;
import top.yixz.mybatis.mapper.StudentMapper;
import top.yixz.mybatis.service.StudentService;
import top.yixz.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生服务实现
 *
 * <p>继承 ServiceImpl 之后，save / getById / list / page 这些方法直接就能用，
 * 这里只写"通用方法写不出来的那部分"。</p>
 *
 * @author yixz
 * @description StudentServiceImpl
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    /** 成绩表操作，多表查询写在它的 Mapper 里 */
    private final ScoreMapper scoreMapper;

    /** 课程表操作，分步查询成绩单时要用它把课程名查出来 */
    private final CourseMapper courseMapper;

    /* ==================== 单表查询 ==================== */

    @Override
    public Student getStudent(Long id) {
        // getById 会自己拼 WHERE id = ? AND deleted = 0
        return getById(id);
    }

    @Override
    public List<Student> listAll() {
        // 不带条件查全部，同样自动带上 deleted = 0，所以只返回 5 条
        return list();
    }

    @Override
    public List<Student> listByClass(String className) {
        return list(Wrappers.<Student>lambdaQuery()
                .eq(Student::getClassName, className)
                .orderByAsc(Student::getId));
    }

    @Override
    public List<Student> searchByName(String name) {
        // 第一个参数是"这个条件要不要生效"：没传姓名就整段跳过，等于查全部
        return list(Wrappers.<Student>lambdaQuery()
                .like(StringUtils.hasText(name), Student::getName, name)
                .orderByAsc(Student::getId));
    }

    @Override
    public List<Student> listByAgeRange(Integer minAge, Integer maxAge) {
        // ge 大于等于，le 小于等于；两端都传 null 时两个条件都不生效，等于查全部
        return list(Wrappers.<Student>lambdaQuery()
                .ge(minAge != null, Student::getAge, minAge)
                .le(maxAge != null, Student::getAge, maxAge)
                .orderByAsc(Student::getAge)
                .orderByAsc(Student::getId));
    }

    @Override
    public Page<Student> pageStudents(long current, long size) {
        // 分页插件会翻译成 LIMIT ?, ?，另外再发一条 COUNT 查总数
        return page(new Page<>(current, size), Wrappers.<Student>lambdaQuery()
                .orderByAsc(Student::getId));
    }

    @Override
    public List<String> listNames() {
        // select 指定只查 name 一列，listObjs 把它当一串单值返回
        return listObjs(Wrappers.<Student>lambdaQuery()
                .select(Student::getName)
                .orderByAsc(Student::getId), Object::toString);
    }

    /* ==================== 分组统计 ==================== */

    @Override
    public List<Map<String, Object>> scoreStat() {
        // COUNT(*) 这类表达式没法用方法引用写，所以这里换成 QueryWrapper 用字符串列名
        //
        // 注意：这段 SQL 只查了 t_score，没管学生表，
        // 所以已经被逻辑删除的赵雪（student_id = 6）照样被算进来了 —— 教程专门留的这个坑。
        // 想排除她，请看练习 3 的 classStat()：join 学生表并写上 st.deleted = 0
        return scoreMapper.selectMaps(new QueryWrapper<Score>()
                .select("student_id",
                        "COUNT(*) AS course_count",
                        "SUM(score) AS total_score",
                        "ROUND(AVG(score), 2) AS avg_score")
                .groupBy("student_id")
                .orderByDesc("total_score"));
    }

    /* ==================== 多表查询：两种写法 ==================== */

    @Override
    public List<ScoreSheetVO> scoreSheetBySteps(Long studentId) {
        // 第一步：查学生本人，拿到姓名。被逻辑删除的学生这里返回 null，直接给空列表
        Student student = getById(studentId);
        if (student == null) {
            return List.of();
        }

        // 第二步：查这个学生的所有成绩记录
        List<Score> scores = scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                .eq(Score::getStudentId, studentId)
                .orderByAsc(Score::getId));
        if (scores.isEmpty()) {
            return List.of();
        }

        // 第三步：把用到的课程 id 收集起来，一次查回来。
        // 别在循环里一条条查，3 条成绩就是 3 次数据库往返（N+1 问题）
        Set<Long> courseIds = scores.stream()
                .map(Score::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, String> courseNameMap = courseMapper.selectByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        // 第四步：在内存里把三段数据拼成成绩单
        return scores.stream()
                .map(score -> new ScoreSheetVO(
                        score.getId(),
                        student.getName(),
                        courseNameMap.getOrDefault(score.getCourseId(), "课程不存在"),
                        score.getScore()))
                .toList();
    }

    @Override
    public List<ScoreSheetVO> scoreSheetByJoin(Long studentId) {
        // 关联条件全写在 SQL 里，一次查询拿到结果，返回值和分步查完全一致
        return scoreMapper.selectScoreSheet(studentId);
    }

    /* ==================== 动手练习 ==================== */

    @Override
    public List<Map<String, Object>> courseStat() {
        return scoreMapper.selectCourseAvg();
    }

    @Override
    public IPage<ScoreSheetVO> scoreSheetPage(Long studentId, long current, long size) {
        return scoreMapper.selectScoreSheetPage(new Page<>(current, size), studentId);
    }

    @Override
    public List<Map<String, Object>> classStat() {
        return scoreMapper.selectClassAvg();
    }
}
