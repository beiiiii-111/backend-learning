package top.yixz.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import top.yixz.security.common.BusinessException;
import top.yixz.security.common.ErrorCode;
import top.yixz.security.dto.ReviewTaskRequest;
import top.yixz.security.dto.SubmitTaskRequest;
import top.yixz.security.entity.Task;
import top.yixz.security.entity.User;
import top.yixz.security.mapper.TaskMapper;
import top.yixz.security.mapper.UserMapper;
import top.yixz.security.model.TaskRow;
import top.yixz.security.model.TaskStatus;
import top.yixz.security.vo.TaskView;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务服务
 *
 * @author yixz
 * @date 2026-10-29
 * @description 归属判断全部在服务层，不在控制层——以后加"导出学生提交"之类的
 * 接口，同一条规则不用再抄一遍。当前登录人由 Controller 从令牌取出后传参进来，
 * 服务层不碰 SecurityContextHolder，单测不用伪造安全上下文。
 */
@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public TaskService(TaskMapper taskMapper, UserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    /**
     * 提交任务：归属永远写当前登录人，请求体里塞什么都改不了（第 18 条）。
     * 同一学生有同标题且仍待批改的任务时拒绝重复提交（30011）；
     * 已批改的允许重交，算补交。
     */
    public TaskView submit(String username, SubmitTaskRequest request) {
        User owner = findUser(username);
        Long duplicated = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getOwnerId, owner.getId())
                .eq(Task::getTitle, request.getTitle())
                .eq(Task::getStatus, TaskStatus.SUBMITTED.name()));
        if (duplicated != null && duplicated > 0) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_SUBMITTED);
        }
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setOwnerId(owner.getId());
        task.setStatus(TaskStatus.SUBMITTED.name());
        task.setSubmittedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return view(task.getId());
    }

    public List<TaskView> listMine(String username) {
        User owner = findUser(username);
        return taskMapper.selectByOwner(owner.getId()).stream()
                .map(TaskView::from)
                .toList();
    }

    /**
     * 任务详情：本人 或者 有批改权限的人可见，其余按越权处理（真实 403）
     */
    public TaskView getDetail(Long id, String username, boolean canReview) {
        TaskRow row = findRow(id);
        if (!row.getOwner().equals(username) && !canReview) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return TaskView.from(row);
    }

    public List<TaskView> listAll() {
        return taskMapper.selectAll().stream()
                .map(TaskView::from)
                .toList();
    }

    /**
     * 批改：先查存在（30010），再查状态（已批改 30012），最后才更新
     */
    public TaskView review(Long id, ReviewTaskRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (TaskStatus.REVIEWED.name().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_ALREADY_REVIEWED);
        }
        task.setStatus(TaskStatus.REVIEWED.name());
        task.setScore(request.getScore());
        task.setComment(request.getComment());
        task.setReviewedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return view(id);
    }

    private User findUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private TaskRow findRow(Long id) {
        TaskRow row = taskMapper.selectDetail(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        return row;
    }

    private TaskView view(Long id) {
        return TaskView.from(findRow(id));
    }
}
