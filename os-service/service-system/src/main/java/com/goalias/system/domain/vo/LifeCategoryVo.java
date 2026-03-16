package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 生活分类视图对象 life_category
 *
 * @author Goalias
 */
@Data
public class LifeCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String name;

    private Integer order;

    private Date createTime;

    private Date updateTime;

}
