package com.goalias.chat.chat.controller.knowledge;

import cn.dev33.satoken.stp.StpUtil;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.knowledge.domain.KnowledgeAttach;
import com.goalias.knowledge.domain.KnowledgeFragment;
import com.goalias.knowledge.domain.KnowledgeInfo;
import com.goalias.knowledge.domain.bo.KnowledgeAttachBo;
import com.goalias.knowledge.domain.bo.KnowledgeFragmentBo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoBo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoUploadBo;
import com.goalias.knowledge.service.IKnowledgeAttachService;
import com.goalias.knowledge.service.IKnowledgeFragmentService;
import com.goalias.knowledge.service.IKnowledgeInfoService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 知识库管理
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController extends BaseController {

    private final IKnowledgeInfoService knowledgeInfoService;

    private final IKnowledgeAttachService attachService;

    private final IKnowledgeFragmentService fragmentService;

    /**
     * 根据用户信息查询本地知识库
     */
    @GetMapping("/list")
    public TableDataInfo<KnowledgeInfo> list(KnowledgeInfoBo bo, PageQuery pageQuery) {
        if (!StpUtil.isLogin()) {
            throw new SecurityException("请先去登录!");
        }
        if (!Objects.equals(LoginHelper.getUserId(), 1L)) {
            bo.setUid(LoginHelper.getUserId());
        }
        return knowledgeInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 新增知识库
     */
    @PostMapping("/save")
    public R<Void> save(@Validated(AddGroup.class) @RequestBody KnowledgeInfoBo bo) {
        knowledgeInfoService.saveOne(bo);
        return R.ok();
    }

    /**
     * 删除知识库
     */
    @PostMapping("/remove/{kid}")
    public R<String> remove(@PathVariable String kid) {
        knowledgeInfoService.removeKnowledge(kid);
        return R.ok("删除知识库成功!");
    }

    /**
     * 修改知识库
     */
    @PostMapping("/edit")
    public R<Void> edit(@RequestBody KnowledgeInfoBo bo) {
        return toAjax(knowledgeInfoService.updateByBo(bo));
    }


    /**
     * 查询知识附件信息
     */
    @GetMapping("/detail/{kid}")
    public TableDataInfo<KnowledgeAttach> attach(KnowledgeAttachBo bo, PageQuery pageQuery,
                                                 @PathVariable String kid) {
        bo.setKid(kid);
        return attachService.queryPageList(bo, pageQuery);
    }

    /**
     * 上传知识库附件
     */
    @PostMapping(value = "/attach/upload")
    public R<String> upload(KnowledgeInfoUploadBo bo) throws Exception {
        knowledgeInfoService.upload(bo);
        return R.ok("上传知识库附件成功!");
    }

    /**
     * 获取知识库附件详细信息
     *
     * @param id 主键
     */
    @GetMapping("attach/info/{id}")
    public R<KnowledgeAttach> getAttachInfo(@NotNull(message = "主键不能为空")
                                              @PathVariable Long id) {
        return R.ok(attachService.queryById(id));
    }

    /**
     * 删除知识库附件
     */
    @PostMapping("attach/remove/{kid}")
    public R<Void> removeAttach(@NotEmpty(message = "主键不能为空")
                                @PathVariable String kid) {
        attachService.removeKnowledgeAttach(kid);
        return R.ok();
    }


    /**
     * 查询知识片段
     */
    @GetMapping("/fragment/list/{docId}")
    public TableDataInfo<KnowledgeFragment> fragmentList(KnowledgeFragmentBo bo,
                                                         PageQuery pageQuery, @PathVariable String docId) {
        bo.setDocId(docId);
        return fragmentService.queryPageList(bo, pageQuery);
    }

    /**
     * 上传文件翻译
     */
    @PostMapping("/translationByFile")
    @ResponseBody
    public String translationByFile(@RequestParam("file") MultipartFile file, String targetLanguage) {
        return attachService.translationByFile(file, targetLanguage);
    }

}
