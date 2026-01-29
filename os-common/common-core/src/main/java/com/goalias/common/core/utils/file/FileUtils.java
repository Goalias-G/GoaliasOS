package com.goalias.common.core.utils.file;

import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件处理工具类
 *
 * @author Goalias
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FileUtils extends FileUtil {

    private static final String FILE_EXTENTION_SPLIT = ".";

    /**
     * 下载文件名重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) {
        String percentEncodedFileName = percentEncode(realFileName);
        String contentDispositionValue = "attachment; filename=%s;filename*=utf-8''%s".formatted(percentEncodedFileName, percentEncodedFileName);
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue);
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 检查文件扩展名是否符合要求
     *
     * @param file
     * @return
     */
    public static boolean isValidFileExtention(MultipartFile file, String[] ALLOWED_EXTENSIONS) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        final String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename) || !filename.contains(FILE_EXTENTION_SPLIT)) {
            return false;
        }
        // 获取文件后缀
        String fileExtension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        return Arrays.asList(ALLOWED_EXTENSIONS).contains(fileExtension);
    }

    /**
     * 获取安全的文件路径
     *
     * @param originalFilename 原始文件名
     * @param secureFilePath   安全路径
     * @return 安全文件路径
     */
    public static String getSecureFilePathForUpload(final String originalFilename, final String secureFilePath) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf(FILE_EXTENTION_SPLIT));
        String newFileName = UUID.randomUUID() + extension;

        return secureFilePath + newFileName; // 预定义安全路径
    }

    public static File convertMultiPartToFile(MultipartFile multipartFile) {
        File file = null;
        try {
            // 获取原始文件名
            String originalFileName = multipartFile.getOriginalFilename();
            // 默认扩展名
            String extension = ".tmp";
            // 尝试从原始文件名中获取扩展名
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // 使用原始文件的扩展名创建临时文件
            Path tempFile = Files.createTempFile(null, extension);
            file = tempFile.toFile();

            // 将MultipartFile的内容写入文件
            try (InputStream inputStream = multipartFile.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                // 处理文件写入异常
                log.error("文件写入异常", e);
            }
        } catch (IOException e) {
            // 处理临时文件创建异常
            log.error("临时文件创建异常", e);
        }
        return file;
    }
}
