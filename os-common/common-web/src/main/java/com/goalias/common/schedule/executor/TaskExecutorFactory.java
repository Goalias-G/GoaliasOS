package com.goalias.common.schedule.executor;

import com.goalias.common.schedule.enums.TaskType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 任务执行器策略工厂
 * <p>
 * 启动时收集所有 {@link TaskExecutorStrategy} Bean，按 TaskType 索引；调度器通过本类路由。
 *
 * @author Goalias
 */
@Slf4j
@Component
public class TaskExecutorFactory {

    private final List<TaskExecutorStrategy> strategies;
    private final Map<TaskType, TaskExecutorStrategy> registry = new EnumMap<>(TaskType.class);

    public TaskExecutorFactory(List<TaskExecutorStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        for (TaskExecutorStrategy strategy : strategies) {
            TaskType type = strategy.supports();
            if (type == null) {
                log.warn("[任务调度] 策略 {} 返回空的 supports()，已忽略", strategy.getClass().getName());
                continue;
            }
            TaskExecutorStrategy previous = registry.put(type, strategy);
            if (previous != null) {
                log.warn("[任务调度] 任务类型 {} 存在多个实现: {} 与 {}，后者覆盖前者",
                    type, previous.getClass().getName(), strategy.getClass().getName());
            } else {
                log.info("[任务调度] 注册任务执行策略 type={} impl={}", type, strategy.getClass().getSimpleName());
            }
        }
    }

    /**
     * 根据任务类型获取执行器
     */
    public TaskExecutorStrategy get(TaskType type) {
        TaskExecutorStrategy strategy = registry.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到任务类型对应的执行器: " + type);
        }
        return strategy;
    }

    /**
     * 当前已注册的任务类型
     */
    public Map<TaskType, TaskExecutorStrategy> registered() {
        return Collections.unmodifiableMap(registry);
    }
}
