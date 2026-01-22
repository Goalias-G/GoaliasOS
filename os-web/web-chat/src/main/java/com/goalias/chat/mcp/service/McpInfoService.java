package com.goalias.chat.mcp.service;


import com.goalias.chat.domain.McpInfo;
import com.goalias.chat.domain.bo.McpInfoBo;
import com.goalias.chat.mcp.config.McpConfig;
import com.goalias.chat.mcp.config.McpServerConfig;
import com.goalias.chat.mcp.domain.McpInfoRequest;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * MCPService接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface McpInfoService {

    /**
     * 查询MCP
     */
        McpInfo queryById(Integer mcpId);

        /**
         * 查询MCP列表
         */
        TableDataInfo<McpInfo> queryPageList(McpInfoBo bo, PageQuery pageQuery);

    /**
     * 查询MCP列表
     */
    List<McpInfo> queryList(McpInfoBo bo);

    /**
     * 新增MCP
     */
    Boolean insertByBo(McpInfoBo bo);

    /**
     * 修改MCP
     */
    Boolean updateByBo(McpInfoBo bo);

    /**
     * 校验并批量删除MCP信息
     */
    Boolean deleteWithValidByIds(Collection<Integer> ids, Boolean isValid);

    McpServerConfig getToolConfigByName(String serverName);

    McpConfig getAllActiveMcpConfig();

    List<String> getActiveServerNames();

    McpInfo saveToolConfig(McpInfoRequest request);

    boolean deleteToolConfig(String serverName);

    boolean updateToolStatus(String serverName, Boolean status);

    boolean enableTool(String serverName);

    boolean disableTool(String serverName);
}
