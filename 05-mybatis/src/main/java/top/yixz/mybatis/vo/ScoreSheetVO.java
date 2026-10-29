package top.yixz.mybatis.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩单返回结构（VO）
 *
 * <p>它不对应任何一张表，只是"接口返回长什么样"。
 * 三张表 join 出来的列，靠 SQL 里的 AS 别名对上这里的属性名。</p>
 *
 * @author yixz
 * @description ScoreSheetVO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSheetVO {

    private Long scoreId;

    private String studentName;

    private String courseName;

    private BigDecimal score;
}
