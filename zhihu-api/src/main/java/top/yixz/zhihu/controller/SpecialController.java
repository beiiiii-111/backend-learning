package top.yixz.zhihu.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.zhihu.common.ResponseResult;
import top.yixz.zhihu.entity.Special;
import top.yixz.zhihu.service.SpecialService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpecialController
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description SpecialController
 */
@RestController
@RequestMapping("/api/v1/special")
public class SpecialController {

    @Resource
    private SpecialService specialService;

    @GetMapping("all")
    public ResponseResult getAll() {
        List<Special> all = specialService.getAll();
        return ResponseResult.builder()
                .code(200)
                .msg("数据获取成功")
                .data(all)
                .build();
    }

    @GetMapping("/category/{category}")
    public ResponseResult getByCategory(@PathVariable String category) {
        List<Special> specials = specialService.getByCategory(category);
        return ResponseResult.builder()
                .code(200)
                .msg("数据获取成功")
                .data(specials)
                .build();
    }

    @GetMapping("/page")
    public ResponseResult getByPage(@RequestParam(defaultValue = "3") int limit,
                                    @RequestParam(defaultValue = "0") int offset) {
        Map<String, Object> map = new HashMap<>();
        List<Special> specials = specialService.getByPage(limit, offset);
        map.put("specials", specials);
        map.put("total", specialService.getAll().size());
        return ResponseResult.builder()
                .code(200)
                .msg("数据获取成功")
                .data(map)
                .build();
    }

    @GetMapping("/categories")
    public ResponseResult getCategories() {
        return ResponseResult.builder()
                .code(200)
                .msg("数据获取成功")
                .data(specialService.getCategories())
                .build();
    }
}
