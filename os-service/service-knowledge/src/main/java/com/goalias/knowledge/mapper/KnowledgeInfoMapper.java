package com.goalias.knowledge.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.knowledge.domain.KnowledgeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库Mapper接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Mapper
public interface KnowledgeInfoMapper extends BaseMapper<KnowledgeInfo> {

    /**
     * 根据kid查询知识库
     * @param kid 知识库id
     * @return KnowledgeInfo
     */
    KnowledgeInfo selectByKid(@Param("kid") String kid);
}
