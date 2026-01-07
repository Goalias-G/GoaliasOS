package com.goalias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 *
 * @author Goalias
 */
@SpringBootApplication(scanBasePackages = {"com.goalias"})
@EnableScheduling
public class GoaliasOSApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(GoaliasOSApplication.class);
        //Actuator /actuator/startup 获取应用启动耗时信息(dev)
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
    }
}