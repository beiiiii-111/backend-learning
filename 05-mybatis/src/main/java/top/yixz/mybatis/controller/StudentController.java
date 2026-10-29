package top.yixz.mybatis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.mybatis.entity.Student;
import top.yixz.mybatis.service.StudentService;
import top.yixz.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;

/**
 * 学生成绩接口（案例）
 *
 * @author yixz
 * @description StudentController
 */
@Tag(name = "学生成绩", description = "学生 / 成绩单 / 分组统计，覆盖 MP 单表与多表查询")
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "全部学生", description = "自动过滤已逻辑删除的记录，6 条数据只返回 5 条")
    @GetMapping
    public List<Student> list() {
        return studentService.listAll();
    }

    @Operation(summary = "按 id 查一个学生", description = "查不到返回 null，不抛异常")
    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @Operation(summary = "按班级查")
    @GetMapping("/class")
    public List<Student> listByClass(@Parameter(description = "班级名，如 软件2411")
                                     @RequestParam String className) {
        return studentService.listByClass(className);
    }

    @Operation(summary = "按姓名模糊查", description = "不传 name 时等于查全部")
    @GetMapping("/search")
    public List<Student> search(@Parameter(description = "姓名关键字") @RequestParam(required = false) String name) {
        return studentService.searchByName(name);
    }

    @Operation(summary = "按年龄区间查", description = "minAge / maxAge 都可以不传")
    @GetMapping("/age-range")
    public List<Student> listByAgeRange(@RequestParam(required = false) Integer minAge,
                                        @RequestParam(required = false) Integer maxAge) {
        return studentService.listByAgeRange(minAge, maxAge);
    }

    @Operation(summary = "分页查学生")
    @GetMapping("/page")
    public Page<Student> page(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "2") long size) {
        return studentService.pageStudents(current, size);
    }

    @Operation(summary = "只要姓名这一列")
    @GetMapping("/names")
    public List<String> names() {
        return studentService.listNames();
    }

    @Operation(summary = "每个学生的选课门数、总分、平均分")
    @GetMapping("/statistics")
    public List<Map<String, Object>> statistics() {
        return studentService.scoreStat();
    }

    @Operation(summary = "某学生的成绩单", description = "mode = steps 走多次单表查询在 Java 里拼，mode = join 走一条 SQL，两者返回一致")
    @GetMapping("/{id}/scores")
    public List<ScoreSheetVO> scores(@PathVariable Long id,
                                     @RequestParam(defaultValue = "steps") String mode) {
        return "join".equals(mode)
                ? studentService.scoreSheetByJoin(id)
                : studentService.scoreSheetBySteps(id);
    }

    /* ==================== 动手练习 ==================== */

    @Operation(summary = "练习 1：每门课的平均分", description = "带课程名和选课人数，按平均分降序")
    @GetMapping("/courses/statistics")
    public List<Map<String, Object>> courseStatistics() {
        return studentService.courseStat();
    }

    @Operation(summary = "练习 2：成绩单分页")
    @GetMapping("/{id}/scores/page")
    public IPage<ScoreSheetVO> scorePage(@PathVariable Long id,
                                         @RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "2") long size) {
        return studentService.scoreSheetPage(id, current, size);
    }

    @Operation(summary = "练习 3：每个班的平均分", description = "已逻辑删除的赵雪不参与统计")
    @GetMapping("/classes/statistics")
    public List<Map<String, Object>> classStatistics() {
        return studentService.classStat();
    }
}
