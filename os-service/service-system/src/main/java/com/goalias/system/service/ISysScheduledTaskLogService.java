package com.goalias.system.service;

import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTaskLog;
import com.goalias.system.domain.bo.SysScheduledTaskLogBo;
import com.goalias.system.domain.vo.SysScheduledTaskLogVo;

import java.util.Collection;

/**
 * 任务执行日志 Service
 *
 * @author Goalias
 */
public interface ISysScheduledTaskLogService {

    /**
     * 分页查询
     */
    TableDataInfo<SysScheduledTaskLogVo> queryPageList(SysScheduledTaskLogBo bo, PageQuery pageQuery);

    /**
     * 详情
     */
    SysScheduledTaskLog queryById(Long id);

    /**
     * 新增日志
     */
    void insertLog(SysScheduledTaskLog log);

    /**
     * 批量删除
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 一键清空
     */
    void cleanLog();
}
