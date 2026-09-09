package top.yixz.config.quickstart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description user
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private Long id;
    private String name;
    private LocalDate birthday;
}
