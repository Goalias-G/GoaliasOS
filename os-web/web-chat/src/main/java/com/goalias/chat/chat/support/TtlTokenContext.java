package com.goalias.chat.chat.support;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.core.domain.model.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * @apiNote : 基于 TransmittableThreadLocal 封装工具类，用于保存和获取当前登录用户Sa-Token token值
 * @author Goalias
 **/
@Slf4j
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

    /**
     * 获取当前登录用户ID
     * 注意：此方法依赖 TTL 上下文中的 token 值，在异步线程中需确保 token 已被设置
     * @throws IllegalStateException 当 token 未设置或用户信息获取失败时抛出
     */
    public static Long getCurrentUserId(){
        String token = ttlThreadLocal.get();
        if (token == null || token.isEmpty()) {
            log.error("TtlTokenContext 中未设置 token，当前线程: {}", Thread.currentThread().getName());
            throw new IllegalStateException("当前线程未设置用户 token 上下文，无法获取用户信息");
        }
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        if (loginUser == null) {
            log.error("通过 token 无法获取登录用户信息，token: {}..., 当前线程: {}", token.substring(0, Math.min(8, token.length())), Thread.currentThread().getName());
            throw new IllegalStateException("用户登录状态已失效，请重新登录");
        }
        return loginUser.getUserId();
    }
}
