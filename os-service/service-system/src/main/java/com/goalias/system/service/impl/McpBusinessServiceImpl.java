package com.goalias.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.FinanceCategory;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.mapper.DailyHealthMapper;
import com.goalias.system.mapper.FinanceCategoryMapper;
import com.goalias.system.mapper.FinanceTransactionMapper;
import com.goalias.system.mapper.LifeCategoryMapper;
import com.goalias.system.mapper.LifeRecordMapper;
import com.goalias.system.service.IMcpBusinessService;
import com.goalias.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP 业务能力门面 Service 实现
 * <p>
 * 与登录态解耦：所有查询通过显式 userId 条件访问数据，
 * 不复用内部依赖 LoginHelper 的 Service 方法，保证 MCP 无登录态调用安全。
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class McpBusinessServiceImpl implements IMcpBusinessService {

    private final ISysUserService sysUserService;

    private final LifeRecordMapper lifeRecordMapper;

    private final LifeCategoryMapper lifeCategoryMapper;

    private final DailyHealthMapper dailyHealthMapper;

    private final FinanceTransactionMapper financeTransactionMapper;

    private final FinanceCategoryMapper financeCategoryMapper;

    @Override
    public SysUserVo getUserProfile(Long userId) {
        return sysUserService.selectUserById(userId);
    }

    @Override
    public Map<Long, String> queryLifeCategoryNames(Long userId) {
        LambdaQueryWrapper<LifeCategory> lqw = Wrappers.lambdaQuery();
        lqw.eq(LifeCategory::getUserId, userId);
        List<LifeCategory> categories = lifeCategoryMapper.selectList(lqw);
        return categories.stream()
                .collect(Collectors.toMap(LifeCategory::getId, LifeCategory::getName, (a, b) -> a));
    }

    @Override
    public List<LifeRecord> queryRecentLifeRecords(Long userId, LocalDate beginDate, LocalDate endDate,
                                                   Long categoryId, int limit) {
        LambdaQueryWrapper<LifeRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(LifeRecord::getUserId, userId);
        lqw.eq(categoryId != null, LifeRecord::getCategoryId, categoryId);
        lqw.ge(beginDate != null, LifeRecord::getRecordDate, beginDate == null ? null : Timestamp.valueOf(beginDate.atStartOfDay()));
        lqw.le(endDate != null, LifeRecord::getRecordDate, endDate == null ? null : Timestamp.valueOf(endDate.atTime(LocalTime.MAX)));
        lqw.orderByDesc(LifeRecord::getRecordDate);
        lqw.orderByDesc(LifeRecord::getCreateTime);
        // limit 已在上层 clamp 到 [1,50]，此处拼接安全
        lqw.last("LIMIT " + Math.max(1, limit));
        return lifeRecordMapper.selectList(lqw);
    }

    @Override
    public List<DailyHealth> queryDailyHealthRecords(Long userId, LocalDate beginDate, LocalDate endDate) {
        LambdaQueryWrapper<DailyHealth> lqw = Wrappers.lambdaQuery();
        lqw.eq(DailyHealth::getUserId, userId);
        lqw.ge(beginDate != null, DailyHealth::getCreateTime, beginDate == null ? null : Timestamp.valueOf(beginDate.atStartOfDay()));
        lqw.le(endDate != null, DailyHealth::getCreateTime, endDate == null ? null : Timestamp.valueOf(endDate.atTime(LocalTime.MAX)));
        lqw.orderByDesc(DailyHealth::getCreateTime);
        return dailyHealthMapper.selectList(lqw);
    }

    @Override
    public FinanceOverviewVo getFinanceOverview(Long userId) {
        return financeTransactionMapper.selectOverview(userId);
    }

    @Override
    public List<FinanceCategoryPieVo> getFinanceCategoryPie(Long userId, String startDate, String endDate) {
        return financeTransactionMapper.selectCategoryPie(userId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceTransaction addExpense(Long userId, String categoryName, long amountCent, Integer tag, String remark) {
        if (StrUtil.isBlank(categoryName)) {
            throw new ServiceException("支出分类名称不能为空");
        }
        if (amountCent <= 0) {
            throw new ServiceException("支出金额必须大于0");
        }
        // 按用户+名称+支出类型精确匹配分类，未命中则自动创建
        LambdaQueryWrapper<FinanceCategory> categoryWrapper = Wrappers.lambdaQuery();
        categoryWrapper.eq(FinanceCategory::getUserId, userId);
        categoryWrapper.eq(FinanceCategory::getName, categoryName.trim());
        categoryWrapper.eq(FinanceCategory::getType, 1);
        FinanceCategory category = financeCategoryMapper.selectOne(categoryWrapper);
        if (category == null) {
            category = new FinanceCategory();
            category.setUserId(userId);
            category.setName(categoryName.trim());
            category.setType(1);
            financeCategoryMapper.insert(category);
        }
        FinanceTransaction transaction = new FinanceTransaction();
        transaction.setUserId(userId);
        transaction.setCategoryId(category.getId());
        transaction.setAmount(amountCent);
        transaction.setTag(tag);
        transaction.setRemark(remark);
        financeTransactionMapper.insert(transaction);
        return transaction;
    }
}
