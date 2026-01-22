package com.goalias.chat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatUsageToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户token使用详情Mapper接口
 *
 * @author Goalias
 * @since 2026-01-22 */
@Mapper
public interface ChatUsageTokenMapper extends BaseMapper<ChatUsageToken> {

}
