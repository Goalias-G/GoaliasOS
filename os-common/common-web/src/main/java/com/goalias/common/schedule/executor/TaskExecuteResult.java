package com.goalias.common.schedule.executor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务执行结果
 *
 * @author Goalias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecuteResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果摘要
     */
    private String message;

    /**
     * 异常信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 构造一个成功结果
     */
    public static TaskExecuteResult ok(String message) {
        return TaskExecuteResult.builder()
            .success(true)
            .message(message)
            .build();
    }

    /**
     * 构造一个失败结果
     */
    public static TaskExecuteResult fail(String errorMessage) {
        return TaskExecuteResult.builder()
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * 构造一个失败结果（带异常堆栈）
     */
    public static TaskExecuteResult fail(Throwable t) {
        return TaskExecuteResult.builder()
            .success(false)
            .errorMessage(t.getClass().getSimpleName() + ": " + t.getMessage())
            .build();
    }
}
