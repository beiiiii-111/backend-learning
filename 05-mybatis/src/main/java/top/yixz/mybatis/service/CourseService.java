package top.yixz.mybatis.service;

import com.baomidou.mybatisplus.spring.service.IService;
import top.yixz.mybatis.entity.Course;

import java.util.List;

/**
 * 课程 Service（课后进阶 1）：重点练 saveBatch 与 removeByIds
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description CourseService
 */
public interface CourseService extends IService<Course> {

    /** 批量新增，底层走 IService.saveBatch，默认每 1000 条刷一次 */
    boolean createCourseBatch(List<Course> courses);

    /** 批量删除（物理删除：t_course 没配逻辑删除字段） */
    boolean removeCourseBatch(List<Long> ids);
}
