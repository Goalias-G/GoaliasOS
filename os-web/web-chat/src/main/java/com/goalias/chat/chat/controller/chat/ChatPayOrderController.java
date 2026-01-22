package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.ChatPayOrder;
import com.goalias.chat.domain.bo.ChatPayOrderBo;
import com.goalias.chat.service.IChatPayOrderService;
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
 * 支付订单
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/payOrder")
public class ChatPayOrderController extends BaseController {

    private final IChatPayOrderService chatPayOrderService;

    /**
     * 查询支付订单列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatPayOrder> list(ChatPayOrderBo bo, PageQuery pageQuery) {
        return chatPayOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取支付订单详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<ChatPayOrder> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatPayOrderService.queryById(id));
    }

    /**
     * 新增支付订单
     */
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChatPayOrderBo bo) {
        return toAjax(chatPayOrderService.insertByBo(bo));
    }

    /**
     * 修改支付订单
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatPayOrderBo bo) {
        return toAjax(chatPayOrderService.updateByBo(bo));
    }

    /**
     * 删除支付订单
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatPayOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
