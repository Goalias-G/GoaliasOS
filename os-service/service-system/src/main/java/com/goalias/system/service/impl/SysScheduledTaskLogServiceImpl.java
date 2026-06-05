package com.goalias.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTaskLog;
import com.goalias.system.domain.bo.SysScheduledTaskLogBo;
import com.goalias.system.domain.vo.SysScheduledTaskLogVo;
import com.goalias.system.mapper.SysScheduledTaskLogMapper;
import com.goalias.system.service.ISysScheduledTaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class SysScheduledTaskLogServiceImpl implements ISysScheduledTaskLogService {

    private final SysScheduledTaskLogMapper baseMapper;

    @Override
    public TableDataInfo<SysScheduledTaskLogVo> queryPageList(SysScheduledTaskLogBo bo, PageQuery pageQuery) {
        Page<SysScheduledTaskLogVo> page = baseMapper.selectLogPage(
            pageQuery.build(),
            bo.getTaskId(),
            bo.getTaskName(),
            bo.getTaskType(),
            bo.getStatus(),
            bo.getSource(),
            bo.getBeginTime(),
            bo.getEndTime()
        );
        return TableDataInfo.build(page);
    }

    @Override
    public SysScheduledTaskLog queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void insertLog(SysScheduledTaskLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(new java.util.Date());
        }
        baseMapper.insert(log);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void cleanLog() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
