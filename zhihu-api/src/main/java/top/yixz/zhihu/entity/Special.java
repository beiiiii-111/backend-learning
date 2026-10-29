package top.yixz.zhihu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 知乎专题实体类，对应 t_special 表
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description Special
 */
@Data
@TableName("t_special")
public class Special {
    private Long id;
    private String title;
    private String description;
    private String banner;
    private String isFollowing;
    private Integer followersCount;
    private Integer viewCount;
    private Long updated;
    private String category;
}
