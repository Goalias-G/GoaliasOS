package com.goalias.common.schedule.config;

import com.goalias.common.schedule.config.properties.TaskScheduleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 动态任务调度配置
 * <p>
 * 通过覆盖默认 TaskScheduler Bean，使 {@code @Scheduled} 与动态注册的 cron 任务复用同一线程池。
 *
 * @author Goalias
 */
@Configuration
@EnableConfigurationProperties(TaskScheduleProperties.class)
public class ScheduleConfig {

    @Bean(TaskSchedulerBeanName.GOALIAS_TASK_SCHEDULER)
    public ThreadPoolTaskScheduler goaliasTaskScheduler(TaskScheduleProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
        scheduler.setWaitForTasksToCompleteOnShutdown(properties.getWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 将自定义调度器作为 Spring 默认 TaskScheduler，
     * 兼容 {@code @Scheduled} 与动态 cron 任务
     */
    @Bean
    public TaskScheduler taskScheduler(@org.springframework.beans.factory.annotation.Qualifier(TaskSchedulerBeanName.GOALIAS_TASK_SCHEDULER) ThreadPoolTaskScheduler goaliasTaskScheduler) {
        return goaliasTaskScheduler;
    }

    /**
     * 内部类，集中维护调度器相关 Bean 名称常量
     */
    public static final class TaskSchedulerBeanName {
        public static final String GOALIAS_TASK_SCHEDULER = "goaliasTaskScheduler";
        private TaskSchedulerBeanName() {}
    }
}
