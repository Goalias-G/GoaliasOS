package com.goalias.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 每日健康记录业务对象 daily_health
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DailyHealthBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private Long userId;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime upTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime sleepTime;

    private String food;

    private String exercise;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate healthDate;

    private Date startTime;

    private Date endTime;
}
