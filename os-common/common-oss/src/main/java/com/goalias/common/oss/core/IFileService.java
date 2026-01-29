package com.goalias.common.oss.core;

import com.goalias.common.oss.domain.dto.MultipartUploadInitDTO;
import com.goalias.common.oss.domain.dto.MultipartUploadMergeDTO;
import com.goalias.common.oss.domain.vo.ChunkUploadVO;
import com.goalias.common.oss.domain.vo.UploadInitVO;
import com.goalias.common.oss.domain.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public interface IFileService {

    boolean isBucketExist(String bucketName);

    boolean isObjectExist(String bucketName, String objectName);

    String uploadFile(String objectName, InputStream file);

    String uploadFile(String objectName, MultipartFile file);

    String uploadLocalFile(String objectName, String filePath);

    void delFile(java.util.List<String> objectNames);

    String getUrl(String bucketName, String objectName);

    UploadInitVO initMultipartUpload(MultipartUploadInitDTO dto);

    ChunkUploadVO uploadChunk(String objectName, String uploadId,
                              Integer chunkNumber, MultipartFile file);

    UploadResultVO completeMultipartUpload(MultipartUploadMergeDTO dto);

    void abortMultipartUpload(String objectName, String uploadId);

    List<Integer> getUploadedChunks(String uploadId);


    /**
     * 获取时间层次的文件夹路径
     *
     * @return 如：/2026/01/07/
     */
    default String getTimeFilePath(Long userId, String fileName) {
        String timePath = new SimpleDateFormat("/yyyy/MM/dd").format(new Date()) + "/";
        return userId + timePath + fileName;
    }

}
