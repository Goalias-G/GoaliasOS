package com.goalias.chat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息Mapper接口
 *
 * @author Goalias
 * @since 2026-01-22 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
