package com.goalias.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户token使用记录
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@TableName("chat_usage_token")
public class ChatToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID", groups = { AddGroup.class, EditGroup.class })
    private Long UserId;

    /**
     * 待结算token
     */
    private Integer token;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

}
