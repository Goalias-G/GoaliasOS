package com.goalias.system.controller.life;

import com.goalias.common.core.domain.R;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.vo.SleepChartVo;
import com.goalias.system.domain.bo.DailyHealthBo;
import com.goalias.system.service.IDailyHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日健康记录Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/life/health")
public class DailyHealthController extends BaseController {

    private final IDailyHealthService dailyHealthService;

    /**
     * 查询每日健康记录列表
     */
    @GetMapping("/list")
    public TableDataInfo<DailyHealth> list(@Validated DailyHealthBo bo, PageQuery pageQuery) {
        return dailyHealthService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取每日健康记录详细信息
     *
     * @param id 主键
     */
    @GetMapping(value = "/{id}")
    public R<DailyHealth> getInfo(@PathVariable Long id) {
        return R.ok(dailyHealthService.queryById(id));
    }

    /**
     * 新增每日健康记录
     */
    @PostMapping
    public R<Void> add(@Validated @RequestBody DailyHealthBo bo) {
        return toAjax(dailyHealthService.insertByBo(bo));
    }

    /**
     * 修改每日健康记录
     */
    @PutMapping
    public R<Void> edit(@Validated @RequestBody DailyHealthBo bo) {
        return toAjax(dailyHealthService.updateByBo(bo));
    }


    /**
     * 获取近可选时间的起床和睡眠时间数据（用于双折线图）
     *
     * @param days 天数，默认10天
     * @return 睡眠图表数据
     */
    @GetMapping("/chart")
    public R<List<SleepChartVo>> getSleepChart(@RequestParam(defaultValue = "10") Integer days) {
        List<DailyHealth> dailyHealths = dailyHealthService.queryRecentDays(days);
        List<SleepChartVo> chartData = dailyHealths.stream()
                .map(dh -> {
                    SleepChartVo vo = new SleepChartVo();
                    vo.setDate(dh.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                    vo.setUpTime(dh.getUpTime());
                    vo.setSleepTime(dh.getSleepTime());
                    return vo;
                })
                .collect(Collectors.toList());
        return R.ok(chartData);
    }


}
