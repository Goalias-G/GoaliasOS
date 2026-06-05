package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务执行日志对象 sys_scheduled_task_log
 *
 * @author Goalias
 */
@Data
@TableName("sys_scheduled_task_log")
public class SysScheduledTaskLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联任务ID
     */
    private Long taskId;

    /**
     * 任务名称（冗余）
     */
    private String taskName;

    /**
     * 任务类型（冗余）
     */
    private String taskType;

    /**
     * 执行开始时间
     */
    private Date startTime;

    /**
     * 执行结束时间
     */
    private Date endTime;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 执行状态：0-失败 1-成功
     */
    private String status;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 执行结果摘要
     */
    private String resultMessage;

    /**
     * 触发来源：SCHEDULE-调度 MANUAL-手动
     */
    private String source;

    /**
     * 入库时间
     */
    private Date createTime;
}
