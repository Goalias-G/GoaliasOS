package com.goalias.knowledge.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.knowledge.domain.KnowledgeFragment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 知识片段Mapper接口
 *
 * @author Goalias
 * @since 2026-01-22 */
@Mapper
public interface KnowledgeFragmentMapper extends BaseMapper<KnowledgeFragment> {

    void insertBatch(List<KnowledgeFragment> list);
}
