package com.goalias.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.domain.bo.ChatUsageTokenBo;
import com.goalias.chat.mapper.ChatUsageTokenMapper;
import com.goalias.chat.service.IChatUsageTokenService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户token使用详情Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-16
 */
@RequiredArgsConstructor
@Service
public class ChatUsageTokenServiceImpl implements IChatUsageTokenService {

    private final ChatUsageTokenMapper baseMapper;

    /**
     * 查询用户token使用详情
     */
    @Override
    public ChatUsageToken queryById(Long id){
        return baseMapper.selectById(id);
    }

    /**
     * 查询用户token使用详情列表
     */
    @Override
    public TableDataInfo<ChatUsageToken> queryPageList(ChatUsageTokenBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChatUsageToken> lqw = buildQueryWrapper(bo);
        Page<ChatUsageToken> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用户token使用详情列表
     */
    @Override
    public List<ChatUsageToken> queryList(ChatUsageTokenBo bo) {
        LambdaQueryWrapper<ChatUsageToken> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<ChatUsageToken> buildQueryWrapper(ChatUsageTokenBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChatUsageToken> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, ChatUsageToken::getUserId, bo.getUserId());
        lqw.eq(bo.getToken() != null, ChatUsageToken::getToken, bo.getToken());
        lqw.like(StringUtils.isNotBlank(bo.getModelName()), ChatUsageToken::getModelName, bo.getModelName());
        lqw.eq(StringUtils.isNotBlank(bo.getTotalToken()), ChatUsageToken::getTotalToken, bo.getTotalToken());
        return lqw;
    }

    /**
     * 新增用户token使用详情
     */
    @Override
    public Boolean insertByBo(ChatUsageTokenBo bo) {
        ChatUsageToken add = MapstructUtils.convert(bo, ChatUsageToken.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户token使用详情
     */
    @Override
    public Boolean updateByBo(ChatUsageTokenBo bo) {
        ChatUsageToken update = MapstructUtils.convert(bo, ChatUsageToken.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChatUsageToken entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除用户token使用详情
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
