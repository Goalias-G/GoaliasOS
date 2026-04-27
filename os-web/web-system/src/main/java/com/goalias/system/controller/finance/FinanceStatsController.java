package com.goalias.system.controller.finance;

import com.goalias.common.core.domain.R;
import com.goalias.common.web.core.BaseController;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceDayTrendVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.FinanceTrendVo;
import com.goalias.system.service.IFinanceStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/stats")
public class FinanceStatsController extends BaseController {

    private final IFinanceStatsService financeStatsService;

    @GetMapping("/overview")
    public R<FinanceOverviewVo> overview() {
        return R.ok(financeStatsService.getOverview());
    }

    @GetMapping("/monthTrend")
    public R<List<FinanceTrendVo>> monthTrend(@RequestParam(required = false) Integer year) {
        return R.ok(financeStatsService.getMonthTrend(year));
    }

    @GetMapping("/dayTrend")
    public R<List<FinanceDayTrendVo>> dayTrend(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {
        return R.ok(financeStatsService.getDayTrend(year, month));
    }

    @GetMapping("/category-pie")
    public R<List<FinanceCategoryPieVo>> categoryPie(@RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return R.ok(financeStatsService.getCategoryPie(startDate, endDate));
    }
}
