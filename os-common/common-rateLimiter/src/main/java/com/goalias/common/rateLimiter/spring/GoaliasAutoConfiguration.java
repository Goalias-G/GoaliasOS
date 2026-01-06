package com.goalias.common.rateLimiter.spring;


import com.goalias.common.rateLimiter.config.GoaliasProperty;
import com.goalias.common.rateLimiter.strategy.FallBackStrategy;
import com.goalias.common.rateLimiter.strategy.MethodHotStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GoaliasProperty.class)
public class GoaliasAutoConfiguration {

    @Bean
    public GoaliasScanner goaliasScanner()
    {
        return new GoaliasScanner();
    }
    @Bean
    public GoaliasProperty goaliasProperty(){
        return new GoaliasProperty();
    }
    @Bean
    public FallBackStrategy fallBackStrategy(){
        return new FallBackStrategy();
    }
    @Bean
    public MethodHotStrategy methodHotStrategy(){
        return new MethodHotStrategy();
    }
}
