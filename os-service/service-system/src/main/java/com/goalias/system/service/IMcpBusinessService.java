package com.goalias.system.service;

import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.vo.FinanceCategoryPieVo;
import com.goalias.system.domain.vo.FinanceOverviewVo;
import com.goalias.system.domain.vo.SysUserVo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MCP 业务能力门面 Service 接口
 * <p>
 * 作为业务能力对外（MCP Server）输出的统一出口，与登录态彻底解耦：
 * 所有方法显式接收 userId，内部不调用 LoginHelper。
 *
 * @author Goalias
 */
public interface IMcpBusinessService {

    /**
     * 查询用户基本画像
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUserVo getUserProfile(Long userId);

    /**
     * 查询生活场景分类映射（id -> name）
     *
     * @param userId 用户ID
     * @return 分类ID到名称的映射
     */
    Map<Long, String> queryLifeCategoryNames(Long userId);

    /**
     * 查询时间区间内的生活记录（按记录日期倒序）
     *
     * @param userId     用户ID
     * @param beginDate  开始日期（含）
     * @param endDate    结束日期（含）
     * @param categoryId 场景分类ID（可空）
     * @param limit      返回条数上限
     * @return 生活记录列表
     */
    List<LifeRecord> queryRecentLifeRecords(Long userId, LocalDate beginDate, LocalDate endDate,
                                            Long categoryId, int limit);

    /**
     * 查询时间区间内的每日健康记录（含饮食/运动字段，按创建时间倒序）
     *
     * @param userId    用户ID
     * @param beginDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 每日健康记录列表
     */
    List<DailyHealth> queryDailyHealthRecords(Long userId, LocalDate beginDate, LocalDate endDate);

    /**
     * 获取收支总览
     *
     * @param userId 用户ID
     * @return 收支总览
     */
    FinanceOverviewVo getFinanceOverview(Long userId);

    /**
     * 获取分类支出占比（饼图数据）
     *
     * @param userId    用户ID
     * @param startDate 开始日期 yyyy-MM-dd（可空）
     * @param endDate   结束日期 yyyy-MM-dd（可空）
     * @return 分类占比列表
     */
    List<FinanceCategoryPieVo> getFinanceCategoryPie(Long userId, String startDate, String endDate);

    /**
     * 新增支出流水（分类不存在时自动创建支出分类）
     *
     * @param userId       用户ID
     * @param categoryName 支出分类名称
     * @param amountCent   金额（分）
     * @param tag          流水标签 1-必要支出 2-弹性支出
     * @param remark       备注
     * @return 新增的支出流水
     */
    FinanceTransaction addExpense(Long userId, String categoryName, long amountCent, Integer tag, String remark);
}
