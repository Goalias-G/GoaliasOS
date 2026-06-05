package com.goalias.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.schedule.enums.TaskType;
import com.goalias.common.schedule.event.TaskSnapshot;
import com.goalias.common.schedule.executor.DynamicTaskRegistrar;
import com.goalias.common.schedule.executor.TaskExecuteResult;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysScheduledTask;
import com.goalias.system.domain.bo.SysScheduledTaskBo;
import com.goalias.system.domain.vo.SysScheduledTaskVo;
import com.goalias.system.mapper.SysScheduledTaskMapper;
import com.goalias.system.service.ISysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysScheduledTaskServiceImpl implements ISysScheduledTaskService {

    private final SysScheduledTaskMapper baseMapper;
    private final DynamicTaskRegistrar dynamicTaskRegistrar;

    @Override
    public TableDataInfo<SysScheduledTaskVo> queryPageList(SysScheduledTaskBo bo, PageQuery pageQuery) {
        Page<SysScheduledTaskVo> page = baseMapper.selectTaskPage(
            pageQuery.build(),
            bo.getUserId(),
            bo.getTaskName(),
            bo.getTaskType(),
            bo.getStatus()
        );
        return TableDataInfo.build(page);
    }

    @Override
    public SysScheduledTask queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertByBo(SysScheduledTaskBo bo) {
        validateParams(bo.getTaskType(), bo.getTaskParams());
        SysScheduledTask entity = MapstructUtils.convert(bo, SysScheduledTask.class);
        if (entity.getUserId() == null) {
            entity.setUserId(LoginHelper.getUserId());
        }
        if (ObjectUtil.isEmpty(entity.getStatus())) {
            entity.setStatus("0");
        }
        baseMapper.insert(entity);
        if ("1".equals(entity.getStatus())) {
            try {
                dynamicTaskRegistrar.addTask(toSnapshot(entity));
            } catch (Exception e) {
                log.error("[任务调度] 注册任务失败 id={}", entity.getId(), e);
                throw new ServiceException("注册调度任务失败: " + e.getMessage());
            }
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SysScheduledTaskBo bo) {
        validateParams(bo.getTaskType(), bo.getTaskParams());
        SysScheduledTask exist = baseMapper.selectById(bo.getId());
        if (exist == null) {
            throw new ServiceException("任务不存在");
        }
        SysScheduledTask entity = MapstructUtils.convert(bo, SysScheduledTask.class);
        boolean ok = baseMapper.updateById(entity) > 0;
        if (!ok) {
            return false;
        }
        try {
            boolean typeChanged = !ObjectUtil.equal(exist.getTaskType(), entity.getTaskType());
            boolean cronChanged = !ObjectUtil.equal(exist.getCronExpression(), entity.getCronExpression());
            boolean statusChanged = !ObjectUtil.equal(exist.getStatus(), entity.getStatus());
            boolean paramsChanged = !ObjectUtil.equal(exist.getTaskParams(), entity.getTaskParams());
            SysScheduledTask refreshed = baseMapper.selectById(bo.getId());
            if ("1".equals(refreshed.getStatus())) {
                if (!"1".equals(exist.getStatus()) || typeChanged || cronChanged || paramsChanged) {
                    dynamicTaskRegistrar.addTask(toSnapshot(refreshed));
                }
            } else {
                dynamicTaskRegistrar.removeTask(refreshed.getId());
            }
        } catch (Exception e) {
            log.error("[任务调度] 更新调度任务失败 id={}", bo.getId(), e);
            throw new ServiceException("更新调度任务失败: " + e.getMessage());
        }
        return true;
    }

    @Override
    public Boolean changeStatus(Long id, String status) {
        SysScheduledTask task = baseMapper.selectById(id);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("状态值非法");
        }
        task.setStatus(status);
        boolean ok = baseMapper.updateById(task) > 0;
        if (!ok) {
            return false;
        }
        if ("1".equals(status)) {
            dynamicTaskRegistrar.addTask(toSnapshot(task));
        } else {
            dynamicTaskRegistrar.removeTask(id);
        }
        return true;
    }

    @Override
    public TaskExecuteResult runOnce(Long id) {
        SysScheduledTask task = baseMapper.selectById(id);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return dynamicTaskRegistrar.runOnce(toSnapshot(task));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            dynamicTaskRegistrar.removeTask(id);
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void loadAllRunningTasks() {
        List<SysScheduledTask> tasks = baseMapper.selectRunningTasks();
        if (tasks == null || tasks.isEmpty()) {
            log.info("[任务调度] 启动加载：未发现运行中的任务");
            return;
        }
        int success = 0, failed = 0;
        for (SysScheduledTask task : tasks) {
            try {
                dynamicTaskRegistrar.addTask(toSnapshot(task));
                success++;
            } catch (Exception e) {
                failed++;
                log.error("[任务调度] 启动加载任务失败 id={} cron={}", task.getId(), task.getCronExpression(), e);
            }
        }
        log.info("[任务调度] 启动加载完成，共 {} 条，成功 {} 条，失败 {} 条", tasks.size(), success, failed);
    }

    @Override
    public void recordExecution(Long taskId, boolean success, Date executeTime) {
        try {
            SysScheduledTask exist = baseMapper.selectById(taskId);
            if (exist == null) {
                return;
            }
            Date next = null;
            try {
                next = dynamicTaskRegistrar.nextExecutionTime(exist.getCronExpression());
            } catch (Exception ignore) {
                // cron 非法时跳过下次时间
            }
            LambdaUpdateWrapper<SysScheduledTask> wrapper = new LambdaUpdateWrapper<SysScheduledTask>()
                .eq(SysScheduledTask::getId, taskId)
                .set(SysScheduledTask::getLastExecuteTime, executeTime)
                .set(SysScheduledTask::getLastExecuteStatus, success ? "1" : "0")
                .set(SysScheduledTask::getExecuteCount, ObjectUtil.defaultIfNull(exist.getExecuteCount(), 0L) + 1)
                .set(SysScheduledTask::getNextExecuteTime, next)
                .set(SysScheduledTask::getUpdateTime, new Date());
            baseMapper.update(null, wrapper);
        } catch (Exception e) {
            log.error("[任务调度] 更新任务最近执行状态失败 taskId={}", taskId, e);
        }
    }

    @Override
    public List<SysScheduledTask> listRunningTasks() {
        return baseMapper.selectRunningTasks();
    }

    /**
     * 校验各任务类型约定的 params 必填字段
     */
    private void validateParams(String taskType, String paramsJson) {
        TaskType type = TaskType.of(taskType);
        if (type == null) {
            return;
        }
        Map<String, Object> params = parseParams(paramsJson);
        switch (type) {
            case FINANCE -> {
                Object categoryId = params.get("categoryId");
                if (categoryId == null) {
                    throw new ServiceException("FINANCE 任务必须传入 params.categoryId");
                }
                if (!(categoryId instanceof Number)) {
                    throw new ServiceException("params.categoryId 必须为数字");
                }
            }
            case EMAIL_CHAT -> {
                Object prompt = params.get("prompt");
                if (prompt == null || prompt.toString().isBlank()) {
                    throw new ServiceException("EMAIL_CHAT 任务必须传入 params.prompt");
                }
                Object recipient = params.get("recipient");
                if (recipient == null || recipient.toString().isBlank()) {
                    throw new ServiceException("EMAIL_CHAT 任务必须传入 params.recipient");
                }
            }
            default -> {
                // 其它任务类型暂不校验
            }
        }
    }

    private TaskSnapshot toSnapshot(SysScheduledTask task) {
        return TaskSnapshot.builder()
            .id(task.getId())
            .userId(task.getUserId())
            .taskName(task.getTaskName())
            .taskType(TaskType.of(task.getTaskType()))
            .cronExpression(task.getCronExpression())
            .description(task.getDescription())
            .params(parseParams(task.getTaskParams()))
            .build();
    }

    private List<TaskSnapshot> toSnapshots(List<SysScheduledTask> tasks) {
        return tasks.stream().map(this::toSnapshot).collect(Collectors.toList());
    }

    /**
     * 将 params 的 JSON 字符串解析为 Map；空值返回空 Map
     */
    private Map<String, Object> parseParams(String paramsJson) {
        if (StrUtil.isBlank(paramsJson)) {
            return Collections.emptyMap();
        }
        try {
            return JSONUtil.toBean(paramsJson, Map.class);
        } catch (Exception e) {
            log.warn("[任务调度] params 解析失败 paramsJson={}", paramsJson, e);
            return Collections.emptyMap();
        }
    }
}
