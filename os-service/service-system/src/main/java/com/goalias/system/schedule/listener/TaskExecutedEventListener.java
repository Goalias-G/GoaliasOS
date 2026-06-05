package com.goalias.system.schedule.listener;

import com.goalias.common.schedule.event.TaskExecutedEvent;
import com.goalias.system.domain.SysScheduledTask;
import com.goalias.system.domain.SysScheduledTaskLog;
import com.goalias.system.service.ISysScheduledTaskLogService;
import com.goalias.system.service.ISysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 任务执行事件监听
 * <p>
 * 调度器执行完一次任务后发布 {@link TaskExecutedEvent}，本监听器负责：
 * 1. 写入 {@code sys_scheduled_task_log} 执行日志
 * 2. 更新任务最近执行时间/状态/计数
 *
 * @author Goalias
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecutedEventListener {

    private final ISysScheduledTaskLogService taskLogService;
    private final ISysScheduledTaskService taskService;

    @Async
    @EventListener
    public void onTaskExecuted(TaskExecutedEvent event) {
        try {
            TaskExecutedEvent source = event;
            // 写入日志
            SysScheduledTaskLog log = new SysScheduledTaskLog();
            log.setTaskId(source.getTask().getId());
            log.setTaskName(source.getTask().getTaskName());
            log.setTaskType(source.getTask().getTaskType() == null ? null : source.getTask().getTaskType().getCode());
            log.setStartTime(source.getResult().getStartTime());
            log.setEndTime(source.getResult().getEndTime());
            log.setDurationMs(source.getResult().getDurationMs());
            log.setStatus(source.getResult().isSuccess() ? "1" : "0");
            log.setResultMessage(source.getResult().getMessage());
            log.setErrorMessage(source.getResult().getErrorMessage());
            log.setSource(source.getSource());
            log.setCreateTime(new Date());
            taskLogService.insertLog(log);

            // 更新任务最近执行状态
            Date executeTime = source.getResult().getStartTime() == null ? new Date() : source.getResult().getStartTime();
            taskService.recordExecution(source.getTask().getId(), source.getResult().isSuccess(), executeTime);
        } catch (Exception e) {
            log.error("[任务调度] 处理任务执行事件失败 taskId={}", event.getTask().getId(), e);
        }
    }
}
