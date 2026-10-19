package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.mybatis.entity.Order;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description OrderMapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
