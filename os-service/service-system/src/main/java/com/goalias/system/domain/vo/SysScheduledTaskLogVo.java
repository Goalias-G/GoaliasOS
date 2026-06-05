package com.goalias.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 任务执行日志 视图对象
 *
 * @author Goalias
 */
@Data
public class SysScheduledTaskLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long taskId;

    private String taskName;

    private String taskType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long durationMs;

    /**
     * 执行状态：0-失败 1-成功
     */
    private String status;

    private String errorMessage;

    private String resultMessage;

    /**
     * 触发来源：SCHEDULE-调度 MANUAL-手动
     */
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
