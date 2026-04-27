package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FinanceOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long totalIncome;

    private Long totalExpense;

    private Long balance;

    private Long monthIncome;

    private Long monthExpense;

    private Long monthBalance;
}
