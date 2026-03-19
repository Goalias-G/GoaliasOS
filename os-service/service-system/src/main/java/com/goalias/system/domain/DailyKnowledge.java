package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 每日知识表 daily_knowledge
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_knowledge")
@Builder
public class DailyKnowledge extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String type;

    private String title;

    private String content;
}
