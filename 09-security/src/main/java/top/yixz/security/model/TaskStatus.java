package top.yixz.security.model;

/**
 * 任务状态
 *
 * @author yixz
 * @date 2026-10-29
 * @description sec_task.status 只有两个值。statusText 是给前端直接显示的中文，
 * 前端不再自己映射一遍。
 */
public enum TaskStatus {

    SUBMITTED("待批改"),
    REVIEWED("已批改");

    private final String text;

    TaskStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static TaskStatus from(String value) {
        for (TaskStatus status : values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的任务状态：" + value);
    }
}
