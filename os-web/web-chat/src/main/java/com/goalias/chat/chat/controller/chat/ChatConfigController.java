package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.ChatConfig;
import com.goalias.chat.domain.bo.ChatConfigBo;
import com.goalias.chat.service.IChatConfigService;
import com.goalias.common.core.domain.R;
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
 * 配置信息
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/config")
public class ChatConfigController extends BaseController {

    private final IChatConfigService chatConfigService;


    private final IChatConfigService configService;

    /**
     * 查询配置信息列表
     */
    @GetMapping("/list")
    public TableDataInfo<ChatConfig> list(ChatConfigBo bo, PageQuery pageQuery) {
        return chatConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取配置信息详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<ChatConfig> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(chatConfigService.queryById(id));
    }

    /**
     * 新增配置信息
     */
    @RepeatSubmit()
    @PostMapping("/saveOrUpdate")
    public R<Void> saveOrUpdate(@RequestBody List<ChatConfigBo> boList) {
        for (ChatConfigBo chatConfigBo : boList) {
            if(chatConfigBo.getId() == null){
                chatConfigService.insertByBo(chatConfigBo);
            }else {
                chatConfigService.updateByBo(chatConfigBo);
            }
        }
        return toAjax(true);
    }

    /**
     * 修改配置信息
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChatConfigBo bo) {
        return toAjax(chatConfigService.updateByBo(bo));
    }

    /**
     * 删除配置信息
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(chatConfigService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 根据参数键名查询系统参数值
     *
     * @param configKey 参数Key
     */
    @GetMapping(value = "/configKey/{configKey}")
    public R<String> getConfigKey(@PathVariable String configKey) {
        return R.ok(configService.getConfigValue("sys",configKey));
    }

    /**
     * 查询系统参数
     *
     */
    @GetMapping(value = "/sysConfigKey")
    public R<List<ChatConfig>> getSysConfigKey() {
        return R.ok(chatConfigService.getSysConfigValue("sys"));
    }

}
