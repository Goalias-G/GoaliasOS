package com.goalias.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatModel;
import org.apache.ibatis.annotations.Mapper;


/**
 * 聊天模型Mapper接口
 *
 * @author Goalias
 * @since 2026-01-22 */
@Mapper
public interface ChatModelMapper extends BaseMapper<ChatModel> {

}
