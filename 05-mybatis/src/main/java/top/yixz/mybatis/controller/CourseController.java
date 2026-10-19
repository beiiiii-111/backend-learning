package top.yixz.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.mybatis.entity.Course;
import top.yixz.mybatis.service.CourseService;

import java.util.List;

/**
 * 课程接口（课后进阶 1）
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description CourseController
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** 单条新增 */
    @PostMapping
    public Course create(@RequestBody Course course) {
        courseService.save(course);
        return course;
    }

    /** 批量新增：saveBatch */
    @PostMapping("/batch")
    public int createBatch(@RequestBody List<Course> courses) {
        courseService.createCourseBatch(courses);
        return courses.size();
    }

    @GetMapping("/{id}")
    public Course getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @GetMapping
    public List<Course> list() {
        return courseService.list();
    }

    /** 批量删除：removeByIds */
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestParam List<Long> ids) {
        return courseService.removeCourseBatch(ids);
    }
}
