package top.yixz.security.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.security.common.Result;
import top.yixz.security.dto.ReviewTaskRequest;
import top.yixz.security.dto.SubmitTaskRequest;
import top.yixz.security.security.SecurityUser;
import top.yixz.security.service.TaskService;
import top.yixz.security.vo.TaskView;

import java.util.List;

/**
 * 任务接口
 *
 * @author yixz
 * @date 2026-10-29
 * @description 接口绑权限点不绑角色：学生有 task:submit、教师有 task:review、
 * 管理员两者都不全有，各接口的边界由权限点表达。
 * /mine 只要求已认证，任何角色都能看自己的提交。
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('task:submit')")
    public Result<TaskView> submit(@AuthenticationPrincipal SecurityUser user,
                                   @Valid @RequestBody SubmitTaskRequest request) {
        return Result.ok(taskService.submit(user.getUsername(), request));
    }

    @GetMapping("/mine")
    public Result<List<TaskView>> mine(@AuthenticationPrincipal SecurityUser user) {
        return Result.ok(taskService.listMine(user.getUsername()));
    }

    @GetMapping("/{id}")
    public Result<TaskView> detail(@AuthenticationPrincipal SecurityUser user,
                                   @PathVariable Long id) {
        // 归属判断在服务层做，这里只把"能不能批改"传进去
        return Result.ok(taskService.getDetail(id, user.getUsername(),
                user.hasAuthority("task:review")));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('task:review')")
    public Result<List<TaskView>> list() {
        return Result.ok(taskService.listAll());
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('task:review')")
    public Result<TaskView> review(@PathVariable Long id,
                                   @Valid @RequestBody ReviewTaskRequest request) {
        return Result.ok(taskService.review(id, request));
    }
}
