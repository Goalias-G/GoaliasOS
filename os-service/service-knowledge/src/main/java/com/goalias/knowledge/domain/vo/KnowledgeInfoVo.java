package com.goalias.knowledge.domain.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;




/**
 * 知识库视图对象 knowledge_info
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class KnowledgeInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;

    /**
     * 知识库ID
     */
    private String kid;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 知识库名称
     */
    private String kname;

    /**
     * 是否公开知识库（0 否 1是）
     */
    private Integer share;

    /**
     * 描述
     */
    private String description;

    /**
     * 知识分隔符
     */
    private String knowledgeSeparator;

    /**
     * 提问分隔符
     */
    private String questionSeparator;

    /**
     * 重叠字符数
     */
    private Integer overlapChar;

    /**
     * 知识库中检索的条数
     */
    private Integer retrieveLimit;

    /**
     * 文本块大小
     */
    private Integer textBlockSize;

    /**
     * 向量库模型名称
     */
    private String vectorModelName;

    /**
     * 向量化模型id
     */
    private Long embeddingModelId;

    /**
     * 向量化模型名称
     */
    private String embeddingModelName;


    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 备注
     */
    private String remark;


}
