package com.goalias.chat.chat.support;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @apiNote : 基于 TransmittableThreadLocal 封装工具类，用户保存和获取当前登录用户Sa-Token token值
 * @author Goalias
 **/
public class BaseContext {
    private static final TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();

    /**
 * @apiNote:: 设置值
 * @author Goalias
     * @param: [token] 线程token
     **/
    public static void setCurrentToken(String token){
        threadLocal.set(token);
    }
    /**
 * @apiNote:: 获取值
 * @author Goalias
     **/
    public static String getCurrentToken(){
        return threadLocal.get();
    }

    public static void remove(){
        threadLocal.remove();
    }
}