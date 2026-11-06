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
                        .build(),
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(McpSchema.Tool.builder()
                                .name("add_expense")
                                .description("新增一笔支出流水：金额单位为元，分类不存在时自动创建，返回新增结果")
                                .inputSchema(JsonSchemas.object(Map.of(
                                        "amountYuan", JsonSchemas.property("number", "支出金额（元，必填）"),
                                        "categoryName", JsonSchemas.property("string", "支出分类名称（必填），如 餐饮/交通/购物"),
                                        "expenseType", JsonSchemas.enumProperty(
                                                "支出类型：necessary=必要支出，flexible=弹性支出",
                                                List.of("necessary", "flexible"), "flexible"),
                                        "remark", JsonSchemas.property("string", "备注（可选）")
                                ), "amountYuan", "categoryName"))
                                .build())
                        .callHandler((exchange, request) -> McpResults.call(() ->
                                addExpense(request.arguments())))
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

    /**
     * 新增支出：元转分、支出类型映射为流水标签，分类不存在时自动创建
     */
    private Map<String, Object> addExpense(Map<String, Object> arguments) {
        BigDecimal amountYuan = McpResults.getRequiredAmount(arguments, "amountYuan");
        if (amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支出金额必须大于0");
        }
        String categoryName = McpResults.getRequiredString(arguments, "categoryName");
        String expenseType = McpResults.getString(arguments, "expenseType", "flexible");
        String remark = McpResults.getString(arguments, "remark", "MCP 客户端记录");
        // 支出标签：1-必要支出 2-弹性支出
        int tag = "necessary".equalsIgnoreCase(expenseType) ? 1 : 2;
        long amountCent = McpResults.yuanToCent(amountYuan);
        FinanceTransaction transaction = businessService.addExpense(
                McpToolConstants.DEFAULT_USER_ID, categoryName, amountCent, tag, remark);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("transactionId", transaction.getId());
        result.put("categoryName", categoryName);
        result.put("amountYuan", amountYuan);
        result.put("expenseType", tag == 1 ? "necessary" : "flexible");
        result.put("message", "支出记录已添加");
        return result;
    }
}
