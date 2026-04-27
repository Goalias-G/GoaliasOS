package com.goalias.chat.chat.tools;

import com.goalias.chat.chat.support.TtlTokenContext;
import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.bo.LifeCategoryBo;
import com.goalias.system.domain.bo.LifeRecordBo;
import com.goalias.system.service.ILifeCategoryService;
import com.goalias.system.service.ILifeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LifeTool implements OsToolProvider {

    private final ILifeCategoryService categoryService;
    private final ILifeRecordService recordService;

    @OsTool(name = "select_scene_category", description = "查询当前用户的生活场景分类的id与name")
    public Map<Long, String> lifeCategoryList() {
        LifeCategoryBo bo = new LifeCategoryBo();
        bo.setUserId(TtlTokenContext.getCurrentUserId());
        List<LifeCategory> list = categoryService.queryList(bo);
        return list.stream().collect(Collectors.toMap(LifeCategory::getId, LifeCategory::getName));
    }

    @OsTool(name = "record_life_event", description = "用户指出需要记录生活事件时、用户输入中识别到生活事件时调用。执行前需要先查询目前的场景分类 select_scene_category，" +
            "判断事件是否匹配已有分类列表，有则选对应场景id,没有则创建新对应的场景分类名称，同时生成记录。")
    public Boolean recordLifeEvent(@OsToolParam(name = "sceneId", description = "现已存在与事件匹配的场景id") Long sceneId,
                                @OsToolParam(name = "categoryName", description = "需新创建的场景分类名称") String categoryName,
                                @OsToolParam(name = "title", description = "生活事件标题", required = true) String title,
                                @OsToolParam(name = "content", description = "生活事件内容", required = true) String content,
                                @OsToolParam(name = "recordDate", description = "此生活记录对应时间节点(yyyy-MM-dd)") String recordDate) {
        Long categoryId = null;
        Long userId = TtlTokenContext.getCurrentUserId();
        log.info("recordLifeEvent 执行 用户id:{},场景id：{},场景名称：{}", userId, sceneId, categoryName);

        if (Objects.isNull(sceneId)) {
            LifeCategoryBo bo = new LifeCategoryBo();
            bo.setUserId(userId);
            bo.setName(Optional.ofNullable(categoryName).orElse("默认场景"));
            categoryId = categoryService.insertByBo(bo);
        }
        LifeRecordBo record = new LifeRecordBo();
        record.setUserId(userId);
        record.setCategoryId(Optional.ofNullable(sceneId).orElse(categoryId));
        record.setTitle(title);
        record.setContent(content);
        Date date;
        try {
            date = DateUtils.parseDate(recordDate, "yyyy-MM-dd");
        } catch (ParseException e) {
            log.error("时间格式错误", e);
            date = new Date();
        }
        record.setRecordDate(date);
        record.setRemark("OS AI 自动记录");
        return recordService.insertByBo(record);
    }
}
