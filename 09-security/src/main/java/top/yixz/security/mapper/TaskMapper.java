package top.yixz.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yixz.security.entity.Task;
import top.yixz.security.model.TaskRow;

import java.util.List;

/**
 * 任务 Mapper
 *
 * @author yixz
 * @date 2026-10-29
 * @description 详情/列表走联查投影（带提交人登录名和姓名），更新走 BaseMapper。
 * 排序统一按提交时间倒序，最新交的排最前。
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    String SELECT_WITH_OWNER = """
            SELECT t.id, t.title, t.content, t.owner_id, t.status, t.score,
                   t.comment, t.submitted_at, t.reviewed_at,
                   u.username AS owner, u.nickname AS owner_nickname
            FROM sec_task t
            JOIN sec_user u ON u.id = t.owner_id
            """;

    @Select(SELECT_WITH_OWNER + """
            WHERE t.owner_id = #{ownerId}
            ORDER BY t.submitted_at DESC""")
    List<TaskRow> selectByOwner(@Param("ownerId") Long ownerId);

    @Select(SELECT_WITH_OWNER + """
            ORDER BY t.submitted_at DESC""")
    List<TaskRow> selectAll();

    @Select(SELECT_WITH_OWNER + """
            WHERE t.id = #{id}""")
    TaskRow selectDetail(@Param("id") Long id);
}
