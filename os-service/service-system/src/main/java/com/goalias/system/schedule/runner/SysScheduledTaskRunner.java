package com.goalias.system.schedule.runner;

import com.goalias.system.service.ISysScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 动态任务启动加载 Runner
 * <p>
 * 应用启动时从数据库加载所有运行中的任务，注入到 {@code ThreadPoolTaskScheduler}。
 *
 * @author Goalias
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class SysScheduledTaskRunner implements ApplicationRunner {

    private final ISysScheduledTaskService sysScheduledTaskService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[任务调度] 开始加载运行中的定时任务...");
        try {
            sysScheduledTaskService.loadAllRunningTasks();
        } catch (Exception e) {
            log.error("[任务调度] 启动加载任务异常", e);
        }
    }
}
