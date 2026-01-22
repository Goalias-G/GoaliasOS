package com.goalias.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话管理Mapper接口
 *
 * @author Goalias
 * @since 2026-01-22 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}
