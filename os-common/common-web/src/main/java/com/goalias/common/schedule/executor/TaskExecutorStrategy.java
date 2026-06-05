package com.goalias.common.schedule.executor;

import com.goalias.common.schedule.enums.TaskType;

/**
 * 任务执行器策略接口
 * <p>
 * 采用策略模式按 TaskType 路由；新增任务类型需新增枚举值并实现该接口。
 *
 * @author Goalias
 */
public interface TaskExecutorStrategy {

    /**
     * 当前策略支持的任务类型
     */
    TaskType supports();

    /**
     * 执行任务
     *
     * @param context 任务执行上下文
     * @return 执行结果
     */
    TaskExecuteResult execute(TaskExecutionContext context);
}
