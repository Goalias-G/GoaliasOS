package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 生活记录视图对象 life_record
 *
 * @author Goalias
 */
@Data
public class LifeRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String content;

    private Integer rating;

    private Date recordDate;

    private List<String> attachsUrls;

    private Integer favoriteFlag;

    private String remark;

    private Date createTime;

    private Date updateTime;

}
