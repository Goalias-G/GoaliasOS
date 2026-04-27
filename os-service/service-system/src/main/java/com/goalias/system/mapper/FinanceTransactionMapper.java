package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceDayTrendVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.FinanceTransactionVo;
import com.goalias.system.domain.vo.FinanceTrendVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinanceTransactionMapper extends BaseMapper<FinanceTransaction> {

    FinanceOverviewVo selectOverview(@Param("userId") Long userId);

    List<FinanceTrendVo> selectTrend(@Param("userId") Long userId, @Param("year") Integer year);

    List<FinanceCategoryPieVo> selectCategoryPie(@Param("userId") Long userId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    List<FinanceDayTrendVo> selectDayTrend(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);

    Page<FinanceTransactionVo> selectTransactionPage(IPage<FinanceTransactionVo> page,
                                                     @Param("userId") Long userId,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("tag") Integer tag,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);
}
