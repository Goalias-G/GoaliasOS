package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 生活分类对象 life_category
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("life_category")
public class LifeCategory extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private Integer sortOrder;

    @TableField(exist = false)
    private Integer recordCount;

}
