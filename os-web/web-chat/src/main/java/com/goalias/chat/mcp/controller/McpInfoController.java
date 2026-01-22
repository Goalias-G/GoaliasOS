package com.goalias.chat.mcp.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.goalias.chat.domain.McpInfo;
import com.goalias.chat.domain.bo.McpInfoBo;
import com.goalias.chat.mcp.config.McpServerConfig;
import com.goalias.chat.mcp.domain.McpInfoRequest;
import com.goalias.chat.mcp.service.McpInfoService;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.annotation.RepeatSubmit;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/operator/mcpInfo")
public class McpInfoController extends BaseController {

    private final McpInfoService mcpInfoService;

/**
 * 查询MCP列表
 */
@SaCheckPermission("operator:mcpInfo:list")
@GetMapping("/list")
    public TableDataInfo<McpInfo> list(McpInfoBo bo, PageQuery pageQuery) {
        return mcpInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取MCP详细信息
     *
     * @param mcpId 主键
     */
    @SaCheckPermission("operator:mcpInfo:query")
    @GetMapping("/{mcpId}")
    public R<McpInfo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Integer mcpId) {
        return R.ok(mcpInfoService.queryById(mcpId));
    }

    /**
     * 新增MCP
     */
    @SaCheckPermission("operator:mcpInfo:add")
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody McpInfoBo bo) {
        return toAjax(mcpInfoService.insertByBo(bo));
    }

    /**
     * 修改MCP
     */
    @SaCheckPermission("operator:mcpInfo:edit")
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody McpInfoBo bo) {
        return toAjax(mcpInfoService.updateByBo(bo));
    }

    /**
     * 删除MCP
     *
     * @param mcpIds 主键串
     */
    @SaCheckPermission("operator:mcpInfo:remove")
    @DeleteMapping("/{mcpIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Integer[] mcpIds) {
        return toAjax(mcpInfoService.deleteWithValidByIds(List.of(mcpIds), true));
    }

    /**
     * 添加或更新 MCP 工具
     */
    @PostMapping("/tools")
    public R<McpInfo> saveToolConfig(@RequestBody McpInfoRequest request) {
        return R.ok(mcpInfoService.saveToolConfig(request));
    }

    /**
     * 获取所有活跃服务器名称
     */
    @GetMapping("/tools/names")
    public R<List<String>> getActiveServerNames() {
        return R.ok(mcpInfoService.getActiveServerNames());
    }

    /**
     * 根据名称获取工具配置
     */
    @GetMapping("/tools/{serverName}")
    public R<McpServerConfig> getToolConfig(@PathVariable String serverName) {
        return R.ok(mcpInfoService.getToolConfigByName(serverName));
    }

    /**
     * 启用工具
     */
    @PostMapping("/tools/{serverName}/enable")
    public Map<String, Object> enableTool(@PathVariable String serverName) {
        boolean success = mcpInfoService.enableTool(serverName);
        return Map.of("success", success);
    }

    /**
     * 禁用工具
     */
    @PostMapping("/tools/{serverName}/disable")
    public Map<String, Object> disableTool(@PathVariable String serverName) {
        boolean success = mcpInfoService.disableTool(serverName);
        return Map.of("success", success);
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/tools/{serverName}")
    public Map<String, Object> deleteTool(@PathVariable String serverName) {
        boolean success = mcpInfoService.deleteToolConfig(serverName);
        return Map.of("success", success);
    }
}
