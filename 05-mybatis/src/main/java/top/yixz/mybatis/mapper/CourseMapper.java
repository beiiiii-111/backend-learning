package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.mybatis.entity.Course;

/**
 * 课程 Mapper（课后进阶 1）：接口体留空，CRUD 全由 BaseMapper 提供
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description CourseMapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
