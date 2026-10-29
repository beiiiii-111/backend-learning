package top.yixz.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.yixz.zhihu.entity.Special;
import top.yixz.zhihu.mapper.SpecialMapper;
import top.yixz.zhihu.service.SpecialService;

import java.util.List;

/**
 * SpecialServiceImpl
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description SpecialServiceImpl
 */
@Service
public class SpecialServiceImpl implements SpecialService {

    @Resource
    private SpecialMapper specialMapper;

    @Override
    public List<Special> getAll() {
        LambdaQueryWrapper<Special> wrapper = new LambdaQueryWrapper<>();
        // 根据更新时间降序排列
        wrapper.orderByDesc(Special::getUpdated);
        return specialMapper.selectList(wrapper);
    }

    @Override
    public List<Special> getByCategory(String category) {
        return specialMapper.selectByCategory(category);
    }

    @Override
    public List<Special> getByPage(int limit, int offset) {
        return specialMapper.selectByPage(limit, offset);
    }

    @Override
    public List<String> getCategories() {
        LambdaQueryWrapper<Special> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Special::getCategory).isNotNull(Special::getCategory).groupBy(Special::getCategory);
        return specialMapper.selectList(wrapper).stream().map(Special::getCategory).toList();
    }
}
