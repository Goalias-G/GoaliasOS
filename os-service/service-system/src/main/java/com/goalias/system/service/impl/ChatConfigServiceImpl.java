package com.goalias.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.ChatConfig;
import com.goalias.system.domain.bo.ChatConfigBo;
import com.goalias.system.mapper.ChatConfigMapper;
import com.goalias.system.service.IChatConfigService;
import lombok.RequiredArgsConstructor;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * 配置信息Service业务层处理
 *
 * @author Goalias
 * @date 2025-04-08
 */
@RequiredArgsConstructor
@Service
public class ChatConfigServiceImpl implements IChatConfigService {

    private final ChatConfigMapper baseMapper;

    /**
     * 根据配置类型和配置key获取值
     *
     * @param category 分类
     * @param configKey key名称
     * @return
     */
    @Override
    public String getConfigValue(String category,String configKey) {
        ChatConfigBo bo = new ChatConfigBo();
        bo.setCategory(category);
        bo.setConfigName(configKey);
        LambdaQueryWrapper<ChatConfig> lqw = buildQueryWrapper(bo);
        ChatConfig chatConfigVo = baseMapper.selectOne(lqw);
        return chatConfigVo.getConfigValue();
    }

    /**
     * 查询配置信息
     */
    @Override
    public ChatConfig queryById(Long id){
        return baseMapper.selectById(id);
    }

    /**
     * 查询配置信息列表
     */
    @Override
    public TableDataInfo<ChatConfig> queryPageList(ChatConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChatConfig> lqw = buildQueryWrapper(bo);
        Page<ChatConfig> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询配置信息列表
     */
    @Override
    public List<ChatConfig> queryList(ChatConfigBo bo) {
        LambdaQueryWrapper<ChatConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<ChatConfig> buildQueryWrapper(ChatConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChatConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), ChatConfig::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), ChatConfig::getConfigName, bo.getConfigName());
        lqw.eq(StringUtils.isNotBlank(bo.getConfigValue()), ChatConfig::getConfigValue, bo.getConfigValue());
        lqw.eq(StringUtils.isNotBlank(bo.getConfigDict()), ChatConfig::getConfigDict, bo.getConfigDict());
        return lqw;
    }

    /**
     * 新增配置信息
     */
    @Override
    public Boolean insertByBo(ChatConfigBo bo) {
        ChatConfig add = MapstructUtils.convert(bo, ChatConfig.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改配置信息
     */
    @Override
    public Boolean updateByBo(ChatConfigBo bo) {
        ChatConfig update = MapstructUtils.convert(bo, ChatConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChatConfig entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除配置信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 根据配置类型和配置key获取值
     *
     * @param category
     * @return
     */
    @Override
    public List<ChatConfig> getSysConfigValue(String category) {
        ChatConfigBo bo = new ChatConfigBo();
        bo.setCategory(category);
        LambdaQueryWrapper<ChatConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

}
