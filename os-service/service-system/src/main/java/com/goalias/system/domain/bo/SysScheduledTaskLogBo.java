package com.goalias.system.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 任务执行日志查询条件
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysScheduledTaskLogBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private String taskName;

    private String taskType;

    /**
     * 执行状态：0-失败 1-成功
     */
    private String status;

    /**
     * 触发来源：SCHEDULE-调度 MANUAL-手动
     */
    private String source;

    /**
     * 开始时间起 yyyy-MM-dd HH:mm:ss
     */
    private String beginTime;

    /**
     * 开始时间止 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}
