package top.yixz.mybatis.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yixz.mybatis.entity.Course;
import top.yixz.mybatis.mapper.CourseMapper;
import top.yixz.mybatis.service.CourseService;

import java.util.List;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description CourseServiceImpl
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createCourseBatch(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("批量新增至少要有一条数据");
        }
        // saveBatch 内部按批次 flush（默认 batchSize=1000），
        // 整个方法在一个事务里，任何一条失败全部回滚
        return saveBatch(courses);
    }

    @Override
    public boolean removeCourseBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ids 不能为空");
        }
        // t_course 没有 deleted 字段，这里执行的是真正的 DELETE ... WHERE id IN (...)
        return removeByIds(ids);
    }
}
