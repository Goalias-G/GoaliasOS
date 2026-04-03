package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 睡眠图表VO
 *
 * @author Goalias
 */
@Data
public class SleepChartVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 起床时间
     */
    private LocalTime upTime;

    /**
     * 睡眠时间
     */
    private LocalTime sleepTime;

}