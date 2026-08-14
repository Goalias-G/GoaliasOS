package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.ChatSession;
import com.goalias.chat.domain.bo.ChatSessionBo;
import com.goalias.chat.service.IChatSessionService;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.web.annotation.RepeatSubmit;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 会话管理
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/session")
public class ChatSessionController extends BaseController {

    private final IChatSessionService chatSessionService;
    private final RedisService redisService;

    /**
     * 查询会话管理列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatSession> list(ChatSessionBo bo, PageQuery pageQuery) {
        if(!LoginHelper.isLogin()){
           // 如果用户没有登录,返回空会话列表
           return TableDataInfo.build();
        }
        // 默认查询当前用户会话
        bo.setUserId(LoginHelper.getUserId());
        if (bo.getArchiveStatus() == null) {
            bo.setArchiveStatus(0);
        }
        return chatSessionService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取会话管理详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<ChatSession> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatSessionService.queryById(id));
    }

    /**
     * 新增会话管理
     */
    @RepeatSubmit()
    @PostMapping()
    public R<Long> add(@Validated(AddGroup.class) @RequestBody ChatSessionBo bo) {
        chatSessionService.insertByBo(bo);
        // 返回会话id
        return R.ok(bo.getId());
    }

    /**
     * 修改会话管理
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatSessionBo bo) {
        return toAjax(chatSessionService.updateByBo(bo));
    }

    /**
     * 归档会话。
     */
    @PutMapping("/{id}/archive")
    public R<Void> archive(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(chatSessionService.updateArchiveStatus(id, 1));
    }

    /**
     * 取消归档会话。
     */
    @PutMapping("/{id}/unarchive")
    public R<Void> unarchive(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(chatSessionService.updateArchiveStatus(id, 0));
    }

    /**
     * 查询当前登录用户的 AI 用户画像（Redis Hash，只读）。
     */
    @GetMapping("/user-context")
    public R<Map<String, Object>> userContext() {
        if (!LoginHelper.isLogin()) {
            return R.ok(Collections.emptyMap());
        }
        return R.ok(redisService.hmGet(CacheNames.CHAT_USER_CONTEXT + LoginHelper.getUserId()));
    }

    /**
     * 删除会话管理
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatSessionService.deleteWithValidByIds(List.of(ids), true));
    }
}
