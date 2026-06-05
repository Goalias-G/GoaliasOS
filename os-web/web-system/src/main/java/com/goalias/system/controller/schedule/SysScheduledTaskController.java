package com.goalias.system.controller.schedule;

import cn.hutool.core.util.ObjectUtil;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.schedule.executor.TaskExecuteResult;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTask;
import com.goalias.system.domain.bo.SysScheduledTaskBo;
import com.goalias.system.domain.vo.SysScheduledTaskVo;
import com.goalias.system.service.ISysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 动态调度任务 Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/schedule/tasks")
public class SysScheduledTaskController extends BaseController {

    private final ISysScheduledTaskService sysScheduledTaskService;

    /**
     * 分页查询任务列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysScheduledTaskVo> list(SysScheduledTaskBo bo, PageQuery pageQuery) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return sysScheduledTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public R<SysScheduledTask> getInfo(@PathVariable Long id) {
        return R.ok(sysScheduledTaskService.queryById(id));
    }

    /**
     * 新增任务
     */
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysScheduledTaskBo bo) {
        bo.setUserId(bo.getUserId() == null ? LoginHelper.getUserId() : bo.getUserId());
        return toAjax(sysScheduledTaskService.insertByBo(bo) != null);
    }

    /**
     * 修改任务
     */
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysScheduledTaskBo bo) {
        return toAjax(sysScheduledTaskService.updateByBo(bo));
    }

    /**
     * 启停任务
     */
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysScheduledTaskBo bo) {
        if (ObjectUtil.isNull(bo.getId()) || ObjectUtil.isEmpty(bo.getStatus())) {
            throw new ServiceException("任务ID与状态不能为空");
        }
        return toAjax(sysScheduledTaskService.changeStatus(bo.getId(), bo.getStatus()));
    }

    /**
     * 立即执行一次
     */
    @PutMapping("/run/{id}")
    public R<TaskExecuteResult> runOnce(@PathVariable Long id) {
        return R.ok(sysScheduledTaskService.runOnce(id));
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(sysScheduledTaskService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
