package com.goalias.mcp.tools;

import com.goalias.mcp.tools.support.JsonSchemas;
import com.goalias.mcp.tools.support.McpResults;
import com.goalias.mcp.tools.support.McpToolConstants;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.service.IMcpBusinessService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务 MCP 工具：收支汇总 + 新增支出
 *
 * @author Goalias
 */
@Component
@RequiredArgsConstructor
public class FinanceTool implements McpToolContributor {

    private final IMcpBusinessService businessService;

    @Override
    public List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        return List.of(
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(McpSchema.Tool.builder()
                                .name("get_finance_summary")
                                .description("获取收支汇总：累计与当月收支、结余，以及指定时间段内的分类支出占比（金额单位为元）")
                                .inputSchema(JsonSchemas.object(Map.of(
                                        "startDate", JsonSchemas.property("string", "分类占比统计开始日期 yyyy-MM-dd（可选）"),
                                        "endDate", JsonSchemas.property("string", "分类占比统计结束日期 yyyy-MM-dd（可选）")
                                )))
                                .build())
                        .callHandler((exchange, request) -> McpResults.call(() ->
                                getFinanceSummary(request.arguments())))
                        .build());
    }

    /**
     * 收支汇总（金额由分转换为元输出）
     */
    private Map<String, Object> getFinanceSummary(Map<String, Object> arguments) {
        String startDate = McpResults.getString(arguments, "startDate", null);
        String endDate = McpResults.getString(arguments, "endDate", null);
        FinanceOverviewVo overview = businessService.getFinanceOverview(McpToolConstants.DEFAULT_USER_ID);
        Map<String, Object> result = new LinkedHashMap<>();
        if (overview != null) {
            result.put("totalIncome", McpResults.centToYuan(overview.getTotalIncome()));
            result.put("totalExpense", McpResults.centToYuan(overview.getTotalExpense()));
            result.put("balance", McpResults.centToYuan(overview.getBalance()));
            result.put("monthIncome", McpResults.centToYuan(overview.getMonthIncome()));
            result.put("monthExpense", McpResults.centToYuan(overview.getMonthExpense()));
            result.put("monthBalance", McpResults.centToYuan(overview.getMonthBalance()));
        }
        List<Map<String, Object>> topCategories = new ArrayList<>();
        List<FinanceCategoryPieVo> pie = businessService.getFinanceCategoryPie(
                McpToolConstants.DEFAULT_USER_ID, startDate, endDate);
        if (pie != null) {
            for (FinanceCategoryPieVo category : pie) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("categoryName", category.getCategoryName());
                item.put("amount", McpResults.centToYuan(category.getAmount()));
                item.put("percentage", category.getPercentage());
                topCategories.add(item);
            }
        }
        result.put("topCategories", topCategories);
        return result;
    }
}
