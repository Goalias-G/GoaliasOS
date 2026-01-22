package com.goalias.chat.service;


import com.goalias.chat.domain.ChatUsageToken;

/**
 * 聊天消息Service接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface IChatTokenService {

    /**
     * 查询用户token
     */
    ChatUsageToken queryByUserId(Long userId, String modelName);

    /**
     * 清空用户token
     */
    void resetToken(Long userId,String modelName);

    /**
     * 修改用户token
     */
    void editToken(ChatUsageToken chatToken);

}
