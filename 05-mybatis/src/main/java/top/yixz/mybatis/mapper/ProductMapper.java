package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.mybatis.entity.Product;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description ProductMapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
