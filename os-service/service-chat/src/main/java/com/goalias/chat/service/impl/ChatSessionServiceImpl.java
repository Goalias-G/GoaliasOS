package com.goalias.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.ChatMessage;
import com.goalias.chat.domain.ChatSession;
import com.goalias.chat.domain.bo.ChatSessionBo;
import com.goalias.chat.mapper.ChatMessageMapper;
import com.goalias.chat.mapper.ChatSessionMapper;
import com.goalias.chat.service.IChatSessionService;
import com.goalias.common.satoken.utils.LoginHelper;
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
 * 会话管理Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22
 */
@RequiredArgsConstructor
@Service
public class ChatSessionServiceImpl implements IChatSessionService {

    private final ChatSessionMapper baseMapper;
    private final ChatMessageMapper messageMapper;

    /**
     * 查询会话管理
     */
    @Override
    public ChatSession queryById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 查询会话管理列表
     */
    @Override
    public TableDataInfo<ChatSession> queryPageList(ChatSessionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChatSession> lqw = buildQueryWrapper(bo);
        Page<ChatSession> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询会话管理列表
     */
    @Override
    public List<ChatSession> queryList(ChatSessionBo bo) {
        LambdaQueryWrapper<ChatSession> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<ChatSession> buildQueryWrapper(ChatSessionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChatSession> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, ChatSession::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getSessionTitle()), ChatSession::getSessionTitle, bo.getSessionTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getSessionContent()), ChatSession::getSessionContent, bo.getSessionContent());
        lqw.eq(bo.getArchiveStatus() != null, ChatSession::getArchiveStatus, bo.getArchiveStatus());
        return lqw;
    }

    /**
     * 新增会话管理
     */
    @Override
    public Boolean insertByBo(ChatSessionBo bo) {
        ChatSession add = MapstructUtils.convert(bo, ChatSession.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改会话管理
     */
    @Override
    public Boolean updateByBo(ChatSessionBo bo) {
        ChatSession update = MapstructUtils.convert(bo, ChatSession.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 更新当前用户会话的归档状态，避免跨用户修改会话。
     */
    @Override
    public Boolean updateArchiveStatus(Long id, Integer archiveStatus) {
        ChatSession update = new ChatSession();
        update.setArchiveStatus(archiveStatus);
        return baseMapper.update(update, Wrappers.<ChatSession>lambdaUpdate()
            .eq(ChatSession::getId, id)
            .eq(ChatSession::getUserId, LoginHelper.getUserId())) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChatSession entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除会话管理
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        boolean isSuccess = baseMapper.deleteBatchIds(ids) > 0;
        if (isSuccess) {
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getUserId, LoginHelper.getUserId());
            queryWrapper.in(ChatMessage::getSessionId, ids);
            messageMapper.delete(queryWrapper);
        }
        return isSuccess;
    }
}
