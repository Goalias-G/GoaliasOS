package com.goalias.chat.chat.tools;

import com.goalias.chat.chat.support.TtlTokenContext;
import com.goalias.common.core.utils.StringUtils;
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

    @OsTool(name = "record_life_event", description = "用户指出需要记录生活事件时、用户输入中识别到生活事件时调用。" +
            "同一条用户消息只调用一次：先 select_scene_category，再本工具；成功后禁止再次调用本工具或重新查询分类，直接用自然语言向用户确认即可。" +
            "有匹配分类则传 sceneId，无则传 categoryName 新建。若用户消息含[附件OSS文件ID]，必须原样传入 fileIds。")
    public Map<String, Object> recordLifeEvent(@OsToolParam(name = "sceneId", description = "现已存在与事件匹配的场景id") Long sceneId,
                                @OsToolParam(name = "categoryName", description = "需新创建的场景分类名称") String categoryName,
                                @OsToolParam(name = "title", description = "生活事件标题", required = true) String title,
                                @OsToolParam(name = "content", description = "生活事件内容", required = true) String content,
                                @OsToolParam(name = "recordDate", description = "此生活记录对应时间节点(yyyy-MM-dd)") String recordDate,
                                @OsToolParam(name = "fileIds", description = "附件OSS文件ID，多个英文逗号分隔；来自用户消息中的[附件OSS文件ID]") String fileIds) {
        Long categoryId = null;
        Long userId = TtlTokenContext.getCurrentUserId();
        log.info("recordLifeEvent 执行 用户id:{},场景id：{},场景名称：{},fileIds：{}", userId, sceneId, categoryName, fileIds);

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
        if (StringUtils.isNotBlank(fileIds)) {
            record.setAttachsId(fileIds.trim());
        }
        record.setRemark("OS AI 自动记录");
        boolean ok = Boolean.TRUE.equals(recordService.insertByBo(record));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", ok);
        result.put("title", title);
        result.put("categoryId", Optional.ofNullable(sceneId).orElse(categoryId));
        result.put("message", ok
                ? "生活记录已成功写入"
                : "生活记录写入失败");
        return result;
    }
}
