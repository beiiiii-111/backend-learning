package top.yixz.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.mybatis.entity.Product;
import top.yixz.mybatis.mapper.ProductMapper;

/**
 * 商品接口（课后挑战）：只读，用来对照下单前后的库存变化
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description ProductController
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductMapper productMapper;

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productMapper.selectById(id);
    }
}
