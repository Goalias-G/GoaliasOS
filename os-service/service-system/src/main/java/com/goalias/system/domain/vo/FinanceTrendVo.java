package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FinanceTrendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String month;

    private Long income;

    private Long expense;
}
