package com.goalias.system.domain.bo;

import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 动态任务 业务对象
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysScheduledTaskBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属用户ID（为空时取当前登录用户）
     */
    private Long userId;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称长度不能超过64")
    private String taskName;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @NotBlank(message = "cron表达式不能为空")
    @Size(max = 64, message = "cron表达式长度不能超过64")
    private String cronExpression;

    @Size(max = 255, message = "任务描述长度不能超过255")
    private String description;

    @Pattern(regexp = "^[01]$", message = "任务状态仅支持 0 或 1")
    private String status;

    /**
     * 任务自定义参数（JSON字符串），由各 TaskType 约定 Key。
     * FINANCE：categoryId(Long,必填)、amount(Long,默认0)、tag(Integer,默认2)、remark(String,可选)
     */
    @Size(max = 2000, message = "任务参数长度不能超过2000")
    private String taskParams;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
