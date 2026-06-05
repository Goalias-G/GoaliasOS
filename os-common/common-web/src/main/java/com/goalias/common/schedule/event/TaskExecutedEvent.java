package com.goalias.common.schedule.event;

import com.goalias.common.schedule.executor.TaskExecuteResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 任务执行完成事件
 * <p>
 * 调度器执行完一次任务（定时或手动）后发布，业务侧监听后写入执行日志并更新任务最新执行状态。
 *
 * @author Goalias
 */
@Getter
public class TaskExecutedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 任务定义信息
     */
    private final TaskSnapshot task;

    /**
     * 执行结果
     */
    private final TaskExecuteResult result;

    /**
     * 触发来源：SCHEDULE-调度触发，MANUAL-手动触发
     */
    private final String source;

    public TaskExecutedEvent(Object source, TaskSnapshot task, TaskExecuteResult result, String triggerSource) {
        super(source);
        this.task = task;
        this.result = result;
        this.source = triggerSource;
    }
}
