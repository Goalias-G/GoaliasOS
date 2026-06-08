package com.goalias.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 动态任务 视图对象
 *
 * @author Goalias
 */
@Data
public class SysScheduledTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String taskName;

    private String taskType;

    private String cronExpression;

    private String description;

    /**
     * 任务状态：0-暂停 1-运行
     */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecuteTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextExecuteTime;

    /**
     * 最近一次执行状态：0-失败 1-成功
     */
    private String lastExecuteStatus;

    private Long executeCount;

    /**
     * 任务自定义参数（JSON字符串）
     */
    private String params;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
