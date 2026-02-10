package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.domain.bo.ChatModelBo;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.web.annotation.RepeatSubmit;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.goalias.chat.enums.DisplayType;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天模型
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/model")
public class ChatModelController extends BaseController {

    private final IChatModelService chatModelService;

    /**
     * 查询聊天模型列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatModel> list(ChatModelBo bo, PageQuery pageQuery) {
        return chatModelService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询用户模型列表
     */
    @GetMapping("/modelList")
    public R<List<ChatModel>> modelList(ChatModelBo bo) {
        bo.setModelShow(DisplayType.VISIBLE.getCode());
        return R.ok(chatModelService.queryList(bo));
    }

    /**
     * 获取聊天模型详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<ChatModel> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatModelService.queryById(id));
    }

    /**
     * 新增聊天模型
     */
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChatModelBo bo) {
        return toAjax(chatModelService.insertByBo(bo));
    }

    /**
     * 修改聊天模型
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatModelBo bo) {
        return toAjax(chatModelService.updateByBo(bo));
    }

    /**
     * 删除聊天模型
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatModelService.deleteWithValidByIds(List.of(ids), true));
    }
}
