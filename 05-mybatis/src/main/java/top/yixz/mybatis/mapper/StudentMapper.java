package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.mybatis.entity.Student;

/**
 * 学生 Mapper：接口体留空，单表 CRUD 由 BaseMapper 提供
 *
 * @author yixz
 * @description StudentMapper
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
