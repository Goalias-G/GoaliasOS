package com.goalias.common.schedule.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 抽象任务执行器基类
 * <p>
 * 模板方法：子类实现 {@link #doExecute(TaskExecutionContext)}；
 * 基类负责记录开始/结束时间、耗时、异常捕获与结果封装。
 *
 * @author Goalias
 */
@Slf4j
public abstract class AbstractTaskExecutor implements TaskExecutorStrategy {

    @Override
    public final TaskExecuteResult execute(TaskExecutionContext context) {
        Date start = new Date();
        TaskExecuteResult.TaskExecuteResultBuilder builder = TaskExecuteResult.builder()
            .startTime(start);
        try {
            log.info("[任务执行] 任务开始 taskId={} type={} name={} source={}",
                context.getTaskId(), context.getTaskType(), context.getTaskName(), context.getSource());
            doExecute(context);
            builder.success(true).message("执行成功");
            log.info("[任务执行] 任务成功 taskId={}", context.getTaskId());
        } catch (Throwable t) {
            builder.success(false)
                .errorMessage(t.getClass().getSimpleName() + ": " + t.getMessage());
            log.error("[任务执行] 任务失败 taskId={} type={}", context.getTaskId(), context.getTaskType(), t);
        } finally {
            Date end = new Date();
            builder.endTime(end).durationMs(end.getTime() - start.getTime());
        }
        return builder.build();
    }

    /**
     * 子类实现具体业务逻辑
     */
    protected abstract void doExecute(TaskExecutionContext context) throws Exception;
}
