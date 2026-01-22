package com.goalias.chat.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.mapper.ChatUsageTokenMapper;
import com.goalias.chat.service.IChatTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 聊天消息Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22 */
@RequiredArgsConstructor
@Service
public class ChatTokenServiceImpl implements IChatTokenService {

    private final ChatUsageTokenMapper baseMapper;

    @Override
    public ChatUsageToken queryByUserId(Long userId, String modelName) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<ChatUsageToken>()
                        .eq(ChatUsageToken::getUserId, userId)
                        .eq(ChatUsageToken::getModelName, modelName),
                false
        );
    }

    /**
     * 清空用户token
     *
     */
    @Override
    public void resetToken(Long userId, String modelName) {
        ChatUsageToken chatToken = queryByUserId(userId, modelName);
        chatToken.setToken(0);
        baseMapper.updateById(chatToken);
    }

    /**
     * 增加用户token
     *
     */
    @Override
    public void editToken(ChatUsageToken chatToken) {
        if (chatToken.getId() == null) {
            baseMapper.insert(chatToken);
        } else {
            baseMapper.updateById(chatToken);
        }
    }
}
