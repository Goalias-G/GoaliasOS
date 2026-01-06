package com.goalias.common.rateLimiter.strategy;


import com.goalias.common.rateLimiter.enums.GoaliasStrategyEnum;

import java.lang.reflect.Method;

public interface GoaliasStrategy {

    GoaliasStrategyEnum getStrategy();

    Object process(Object bean, Method method, Object[] args) throws Exception;
}
