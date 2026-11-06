package com.goalias.mcp.tools;

import cn.hutool.core.date.DateUtil;
import com.goalias.mcp.tools.support.JsonSchemas;
import com.goalias.mcp.tools.support.McpResults;
import com.goalias.mcp.tools.support.McpToolConstants;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.service.IMcpBusinessService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生活记录 MCP 工具
 *
 * @author Goalias
 */
@Component
@RequiredArgsConstructor
public class LifeRecordTool implements McpToolContributor {

    private final IMcpBusinessService businessService;

    @Override
    public List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        return List.of(McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder()
                        .name("get_recent_lifes")
                        .description("查询最近生活记录（生活事件、场景分类、评分），默认最近7天、最多20条")
                        .inputSchema(JsonSchemas.object(Map.of(
                                "days", JsonSchemas.property("integer", "查询最近多少天内的生活记录", 7),
                                "limit", JsonSchemas.property("integer", "返回条数上限，最大50", 20),
                                "categoryId", JsonSchemas.property("integer", "生活场景分类ID（可选，不传查全部分类）")
                        )))
                        .build())
                .callHandler((exchange, request) -> McpResults.call(() ->
                        getRecentLifes(request.arguments())))
                .build());
    }

    private List<Map<String, Object>> getRecentLifes(Map<String, Object> arguments) {
        int days = Math.max(1, McpResults.getInt(arguments, "days", 7));
        int limit = Math.min(Math.max(1, McpResults.getInt(arguments, "limit", 20)), 50);
        Long categoryId = McpResults.getOptionalLong(arguments, "categoryId");
        LocalDate endDate = LocalDate.now();
        LocalDate beginDate = endDate.minusDays(days - 1L);
        List<LifeRecord> records = businessService.queryRecentLifeRecords(
                McpToolConstants.DEFAULT_USER_ID, beginDate, endDate, categoryId, limit);
        Map<Long, String> categoryNames = businessService.queryLifeCategoryNames(McpToolConstants.DEFAULT_USER_ID);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LifeRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordDate", DateUtil.formatDate(record.getRecordDate()));
            item.put("category", categoryNames.get(record.getCategoryId()));
            item.put("title", record.getTitle());
            item.put("content", record.getContent());
            item.put("rating", record.getRating());
            result.add(item);
        }
        return result;
    }
}
