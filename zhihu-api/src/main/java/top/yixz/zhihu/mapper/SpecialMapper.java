package top.yixz.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.yixz.zhihu.entity.Special;

import java.util.List;

/**
 * SpecialMapper：继承 BaseMapper 获得通用 CRUD，自定义 SQL 写在 resources/mapper/SpecialMapper.xml
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description SpecialMapper
 */
@Mapper
public interface SpecialMapper extends BaseMapper<Special> {

    /**
     * 按分类查询
     */
    List<Special> selectByCategory(@Param("category") String category);

    /**
     * 分页查询
     */
    List<Special> selectByPage(@Param("limit") int limit, @Param("offset") int offset);
}
