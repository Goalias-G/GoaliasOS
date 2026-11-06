package com.goalias.mcp.tools.support;

import com.goalias.common.core.constant.UserConstants;

/**
 * MCP 工具层通用常量
 *
 * @author Goalias
 */
public final class McpToolConstants {

    /**
     * MCP 工具统一操作用户：所有工具不接收 userId 参数，固定为超级管理员
     */
    public static final Long DEFAULT_USER_ID = UserConstants.SUPER_ADMIN_ID;

    private McpToolConstants() {
    }
}
