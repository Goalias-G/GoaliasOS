package com.goalias.system.schedule.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.goalias.common.core.constant.UserConstants;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.schedule.enums.TaskType;
import com.goalias.common.schedule.executor.AbstractTaskExecutor;
import com.goalias.common.schedule.executor.TaskExecutionContext;
import com.goalias.system.domain.FinanceCategory;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.mapper.FinanceCategoryMapper;
import com.goalias.system.mapper.FinanceTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 财务任务执行器
 * <p>
 * 根据任务自定义参数定时向 {@code finance_transaction} 写入支出或收入记录：
 * <ul>
 *     <li>params.categoryId 必填，决定流水类型（支出/收入由分类 type 决定）</li>
 *     <li>params.amount 可选，默认 0</li>
 *     <li>params.tag 可选，默认 2（弹性支出）</li>
 *     <li>params.remark 可选，默认 "[自动] 定时任务生成"</li>
 * </ul>
 *
 * @author Goalias
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceTaskExecutor extends AbstractTaskExecutor {

    public static final String PARAM_CATEGORY_ID = "categoryId";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_TAG = "tag";
    public static final String PARAM_REMARK = "remark";

    private static final int DEFAULT_TAG = 2;
    private static final long DEFAULT_AMOUNT = 0L;
    private static final String DEFAULT_REMARK_PREFIX = "[自动] 定时任务生成";

    private final FinanceCategoryMapper financeCategoryMapper;
    private final FinanceTransactionMapper financeTransactionMapper;

    @Override
    public TaskType supports() {
        return TaskType.FINANCE;
    }

    @Override
    protected void doExecute(TaskExecutionContext context) {
        Long userId = ObjectUtil.defaultIfNull(context.getUserId(), UserConstants.SUPER_ADMIN_ID);
        Map<String, Object> params = context.getParams();
        if (CollUtil.isEmpty(params)) {
            throw new ServiceException("FINANCE 任务缺少自定义参数");
        }
        Long categoryId = toLong(params.get(PARAM_CATEGORY_ID));
        if (categoryId == null) {
            throw new ServiceException("FINANCE 任务必须传入 params.categoryId");
        }
        FinanceCategory category = financeCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ServiceException("财务分类不存在: " + categoryId);
        }
        if (!ObjectUtil.equal(category.getUserId(), userId)) {
            throw new ServiceException("分类与任务所属用户不匹配");
        }
        Long amount = ObjectUtil.defaultIfNull(toLong(params.get(PARAM_AMOUNT)), DEFAULT_AMOUNT);
        Integer tag = ObjectUtil.defaultIfNull(toInteger(params.get(PARAM_TAG)), DEFAULT_TAG);
        String remark = toString(params.get(PARAM_REMARK));
        if (remark == null || remark.isEmpty()) {
            remark = DEFAULT_REMARK_PREFIX;
        }
        if (amount < 0) {
            amount = Math.abs(amount);
        }

        FinanceTransaction tx = new FinanceTransaction();
        tx.setUserId(userId);
        tx.setCategoryId(categoryId);
        tx.setAmount(amount);
        tx.setTag(tag);
        tx.setRemark(remark);
        tx.setCreateTime(new Date());
        tx.setUpdateTime(new Date());
        financeTransactionMapper.insert(tx);
        log.info("[财务任务] 已为用户 {} 写入{}记录 txId={} categoryId={} amount={} tag={}",
            userId, category.getType() == 1 ? "支出" : "收入", tx.getId(), categoryId, amount, tag);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }
}
