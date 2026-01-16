package com.goalias.knowledge.domain.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;




/**
 * 知识片段视图对象 knowledge_fragment
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class KnowledgeFragmentVo implements Serializable {

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
     * 文档ID
     */
    private String docId;

    /**
     * 知识片段ID
     */
    private String fid;

    /**
     * 片段索引下标
     */
    private Long idx;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 备注
     */
    private String remark;


}
