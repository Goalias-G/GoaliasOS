package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.system.domain.DailyKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日知识Mapper接口
 *
 * @author Goalias
 */
@Mapper
public interface DailyKnowledgeMapper extends BaseMapper<DailyKnowledge> {

}
