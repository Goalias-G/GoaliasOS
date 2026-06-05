package com.goalias.system.controller.schedule;

import com.goalias.common.core.domain.R;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTaskLog;
import com.goalias.system.domain.bo.SysScheduledTaskLogBo;
import com.goalias.system.domain.vo.SysScheduledTaskLogVo;
import com.goalias.system.service.ISysScheduledTaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 任务执行日志 Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/schedule/logs")
public class SysScheduledTaskLogController extends BaseController {

    private final ISysScheduledTaskLogService taskLogService;

    /**
     * 分页查询
     */
    @GetMapping("/list")
    public TableDataInfo<SysScheduledTaskLogVo> list(SysScheduledTaskLogBo bo, PageQuery pageQuery) {
        return taskLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<SysScheduledTaskLog> getInfo(@PathVariable Long id) {
        return R.ok(taskLogService.queryById(id));
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(taskLogService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    /**
     * 一键清空
     */
    @DeleteMapping("/clean")
    public R<Void> clean() {
        taskLogService.cleanLog();
        return R.ok();
    }
}
