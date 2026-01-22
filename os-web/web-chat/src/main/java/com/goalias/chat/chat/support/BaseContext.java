package com.goalias.chat.chat.support;

/**
 * @apiNote : 基于ThreadLocal封装工具类，用户保存和获取当前登录用户Sa-Token token值
 * @author Goalias
 **/
public class BaseContext {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

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
}