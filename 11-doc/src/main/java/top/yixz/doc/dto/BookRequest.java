package top.yixz.doc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 图书入参对象。
 * @Schema 注解会展示在 Swagger UI 的 Schemas 区，说明每个字段含义与约束。
 */
@Data
@Schema(description = "图书新增/编辑入参")
public class BookRequest {

    @NotBlank(message = "书名不能为空")
    @Schema(description = "书名", example = "深入理解计算机系统", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "作者不能为空")
    @Schema(description = "作者", example = "Randal E. Bryant", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @Positive(message = "价格必须为正数")
    @Schema(description = "价格（元）", example = "139.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "分类", example = "计算机", allowableValues = {"计算机", "文学", "经管", "其他"})
    private String category;
}
