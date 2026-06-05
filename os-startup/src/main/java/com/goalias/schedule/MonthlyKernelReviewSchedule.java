package com.goalias.schedule;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.goalias.chat.service.ISseService;
import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.constant.UserConstants;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.notification.core.MailTemplate;
import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.vo.FinanceTransactionVo;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.mapper.FinanceTransactionMapper;
import com.goalias.system.mapper.LifeCategoryMapper;
import com.goalias.system.mapper.LifeRecordMapper;
import com.goalias.system.service.ISysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * super_admin 月度内核复盘任务。
 *
 * <p>手动插入 prompt_template 时使用以下模板，category 对应
 * {@link PromptTemplateEnum#MONTHLY_KERNEL_REVIEW} 的 desc: monthlyKernelReview。</p>
 *
 * <pre>
 * 你是 GoaliasOS 的月度内核审计器。GoaliasOS 把个人成长抽象为操作系统运行过程：
 * 目标是进程，习惯是服务，反思是日志，成长是内核迭代。请基于本月收支流水和生活记录，
 * 生成一份可执行的月度内核复盘。
 *
 * 分析原则：
 * 1. 只使用下方观测数据，不要编造未出现的事实。
 * 2. 同时关注财务流量、生活日志、情绪/能量信号和重复模式。
 * 3. 输出要能直接指导下月调度，避免空泛建议。
 * 4. 如果数据不足，请明确指出盲区，并给出下月应该补充的记录维度。
 * 5. 必须只输出 JSON，不要输出 Markdown、代码块、解释文字或 JSON 之外的任何内容。
 *
 * JSON 输出结构如下，所有字段必须存在：
 * {
 *   "monthlySummary": "3-5 句话概括本月个人系统运行状态",
 *   "financeAnalysis": "分析收入、支出、结余、主要分类、异常波动和可优化点",
 *   "lifeLogAnalysis": "分析高频场景、低分记录、积极事件、风险信号和成长线索",
 *   "couplingInsights": [
 *     "收支变化与生活状态之间的关联洞察，并标注强证据/弱证据/需要继续观察"
 *   ],
 *   "nextMonthKernelSchedules": [
 *     {
 *       "trigger": "触发条件",
 *       "action": "执行动作",
 *       "metric": "复盘指标"
 *     }
 *   ],
 *   "dataCollectionNeeds": [
 *     "下月应补充记录的字段或场景"
 *   ]
 * }
 *
 * 以下是本月观测数据：
 * %s
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlyKernelReviewSchedule {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final String TEMPLATE_PATH = "classpath:templates/monthlyKernelReviewTemplate.html";

    private final ISseService sseService;

    private final MailTemplate mailTemplate;

    private final ISysUserService userService;

    private final ResourceLoader resourceLoader;

    private final FinanceTransactionMapper financeTransactionMapper;

    private final LifeRecordMapper lifeRecordMapper;

    private final LifeCategoryMapper lifeCategoryMapper;

    /**
     * 每月最后一天 20:30 触发，仅为 super_admin 生成月度内核复盘并发送邮件。
     */
    @Scheduled(cron = "0 30 20 L * ?", zone = "Asia/Shanghai")
    public void monthlyKernelReview() {
        try {
            Long userId = UserConstants.SUPER_ADMIN_ID;
            LocalDate monthStart = LocalDate.now(ZONE_ID).withDayOfMonth(1);
            LocalDate nextMonthStart = monthStart.plusMonths(1);

            List<FinanceTransactionVo> transactions = queryMonthlyTransactions(userId, monthStart, nextMonthStart);
            List<LifeRecord> lifeRecords = queryMonthlyLifeRecords(userId, monthStart, nextMonthStart);
            Map<Long, String> lifeCategoryNameMap = queryLifeCategoryNameMap(lifeRecords);

            String promptPayload = buildPromptPayload(monthStart, nextMonthStart, transactions, lifeRecords, lifeCategoryNameMap);
            // prompt_template.template_content 中保留一个 %s，simpleChat 会把 promptPayload 动态插入到完整提示词中。
            String aiReply = sseService.simpleChat(new ChatRequest(), PromptTemplateEnum.MONTHLY_KERNEL_REVIEW, promptPayload);
            MonthlyKernelReviewResult reviewResult = parseReviewResult(aiReply);

            sendMonthlyReviewMail(userId, monthStart, transactions.size(), lifeRecords.size(), reviewResult);
            log.info("super_admin 月度内核复盘已生成并发送，月份: {}", monthStart.format(MONTH_FORMATTER));
        } catch (Exception e) {
            log.error("super_admin 月度内核复盘任务执行失败", e);
        }
    }

    private List<FinanceTransactionVo> queryMonthlyTransactions(Long userId, LocalDate monthStart, LocalDate nextMonthStart) {
        return financeTransactionMapper.selectTransactionList(
                userId,
                null,
                null,
                startOfDay(monthStart),
                startOfDay(nextMonthStart)
        );
    }

    private List<LifeRecord> queryMonthlyLifeRecords(Long userId, LocalDate monthStart, LocalDate nextMonthStart) {
        Date startTime = Date.from(monthStart.atStartOfDay(ZONE_ID).toInstant());
        Date endTime = Date.from(nextMonthStart.atStartOfDay(ZONE_ID).toInstant());
        LambdaQueryWrapper<LifeRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(LifeRecord::getUserId, userId)
                .ge(LifeRecord::getRecordDate, startTime)
                .lt(LifeRecord::getRecordDate, endTime)
                .orderByAsc(LifeRecord::getRecordDate)
                .orderByAsc(LifeRecord::getCreateTime);
        return lifeRecordMapper.selectList(wrapper);
    }

    private Map<Long, String> queryLifeCategoryNameMap(List<LifeRecord> lifeRecords) {
        Set<Long> categoryIds = lifeRecords.stream()
                .map(LifeRecord::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return lifeCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(LifeCategory::getId, LifeCategory::getName, (left, right) -> left));
    }

    private String buildPromptPayload(LocalDate monthStart, LocalDate nextMonthStart,
                                      List<FinanceTransactionVo> transactions,
                                      List<LifeRecord> lifeRecords,
                                      Map<Long, String> lifeCategoryNameMap) {
        long totalIncome = sumAmountYuan(transactions, 2);
        long totalExpense = sumAmountYuan(transactions, 1);
        String month = monthStart.format(MONTH_FORMATTER);

        return """
                ## 运行窗口
                - 月份：%s
                - 起止：%s 至 %s（右开区间）

                ## 财务总览（金额单位：人民币/元）
                - 流水数量：%d
                - 收入合计：%d
                - 支出合计：%d
                - 结余：%d

                ## 财务流水明细
                %s

                ## 生活记录总览
                - 记录数量：%d

                ## 生活记录明细
                %s
                """.formatted(
                month,
                monthStart.format(DATE_FORMATTER),
                nextMonthStart.format(DATE_FORMATTER),
                transactions.size(),
                totalIncome,
                totalExpense,
                totalIncome - totalExpense,
                buildFinanceDetails(transactions),
                lifeRecords.size(),
                buildLifeRecordDetails(lifeRecords, lifeCategoryNameMap)
        );
    }

    private String buildFinanceDetails(List<FinanceTransactionVo> transactions) {
        if (transactions.isEmpty()) {
            return "- 本月没有收支流水。";
        }
        return transactions.stream()
                .map(transaction -> "- %s | %s | 分类：%s | 金额：%s | 标签：%s | 备注：%s".formatted(
                        formatDateTime(transaction.getCreateTime()),
                        financeType(transaction.getCategoryType()),
                        Optional.ofNullable(transaction.getCategoryName()).orElse("未分类"),
                        formatAmountYuan(transaction.getAmount()),
                        Optional.ofNullable(transaction.getTag()).map(String::valueOf).orElse("无"),
                        clip(transaction.getRemark(), 160)
                ))
                .collect(Collectors.joining("\n"));
    }

    private String buildLifeRecordDetails(List<LifeRecord> lifeRecords, Map<Long, String> lifeCategoryNameMap) {
        if (lifeRecords.isEmpty()) {
            return "- 本月没有生活记录。";
        }
        return lifeRecords.stream()
                .map(record -> "- %s | 场景：%s | 评分：%s | 标题：%s | 内容：%s | 备注：%s".formatted(
                        formatDate(record.getRecordDate()),
                        lifeCategoryNameMap.getOrDefault(record.getCategoryId(), "未分类"),
                        Optional.ofNullable(record.getRating()).map(String::valueOf).orElse("无"),
                        clip(record.getTitle(), 80),
                        clip(record.getContent(), 260),
                        clip(record.getRemark(), 120)
                ))
                .collect(Collectors.joining("\n"));
    }

    private MonthlyKernelReviewResult parseReviewResult(String aiReply) {
        String json = extractJson(aiReply);
        if (StringUtils.isBlank(json) || !JSONUtil.isTypeJSON(json)) {
            log.warn("月度内核复盘 AI 响应不是合法 JSON: {}", aiReply);
            return fallbackReviewResult(aiReply);
        }
        try {
            MonthlyKernelReviewResult result = JSONUtil.toBean(json, MonthlyKernelReviewResult.class);
            result.setRawReply(aiReply);
            return result;
        } catch (Exception e) {
            log.warn("月度内核复盘 AI 响应 JSON 解析失败: {}", aiReply, e);
            return fallbackReviewResult(aiReply);
        }
    }

    private String extractJson(String aiReply) {
        if (StringUtils.isBlank(aiReply)) {
            return null;
        }
        String trimmed = aiReply.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return trimmed;
        }
        return trimmed.substring(start, end + 1);
    }

    private MonthlyKernelReviewResult fallbackReviewResult(String aiReply) {
        MonthlyKernelReviewResult result = new MonthlyKernelReviewResult();
        result.setMonthlySummary("AI 未按约定返回 JSON，本次邮件保留原始回复供人工查看。");
        result.setFinanceAnalysis("请检查 prompt_template.category=monthlyKernelReview 的模板是否要求只输出 JSON。");
        result.setLifeLogAnalysis("本次未能解析结构化生活日志分析。");
        result.setCouplingInsights(List.of("结构化解析失败，无法提取系统耦合洞察。"));
        result.setNextMonthKernelSchedules(List.of());
        result.setDataCollectionNeeds(List.of("修正提示词模板后等待下次任务重新生成结构化复盘。"));
        result.setRawReply(Optional.ofNullable(aiReply).orElse("AI 未返回内容"));
        return result;
    }

    private void sendMonthlyReviewMail(Long userId, LocalDate monthStart, int financeCount, int lifeCount,
                                       MonthlyKernelReviewResult reviewResult) throws Exception {
        SysUserVo user = userService.selectUserById(userId);
        if (user == null || StringUtils.isBlank(user.getEmail())) {
            log.warn("super_admin 未配置邮箱，跳过月度内核复盘邮件发送");
            return;
        }

        Resource resource = resourceLoader.getResource(TEMPLATE_PATH);
        String template;
        try (InputStream inputStream = resource.getInputStream()) {
            template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String month = monthStart.format(MONTH_FORMATTER);
        String html = template
                .replace("{{month}}", escapeHtml(month))
                .replace("{{financeCount}}", String.valueOf(financeCount))
                .replace("{{lifeCount}}", String.valueOf(lifeCount))
                .replace("{{generatedAt}}", escapeHtml(DATE_TIME_FORMATTER.format(LocalDateTime.now(ZONE_ID))))
                .replace("{{monthlySummary}}", toHtmlContent(reviewResult.getMonthlySummary()))
                .replace("{{financeAnalysis}}", toHtmlContent(reviewResult.getFinanceAnalysis()))
                .replace("{{lifeLogAnalysis}}", toHtmlContent(reviewResult.getLifeLogAnalysis()))
                .replace("{{couplingInsights}}", toHtmlList(reviewResult.getCouplingInsights()))
                .replace("{{nextMonthKernelSchedules}}", toScheduleHtml(reviewResult.getNextMonthKernelSchedules()))
                .replace("{{dataCollectionNeeds}}", toHtmlList(reviewResult.getDataCollectionNeeds()));
//                .replace("{{rawReply}}", toHtmlContent(reviewResult.getRawReply()));
        mailTemplate.sendHtmlMail(user.getEmail(), "Goalias OS 月度内核复盘 - " + month, html);
    }

    private long sumAmountYuan(List<FinanceTransactionVo> transactions, Integer categoryType) {
        long amountCent = transactions.stream()
                .filter(transaction -> Objects.equals(categoryType, transaction.getCategoryType()))
                .map(FinanceTransactionVo::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        return amountCent / 100;
    }

    private String formatAmountYuan(Long amountCent) {
        if (amountCent == null) {
            return "0";
        }
        return String.valueOf(amountCent / 100);
    }

    private String financeType(Integer categoryType) {
        if (Objects.equals(categoryType, 1)) {
            return "支出";
        }
        if (Objects.equals(categoryType, 2)) {
            return "收入";
        }
        return "未知";
    }

    private String startOfDay(LocalDate date) {
        return date.atStartOfDay().format(DATE_TIME_FORMATTER);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "无日期";
        }
        return DATE_FORMATTER.format(date.toInstant().atZone(ZONE_ID).toLocalDate());
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "无时间";
        }
        return DATE_TIME_FORMATTER.format(date.toInstant().atZone(ZONE_ID).toLocalDateTime());
    }

    private String clip(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return "无";
        }
        String normalized = value.replace("\r\n", "\n").replace("\r", "\n").replace("\n", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private String toHtmlContent(String value) {
        return escapeHtml(Optional.ofNullable(value).orElse("无"))
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br/>");
    }

    private String toHtmlList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "<div style=\"color:#667085;\">无</div>";
        }
        return values.stream()
                .filter(StringUtils::isNotBlank)
                .map(value -> "<li>" + toHtmlContent(value) + "</li>")
                .collect(Collectors.joining("", "<ul style=\"margin:0;padding-left:20px;\">", "</ul>"));
    }

    private String toScheduleHtml(List<KernelSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return "<div style=\"color:#667085;\">无</div>";
        }
        return schedules.stream()
                .map(schedule -> """
                        <li style="margin-bottom:10px;">
                            <div><strong>触发条件：</strong>%s</div>
                            <div><strong>执行动作：</strong>%s</div>
                            <div><strong>复盘指标：</strong>%s</div>
                        </li>
                        """.formatted(
                        toHtmlContent(schedule.getTrigger()),
                        toHtmlContent(schedule.getAction()),
                        toHtmlContent(schedule.getMetric())
                ))
                .collect(Collectors.joining("", "<ul style=\"margin:0;padding-left:20px;\">", "</ul>"));
    }

    private String escapeHtml(String value) {
        return Optional.ofNullable(value).orElse("")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Data
    public static class MonthlyKernelReviewResult {
        private String monthlySummary;
        private String financeAnalysis;
        private String lifeLogAnalysis;
        private List<String> couplingInsights = List.of();
        private List<KernelSchedule> nextMonthKernelSchedules = List.of();
        private List<String> dataCollectionNeeds = List.of();
        private String rawReply;
    }

    @Data
    public static class KernelSchedule {
        private String trigger;
        private String action;
        private String metric;
    }
}
