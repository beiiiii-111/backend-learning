package top.yixz.web.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果：数据列表 + 总数 + 当前页码/每页条数
 *
 * @author yxzhang
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private Integer total;
    private Integer pageNum;
    private Integer pageSize;

    public static <T> PageResult<T> of(List<T> records, Integer total, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.records = records;
        result.total = total;
        result.pageNum = pageNum;
        result.pageSize = pageSize;
        return result;
    }
}
