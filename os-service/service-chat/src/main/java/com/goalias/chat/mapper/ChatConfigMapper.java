package com.goalias.chat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatConfig;
import org.apache.ibatis.annotations.Mapper;


/**
 * 配置信息Mapper接口
 *
 * @author Goalias
 */
@Mapper
public interface ChatConfigMapper extends BaseMapper<ChatConfig> {

}
