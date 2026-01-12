package com.goalias.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户阅读状态视图对象 sys_notice_state
 *
 * @author Lion Li
 * @date 2024-05-11
 */
@Data
public class SysNoticeStateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 阅读状态（0未读 1已读）
     */
    private String readStatus;

    /**
     * 备注
     */
    private String remark;


}
