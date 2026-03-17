package com.goalias.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.domain.bo.ChatUsageTokenBo;
import com.goalias.chat.mapper.ChatUsageTokenMapper;
import com.goalias.chat.service.IChatUsageTokenService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户token使用详情Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatUsageTokenServiceImpl implements IChatUsageTokenService {

    private final ChatUsageTokenMapper baseMapper;

    @Lazy
    private final RedisService redisService;

    /**
     * 程序启动时加载数据库数据到Redis
     */
//    @PostConstruct
    public void loadToRedis() {
        log.info("开始从数据库加载Token使用量到Redis...");
        try {
            List<ChatUsageToken> tokenList = baseMapper.selectList(null);
            if (tokenList != null && !tokenList.isEmpty()) {
                Map<String, Object> inputTokenMap = new HashMap<>();
                Map<String, Object> outputTokenMap = new HashMap<>();
                
                for (ChatUsageToken token : tokenList) {
                    String modelName = token.getModelName();
                    if (modelName != null) {
                        if (token.getInputToken() != null) {
                            inputTokenMap.put(modelName, token.getInputToken());
                        }
                        if (token.getOutputToken() != null) {
                            outputTokenMap.put(modelName, token.getOutputToken());
                        }
                    }
                }
                
                if (!inputTokenMap.isEmpty()) {
                    redisService.hmSet(CacheNames.CHAT_TOKEN_INPUT, inputTokenMap);
                }
                if (!outputTokenMap.isEmpty()) {
                    redisService.hmSet(CacheNames.CHAT_TOKEN_OUTPUT, outputTokenMap);
                }
                log.info("成功加载 {} 条Token使用记录到Redis", tokenList.size());
            } else {
                log.info("数据库中没有Token使用记录");
            }
        } catch (Exception e) {
            log.error("从数据库加载Token使用量到Redis失败", e);
        }
    }

    /**
     * 每6小时同步Redis数据到数据库
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void syncToDatabase() {
        log.info("开始同步Redis Token使用量到数据库...");
        try {
            Map<String, Object> inputTokenMap = redisService.hmGet(CacheNames.CHAT_TOKEN_INPUT);
            Map<String, Object> outputTokenMap = redisService.hmGet(CacheNames.CHAT_TOKEN_OUTPUT);

            if (inputTokenMap == null) {
                inputTokenMap = new java.util.HashMap<>();
            }
            if (outputTokenMap == null) {
                outputTokenMap = new java.util.HashMap<>();
            }

            Set<String> modelNames = new HashSet<>(inputTokenMap.keySet());
            modelNames.addAll(outputTokenMap.keySet());

            LocalDateTime now = LocalDateTime.now();
            int syncCount = 0;

            for (String modelName : modelNames) {
                if (modelName == null || modelName.isEmpty()) {
                    continue;
                }

                Long inputToken = inputTokenMap.get(modelName) != null 
                    ? Long.parseLong(inputTokenMap.get(modelName).toString()) 
                    : 0L;
                Long outputToken = outputTokenMap.get(modelName) != null 
                    ? Long.parseLong(outputTokenMap.get(modelName).toString()) 
                    : 0L;

                ChatUsageToken existingToken = queryByModelName(modelName);
                if (existingToken != null) {
                    existingToken.setInputToken(inputToken);
                    existingToken.setOutputToken(outputToken);
                    existingToken.setUpdateTime(now);
                    baseMapper.updateById(existingToken);
                } else {
                    ChatUsageToken newToken = new ChatUsageToken();
                    newToken.setModelName(modelName);
                    newToken.setInputToken(inputToken);
                    newToken.setOutputToken(outputToken);
                    newToken.setUpdateTime(now);
                    baseMapper.insert(newToken);
                }
                syncCount++;
            }

            log.info("成功同步 {} 条Token使用记录到数据库", syncCount);
        } catch (Exception e) {
            log.error("同步Redis Token使用量到数据库失败", e);
        }
    }

    /**
     * 根据模型名称查询使用量
     */
    public ChatUsageToken queryByModelName(String modelName) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<ChatUsageToken>()
                .eq(ChatUsageToken::getModelName, modelName),
            false
        );
    }

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
        lqw.like(StringUtils.isNotBlank(bo.getModelName()), ChatUsageToken::getModelName, bo.getModelName());
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
