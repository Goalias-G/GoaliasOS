package com.goalias.common.rateLimiter.strategy;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.csp.sentinel.util.MethodUtil;
import com.goalias.common.rateLimiter.enums.GoaliasStrategyEnum;
import com.goalias.common.rateLimiter.spring.GoaliasConfigHolder;


import java.lang.reflect.Method;

public class MethodHotStrategy implements GoaliasStrategy{

    private final TimedCache<String, Object> timedCache;

    public MethodHotStrategy() {
        timedCache= CacheUtil.newTimedCache(1000L * GoaliasConfigHolder.getGoaliasProperty().getHotCacheSeconds());
        timedCache.schedulePrune(1000L);//定时清理过期的缓存项
    }

    @Override
    public GoaliasStrategyEnum getStrategy() {
        return GoaliasStrategyEnum.HOT_METHOD;
    }

    @Override
    public Object process(Object bean, Method method, Object[] args) {
        String hotKey = MethodUtil.resolveMethodName(method);
        if (timedCache.containsKey(hotKey)) {
            return timedCache.get(hotKey, false);
        } else {
            Object result = ReflectUtil.invoke(bean, method, args);
            timedCache.put(hotKey, result);
            return result;
        }
    }
}
