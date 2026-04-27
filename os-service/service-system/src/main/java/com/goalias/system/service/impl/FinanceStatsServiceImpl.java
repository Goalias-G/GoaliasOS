package com.goalias.system.service.impl;

import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceDayTrendVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.FinanceTrendVo;
import com.goalias.system.mapper.FinanceTransactionMapper;
import com.goalias.system.service.IFinanceStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FinanceStatsServiceImpl implements IFinanceStatsService {

    private final FinanceTransactionMapper transactionMapper;

    @Override
    public FinanceOverviewVo getOverview() {
        return transactionMapper.selectOverview(LoginHelper.getUserId());
    }

    @Override
    public List<FinanceTrendVo> getMonthTrend(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return transactionMapper.selectTrend(LoginHelper.getUserId(), year);
    }

    @Override
    public List<FinanceDayTrendVo> getDayTrend(Integer year, Integer month) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        return transactionMapper.selectDayTrend(LoginHelper.getUserId(), year, month);
    }

    @Override
    public List<FinanceCategoryPieVo> getCategoryPie(String startDate, String endDate) {
        if (StringUtils.isNotBlank(endDate)){
            LocalDate end = LocalDate.parse(endDate);
            endDate = end.plusDays(1).toString();
        }
        return transactionMapper.selectCategoryPie(LoginHelper.getUserId(), startDate, endDate);
    }
}
