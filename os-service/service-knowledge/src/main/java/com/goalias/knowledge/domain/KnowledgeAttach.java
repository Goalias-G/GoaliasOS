package com.goalias.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 知识库附件对象 knowledge_attach
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_attach")
public class KnowledgeAttach extends BaseEntity {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   *
   */
  @TableId(value = "id", type = IdType.AUTO)
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
   * 文档名称
   */
  private String docName;

  /**
   * 文档类型
   */
  private String docType;

  /**
   * 文档内容
   */
  private String content;

  /**
   * 备注
   */
  private String remark;


  /**
   * 对象存储主键
   */
  private Long ossId;



  /**
   * 拆解图片状态10未开始，20进行中，30已完成
   */
  private Integer picStatus;

  /**
   * 分析图片状态10未开始，20进行中，30已完成
   */
  private Integer picAnysStatus;

  /**
   * 写入向量数据库状态10未开始，20进行中，30已完成
   */
  private Integer vectorStatus;

}
