package top.yixz.zhihu.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果封装
 *
 * @author yxzhang
 * @date 2026/10/19
 * @description ResponseResult
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseResult {
    private Integer code;
    private String msg;
    private Object data;
}
