package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FinanceDayTrendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日期 "2026-04-01" */
    private String day;

    private Long income;

    private Long expense;
}
