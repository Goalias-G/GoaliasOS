package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 动态调度任务对象 sys_scheduled_task
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_scheduled_task")
public class SysScheduledTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型（FINANCE-财务 LIFE-生活 CHAT-对话）
     */
    private String taskType;

    /**
     * cron 表达式
     */
    private String cronExpression;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务状态：0-暂停 1-运行
     */
    private String status;

    /**
     * 最近执行开始时间
     */
    private Date lastExecuteTime;

    /**
     * 下次执行时间
     */
    private Date nextExecuteTime;

    /**
     * 最近一次执行状态：0-失败 1-成功
     */
    private String lastExecuteStatus;

    /**
     * 累计执行次数
     */
    private Long executeCount;

    /**
     * 任务自定义参数（JSON字符串），由各 TaskType 约定 Key
     */
    @TableField("params")
    private String taskParams;

    /**
     * 逻辑删除标记（与现有库保持一致，0-存在 1-删除）
     */
    @TableLogic
    @TableField(select = false)
    private Integer delFlag;
}
