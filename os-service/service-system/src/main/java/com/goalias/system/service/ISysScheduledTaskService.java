package com.goalias.system.service;

import com.goalias.common.schedule.executor.TaskExecuteResult;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTask;
import com.goalias.system.domain.bo.SysScheduledTaskBo;
import com.goalias.system.domain.vo.SysScheduledTaskVo;

import java.util.Collection;
import java.util.List;

/**
 * 动态调度任务 Service
 *
 * @author Goalias
 */
public interface ISysScheduledTaskService {

    /**
     * 分页查询
     */
    TableDataInfo<SysScheduledTaskVo> queryPageList(SysScheduledTaskBo bo, PageQuery pageQuery);

    /**
     * 详情
     */
    SysScheduledTask queryById(Long id);

    /**
     * 新增；如 status=1 则同步注册到调度器
     */
    Long insertByBo(SysScheduledTaskBo bo);

    /**
     * 修改；如 cron/status 变更则重新调度
     */
    Boolean updateByBo(SysScheduledTaskBo bo);

    /**
     * 启停任务
     */
    Boolean changeStatus(Long id, String status);

    /**
     * 立即执行一次
     */
    TaskExecuteResult runOnce(Long id);

    /**
     * 批量删除；先从调度器移除再删除 DB
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 启动加载所有运行中任务到调度器
     */
    void loadAllRunningTasks();

    /**
     * 供事件监听器更新任务最近执行状态
     */
    void recordExecution(Long taskId, boolean success, java.util.Date executeTime);

    /**
     * 查询所有运行中的任务（启动加载使用）
     */
    List<SysScheduledTask> listRunningTasks();
}
