package com.goalias.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP对象 mcp_info
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mcp_info")
public class McpInfo extends BaseEntity {


    /**
     * id
     */
    @TableId(value = "mcp_id", type = IdType.AUTO)
    private Integer mcpId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 链接方式
     */

    private String transportType;

    /**
     * Command
     */
    private String command;

    /**
     * Args
     */
    private String arguments;

    private String description;

    /**
     * Env
     */
    private String env;

    /**
     * 是否启用
     */
    private Boolean status;




}
