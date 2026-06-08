package com.goalias.common.schedule.executor;

import com.goalias.common.schedule.enums.TaskType;
import com.goalias.common.schedule.event.TaskExecutedEvent;
import com.goalias.common.schedule.event.TaskSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态任务注册器
 * <p>
 * 统一管理 cron 定时任务的注册、修改、移除、立即执行；与 {@link TaskExecutorFactory} 配合完成策略路由。
 *
 * @author Goalias
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTaskRegistrar {

    private final TaskScheduler taskScheduler;
    private final TaskExecutorFactory taskExecutorFactory;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 触发来源：调度触发
     */
    public static final String SOURCE_SCHEDULE = "SCHEDULE";

    /**
     * 触发来源：手动触发
     */
    public static final String SOURCE_MANUAL = "MANUAL";

    /**
     * 任务ID -> 调度Future，便于关闭/查询
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 注册并启动一个 cron 任务；如已存在则先关闭再以新 cron 重启。
     */
    public void addTask(TaskSnapshot task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("任务定义或任务ID不能为空");
        }
        validateCron(task.getCronExpression());
        Long id = task.getId();
        ScheduledFuture<?> oldFuture = scheduledTasks.remove(id);
        if (Objects.nonNull(oldFuture)) {
            oldFuture.cancel(false);
        }
        Runnable runnable = wrap(task, SOURCE_SCHEDULE);
        ScheduledFuture<?> future = taskScheduler.schedule(runnable, new CronTrigger(task.getCronExpression()));
        scheduledTasks.put(id, future);
        log.info("[任务调度] 注册任务 id={} name={} type={} cron={}", id, task.getTaskName(), task.getTaskType(), task.getCronExpression());
    }

    /**
     * 移除并停止一个 cron 任务
     */
    public boolean removeTask(Long id) {
        if (id == null) {
            return false;
        }
        ScheduledFuture<?> future = scheduledTasks.remove(id);
        if (future != null) {
            future.cancel(false);
            log.info("[任务调度] 移除任务 id={}", id);
            return true;
        }
        return false;
    }

    /**
     * 立即执行一次任务，返回结果对象
     */
    public TaskExecuteResult runOnce(TaskSnapshot task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("任务定义或任务ID不能为空");
        }
        TaskExecutionContext context = buildContext(task, SOURCE_MANUAL);
        TaskExecutorStrategy strategy = taskExecutorFactory.get(task.getTaskType());
        TaskExecuteResult result = strategy.execute(context);
        eventPublisher.publishEvent(new TaskExecutedEvent(this, task, result, SOURCE_MANUAL));
        return result;
    }

    /**
     * 查询当前正在调度的任务ID集合
     */
    public Set<Long> runningTaskIds() {
        return Collections.unmodifiableSet(scheduledTasks.keySet());
    }

    /**
     * 关闭所有正在运行的任务
     */
    public void shutdownAll() {
        for (Map.Entry<Long, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            entry.getValue().cancel(false);
        }
        scheduledTasks.clear();
    }

    /**
     * 计算给定 cron 表达式的下一次触发时间
     */
    public Date nextExecutionTime(String cronExpression) {
        validateCron(cronExpression);
        LocalDateTime next = CronExpression.parse(cronExpression).next(LocalDateTime.now());
        return next == null ? null : Date.from(next.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    private Runnable wrap(TaskSnapshot task, String source) {
        return () -> {
            TaskExecutionContext context = buildContext(task, source);
            TaskExecuteResult result;
            try {
                TaskExecutorStrategy strategy = taskExecutorFactory.get(task.getTaskType());
                result = strategy.execute(context);
            } catch (Throwable t) {
                log.error("[任务调度] 任务执行异常 id={} type={}", task.getId(), task.getTaskType(), t);
                result = TaskExecuteResult.builder()
                    .success(false)
                    .errorMessage(t.getClass().getSimpleName() + ": " + t.getMessage())
                    .build();
            }
            eventPublisher.publishEvent(new TaskExecutedEvent(this, task, result, source));
        };
    }

    private TaskExecutionContext buildContext(TaskSnapshot task, String source) {
        return TaskExecutionContext.builder()
            .taskId(task.getId())
            .taskName(task.getTaskName())
            .taskType(task.getTaskType())
            .userId(task.getUserId())
            .cronExpression(task.getCronExpression())
            .description(task.getDescription())
            .params(task.getParams())
            .source(source)
            .build();
    }

    private void validateCron(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("cron表达式不能为空");
        }
        try {
            CronExpression.parse(cronExpression);
        } catch (Exception e) {
            throw new IllegalArgumentException("非法的cron表达式: " + cronExpression, e);
        }
    }

}
