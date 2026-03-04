package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.ChatMessage;
import com.goalias.chat.domain.bo.ChatMessageBo;
import com.goalias.chat.service.IChatMessageService;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.annotation.RepeatSubmit;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/message")
public class ChatMessageController extends BaseController {

    private final IChatMessageService chatMessageService;

    /**
     * 查询聊天消息列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatMessage> list(ChatMessageBo bo, PageQuery pageQuery) {
        return chatMessageService.queryPageList(bo, pageQuery);
    }

    /**
     * 根据会话ID查询聊天消息列表
     */
    @GetMapping("/listBySession/{sessionId}")
    public TableDataInfo<ChatMessage> listBySession(@NotNull(message = "会话ID不能为空")
                                                      @PathVariable Long sessionId,
                                                    PageQuery pageQuery) {
        ChatMessageBo bo = new ChatMessageBo();
        bo.setSessionId(sessionId);
        return chatMessageService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取聊天消息详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<ChatMessage> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatMessageService.queryById(id));
    }


    /**
     * 新增聊天消息
     */
    @RepeatSubmit()
    @PostMapping()
    public R<Long> add(@Validated(AddGroup.class) @RequestBody ChatMessageBo bo) {
        chatMessageService.insertByBo(bo);
        return R.ok(bo.getId());
    }

    /**
     * 修改聊天消息
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatMessageBo bo) {
        return toAjax(chatMessageService.updateByBo(bo));
    }

    /**
     * 删除聊天消息
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatMessageService.deleteWithValidByIds(List.of(ids), true));
    }
}
