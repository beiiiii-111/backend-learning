package top.yixz.doc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.yixz.doc.dto.BookRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 图书管理 CRUD，用来演示接口文档注解：
 * @Tag 标注在类上 → 分组名；@Operation 标注在方法上 → 接口说明；
 * @Parameter 标注在参数上 → 路径/查询参数说明。
 */
@Tag(name = "图书管理", description = "模块 10 接口文档综合练习：图书 CRUD 示例接口")
@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {

    private final Map<Long, BookRequest> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    @Operation(summary = "新增图书", description = "校验通过后入库并返回生成的 id")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody BookRequest req) {
        long id = idGen.incrementAndGet();
        store.put(id, req);
        return Map.of("id", id, "title", req.getTitle(), "author", req.getAuthor(), "price", req.getPrice());
    }

    @Operation(summary = "查询单本", description = "按 id 查询，不存在返回 404")
    @GetMapping("/{id}")
    public Map<String, Object> get(
            @Parameter(description = "图书 id", example = "1")
            @PathVariable Long id) {
        BookRequest req = store.get(id);
        if (req == null) {
            throw new IllegalArgumentException("图书不存在: " + id);
        }
        return Map.of("id", id, "title", req.getTitle(), "author", req.getAuthor(), "price", req.getPrice());
    }

    @Operation(summary = "图书列表", description = "支持按分类过滤，category 不传返回全部")
    @GetMapping
    public Map<String, Object> list(
            @Parameter(description = "分类过滤", example = "计算机")
            @RequestParam(required = false) String category) {
        var list = store.entrySet().stream()
                .filter(e -> category == null || category.isBlank() || category.equals(e.getValue().getCategory()))
                .map(e -> Map.of("id", e.getKey(), "title", e.getValue().getTitle(),
                        "author", e.getValue().getAuthor(), "price", e.getValue().getPrice()))
                .toList();
        return Map.of("total", list.size(), "list", list);
    }

    @Operation(summary = "删除图书", description = "按 id 删除")
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @Parameter(description = "图书 id", example = "1")
            @PathVariable Long id) {
        store.remove(id);
        return Map.of("deleted", id);
    }
}
