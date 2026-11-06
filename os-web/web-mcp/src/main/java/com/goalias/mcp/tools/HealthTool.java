package com.goalias.mcp.tools;

import cn.hutool.core.date.DateUtil;
import com.goalias.mcp.tools.support.JsonSchemas;
import com.goalias.mcp.tools.support.McpResults;
import com.goalias.mcp.tools.support.McpToolConstants;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.service.IMcpBusinessService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 饮食/运动记录 MCP 工具（数据来源：每日健康记录 daily_health）
 *
 * @author Goalias
 */
@Component
@RequiredArgsConstructor
public class HealthTool implements McpToolContributor {

    private final IMcpBusinessService businessService;

    @Override
    public List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        return List.of(
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(McpSchema.Tool.builder()
                                .name("get_meal_records")
                                .description("查询最近饮食记录（每日健康记录中的饮食字段），默认最近7天")
                                .inputSchema(JsonSchemas.object(Map.of(
                                        "days", JsonSchemas.property("integer", "查询最近多少天内的饮食记录", 7)
                                )))
                                .build())
                        .callHandler((exchange, request) -> McpResults.call(() ->
                                getHealthRecords(request.arguments(), true)))
                        .build(),
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(McpSchema.Tool.builder()
                                .name("get_exercise_records")
                                .description("查询最近运动记录（每日健康记录中的运动字段），默认最近7天")
                                .inputSchema(JsonSchemas.object(Map.of(
                                        "days", JsonSchemas.property("integer", "查询最近多少天内的运动记录", 7)
                                )))
                                .build())
                        .callHandler((exchange, request) -> McpResults.call(() ->
                                getHealthRecords(request.arguments(), false)))
                        .build());
    }

    /**
     * 查询饮食/运动记录：meal=true 输出饮食+作息，否则输出运动
     */
    private List<Map<String, Object>> getHealthRecords(Map<String, Object> arguments, boolean meal) {
        int days = Math.max(1, McpResults.getInt(arguments, "days", 7));
        LocalDate endDate = LocalDate.now();
        LocalDate beginDate = endDate.minusDays(days - 1L);
        List<DailyHealth> records = businessService.queryDailyHealthRecords(
                McpToolConstants.DEFAULT_USER_ID, beginDate, endDate);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DailyHealth record : records) {
            if (meal) {
                if (record.getFood() == null || record.getFood().isBlank()) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("date", DateUtil.formatDate(record.getCreateTime()));
                item.put("food", record.getFood());
                item.put("upTime", formatTime(record.getUpTime()));
                item.put("sleepTime", formatTime(record.getSleepTime()));
                result.add(item);
            } else {
                if (record.getExercise() == null || record.getExercise().isBlank()) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("date", DateUtil.formatDate(record.getCreateTime()));
                item.put("exercise", record.getExercise());
                result.add(item);
            }
        }
        return result;
    }

    private String formatTime(LocalTime time) {
        return time == null ? null : time.toString();
    }
}
