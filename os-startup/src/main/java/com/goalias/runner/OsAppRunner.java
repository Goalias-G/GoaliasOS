package com.goalias.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动Runner
 *
 * @author Goalias
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OsAppRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("GoaliasOS start successfully!");
    }

}
