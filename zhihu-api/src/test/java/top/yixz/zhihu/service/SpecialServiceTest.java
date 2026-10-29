package top.yixz.zhihu.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.yixz.zhihu.entity.Special;

import java.util.List;

@SpringBootTest
@Slf4j
public class SpecialServiceTest {

    @Resource
    private SpecialService specialService;

    @Test
    void getAll() {
        List<Special> all = specialService.getAll();
        all.forEach(special -> log.info("专题信息：{}", special));
    }

    @Test
    void getByCategory() {
        List<Special> list = specialService.getByCategory("科技");
        log.info("科技分类共 {} 条", list.size());
        list.forEach(special -> log.info("专题信息：{}", special));
    }

    @Test
    void getByPage() {
        List<Special> list = specialService.getByPage(3, 0);
        log.info("第一页共 {} 条", list.size());
        list.forEach(special -> log.info("专题信息：{}", special));
    }

    @Test
    void getCategories() {
        log.info("全部分类：{}", specialService.getCategories());
    }
}
