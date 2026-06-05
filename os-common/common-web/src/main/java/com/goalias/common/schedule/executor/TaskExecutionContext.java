package com.goalias.common.schedule.executor;

import com.goalias.common.schedule.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务执行上下文
 * <p>
 * 任务调度器在执行策略前构造，传递给具体 {@link TaskExecutorStrategy}。
 *
 * @author Goalias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务主键
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private TaskType taskType;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * cron 表达式
     */
    private String cronExpression;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 自定义参数（用于业务侧扩展）
     */
    @Default
    private Map<String, Object> params = new HashMap<>();

    /**
     * 触发来源：SCHEDULE-调度触发，MANUAL-手动触发
     */
    private String source;
}
