package com.goalias.system.controller.system;


import cn.hutool.core.util.ObjectUtil;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.core.validate.QueryGroup;
import com.goalias.common.oss.core.MinioService;
import com.goalias.common.oss.domain.dto.MultipartUploadInitDTO;
import com.goalias.common.oss.domain.dto.MultipartUploadMergeDTO;
import com.goalias.common.oss.domain.vo.ChunkUploadVO;
import com.goalias.common.oss.domain.vo.UploadInitVO;
import com.goalias.common.oss.domain.vo.UploadResultVO;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysOss;
import com.goalias.system.domain.vo.SysOssUploadVo;
import com.goalias.system.service.ISysOssService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传 控制层
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/oss")
public class SysOssController extends BaseController {

    private final ISysOssService ossService;

    private final MinioService minioService;

    /**
     * 查询OSS对象存储列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysOss> list(@Validated(QueryGroup.class) SysOss sysOss, PageQuery pageQuery) {
        return ossService.queryPageList(sysOss, pageQuery);
    }

    /**
     * 查询OSS对象基于id串
     *
     * @param ossIds OSS对象ID串
     */
    @GetMapping("/listByIds/{ossIds}")
    public R<List<SysOss>> listByIds(@NotEmpty(message = "主键不能为空")
                                     @PathVariable Long[] ossIds) {
        List<SysOss> list = ossService.listByIds(Arrays.asList(ossIds));
        return R.ok(list);
    }

    /**
     * 上传OSS对象存储
     *
     * @param file 文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        SysOss oss = ossService.upload(file);
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId());
        return R.ok(uploadVo);
    }

    /**
     * OSS对象详细信息
     *
     * @param ossId OSS对象ID
     */
    @GetMapping("/info/{ossId}")
    public R<SysOss> info(@PathVariable Long ossId) {
        return R.ok(ossService.getById(ossId));
    }

    /**
     * 删除OSS对象存储
     *
     * @param ossIds OSS对象ID串
     */
    @DeleteMapping("/{ossIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossIds) {
        return toAjax(ossService.deleteWithValidByIds(List.of(ossIds), true));
    }


    // ==================== 分片上传接口 ====================

    /**
     * 初始化分片上传
     * 前端在开始分片上传前调用此接口，获取 uploadId 和预签名 URL 列表
     *
     * @param dto 初始化请求参数
     * @return 分片上传初始化信息（包含 uploadId、预签名 URL 列表等）
     */
    @PostMapping("/chunks/init")
    public R<UploadInitVO> initMultipartUpload(@Valid @RequestBody MultipartUploadInitDTO dto) {
        try {
            UploadInitVO result = minioService.initMultipartUpload(dto);
            return R.ok("初始化成功", result);
        } catch (Exception e) {
            return R.fail("初始化分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传单个分片
     *
     * @param objectName  文件对象名
     * @param uploadId    分片上传 ID
     * @param chunkNumber 分片编号（从 1 开始）
     * @param file        分片文件数据
     * @return 分片上传结果
     */
    @PostMapping("/chunks/upload")
    public R<ChunkUploadVO> uploadChunk(
            @RequestParam String objectName,
            @RequestParam String uploadId,
            @RequestParam Integer chunkNumber,
            @RequestParam MultipartFile file) {

        ChunkUploadVO result = minioService.uploadChunk(objectName, uploadId, chunkNumber, file);
        if (result.getSuccess()) {
            return R.ok("分片上传成功", result);
        }
        return R.fail("分片上传失败: " + result.getErrorMsg());
    }

    /**
     * 合并分片完成上传
     * 所有分片上传完成后调用此接口，合并成完整文件
     *
     * @param dto 合并请求参数
     * @return 上传结果（包含文件 URL、大小等信息）
     */
    @PostMapping("/chunks/complete")
    public R<UploadResultVO> completeMultipartUpload(@Valid @RequestBody MultipartUploadMergeDTO dto) {
        try {
            UploadResultVO result = minioService.completeMultipartUpload(dto);
            if (StringUtils.isNotBlank(result.getFileUrl())) {
                SysOss sysOss = new SysOss();
                sysOss.setUrl(result.getFileUrl());
                sysOss.setFileName(result.getObjectName());
                String[] nameArray = result.getObjectName().split("/");
                String originName = nameArray[nameArray.length - 1];
                sysOss.setOriginalName(originName);
                sysOss.setFileSuffix(MinioService.getExtension(originName));
                Long ossId = ossService.saveFile(sysOss);
                result.setOssId(ossId);
                return R.ok("文件上传完成", result);
            }
            return R.fail("合并分片失败");
        } catch (Exception e) {
            return R.fail("合并分片失败: " + e.getMessage());
        }
    }

    /**
     * 取消分片上传
     * 用于取消未完成的分片上传任务，释放服务器资源
     *
     * @param objectName 文件对象名
     * @param uploadId   分片上传 ID
     * @return 操作结果
     */
    @DeleteMapping("/chunks/abort")
    public R<Void> abortMultipartUpload(
            @RequestParam String objectName,
            @RequestParam String uploadId) {

        minioService.abortMultipartUpload(objectName, uploadId);
        return R.ok("已取消分片上传");
    }

    /**
     * 获取已上传的分片列表（用于断点续传）
     *
     * @param uploadId 分片上传 ID
     * @return 已上传的分片编号列表
     */
    @GetMapping("/chunks/uploaded")
    public R<List<Integer>> getUploadedChunks(@RequestParam String uploadId) {
        List<Integer> chunks = minioService.getUploadedChunks(uploadId);
        return R.ok(chunks);
    }

}
