package top.yixz.zhihu.service;

import top.yixz.zhihu.entity.Special;

import java.util.List;

/**
 * SpecialService
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description SpecialService
 */
public interface SpecialService {

    /**
     * 获取所有数据
     *
     * @return List<Special>
     */
    List<Special> getAll();

    /**
     * 按分类获取数据
     *
     * @param category 分类
     * @return List<Special>
     */
    List<Special> getByCategory(String category);

    /**
     * 分页获取数据
     *
     * @param limit  每页条数
     * @param offset 偏移量
     * @return List<Special>
     */
    List<Special> getByPage(int limit, int offset);

    /**
     * 获取所有分类
     *
     * @return List<String>
     */
    List<String> getCategories();
}
