package com.goalias.system.service;

import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceDayTrendVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.FinanceTrendVo;

import java.util.List;

public interface IFinanceStatsService {

    FinanceOverviewVo getOverview();

    List<FinanceTrendVo> getMonthTrend(Integer year);

    List<FinanceDayTrendVo> getDayTrend(Integer year, Integer month);

    List<FinanceCategoryPieVo> getCategoryPie(String startDate, String endDate);
}
