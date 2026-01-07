package com.goalias.common.oss.core;

import com.goalias.common.oss.domain.dto.MultipartUploadInitDTO;
import com.goalias.common.oss.domain.dto.MultipartUploadMergeDTO;
import com.goalias.common.oss.domain.vo.ChunkUploadVO;
import com.goalias.common.oss.domain.vo.MultipartUploadInitVO;
import com.goalias.common.oss.domain.vo.MultipartUploadResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface IFileService {

    boolean isBucketExist(String bucketName);

    boolean isObjectExist(String bucketName, String objectName);

    String uploadFile(String bucketName, String objectName, InputStream file);

    String uploadFile(String bucketName, String objectName, MultipartFile file);

    String uploadLocalFile(String bucketName, String objectName, String filePath);

    void delFile(String bucketName, java.util.List<String> objectNames);

    String getUrl(String bucketName, String objectName);

    MultipartUploadInitVO initMultipartUpload(MultipartUploadInitDTO dto);

    ChunkUploadVO uploadChunk(String bucketName, String objectName, String uploadId,
                              Integer chunkNumber, MultipartFile file);

    MultipartUploadResultVO completeMultipartUpload(MultipartUploadMergeDTO dto);

    void abortMultipartUpload(String bucketName, String objectName, String uploadId);

    List<Integer> getUploadedChunks(String uploadId);

}
