package com.goalias.common.schedule.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态任务调度配置
 *
 * @author Goalias
 */
@Data
@ConfigurationProperties(prefix = "schedule")
public class TaskScheduleProperties {

    /**
     * 调度线程池大小，默认 CPU * 2
     */
    private Integer poolSize = Math.max(2, Runtime.getRuntime().availableProcessors() * 2);

    /**
     * 线程名前缀
     */
    private String threadNamePrefix = "goalias-scheduler-";

    /**
     * 是否等待任务完成后关闭
     */
    private Boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * 关闭时最大等待时间（秒）
     */
    private Integer awaitTerminationSeconds = 60;

}
