package com.goalias.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 生活记录信息详情表 life_record
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("life_record")
public class LifeRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String content;

    private Integer rating;

    private Date recordDate;

    private String attachsId;

    private Integer favoriteFlag;

    private String remark;

    @TableField(exist = false)
    private java.util.List<String> attachsUrls;

}
