package com.goalias.chat.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * MCP业务对象 mcp_info
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data

public class McpInfoBo implements Serializable {

        /**
         * id
         */
    @NotNull(message = "id不能为空" )
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
