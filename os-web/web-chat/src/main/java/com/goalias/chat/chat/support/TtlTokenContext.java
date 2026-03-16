package com.goalias.chat.chat.support;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.goalias.common.satoken.utils.LoginHelper;

/**
 * @apiNote : 基于 TransmittableThreadLocal 封装工具类，用户保存和获取当前登录用户Sa-Token token值
 * @author Goalias
 **/
public class TtlTokenContext {
    private static final TransmittableThreadLocal<String> ttlThreadLocal = new TransmittableThreadLocal<>();

    /**
 * @apiNote:: 设置值
 * @author Goalias
     * @param: [token] 线程token
     **/
    public static void setCurrentToken(String token){
        ttlThreadLocal.set(token);
    }
    /**
 * @apiNote:: 获取值
 * @author Goalias
     **/
    public static String getCurrentToken(){
        return ttlThreadLocal.get();
    }

    public static void remove(){
        ttlThreadLocal.remove();
    }

    public static Long getCurrentUserId(){
        return LoginHelper.getLoginUser(ttlThreadLocal.get()).getUserId();
    }
}