package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 每日健康记录表 daily_health
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_health")
public class DailyHealth extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Date upTime;

    private Date sleepTime;

    private String food;

    private String exercise;

    private String remark;
}
