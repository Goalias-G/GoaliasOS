package com.goalias.domain.bo;

import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户阅读状态业务对象 sys_notice_state
 *
 * @author Lion Li
 * @date 2024-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeStateBo extends BaseEntity {

    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 公告ID
     */
    @NotNull(message = "公告ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long noticeId;

    /**
     * 阅读状态（0未读 1已读）
     */
    @NotBlank(message = "阅读状态（0未读 1已读）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String readStatus;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;


}
