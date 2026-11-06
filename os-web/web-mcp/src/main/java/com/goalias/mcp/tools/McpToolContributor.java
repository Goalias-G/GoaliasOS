package com.goalias.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;

import java.util.List;

/**
 * MCP 工具提供者接口
 * <p>
 * 提供者模式：每个实现向 MCP Server 贡献一组工具定义，
 * 新增业务工具只需新增实现类，无需修改装配逻辑（开闭原则）。
 *
 * @author Goalias
 */
public interface McpToolContributor {

    /**
     * 当前提供者贡献的工具规格列表
     *
     * @return 工具规格列表
     */
    List<McpServerFeatures.SyncToolSpecification> toolSpecifications();
}
