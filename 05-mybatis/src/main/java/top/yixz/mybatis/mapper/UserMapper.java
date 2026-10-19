package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.mybatis.entity.User;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description UserMapper：继承 BaseMapper 后单表 CRUD 直接就有，不用写 XML
 **/
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
