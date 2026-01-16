package com.goalias.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词模板Mapper接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

}