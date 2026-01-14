package com.goalias.system.service;

import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysOss;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 文件上传 服务层
 *
 * @author Goalias
 */
public interface ISysOssService {

  TableDataInfo<SysOss> queryPageList(SysOss sysOss, PageQuery pageQuery);

  List<SysOss> listByIds(Collection<Long> ossIds);

  SysOss getById(Long ossId);

  SysOss upload(MultipartFile file);

  Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

  /**
   * 根据文件路径删除文件
   *
   * @param filePath 文件路径
   * @return 是否删除成功
   */
  boolean deleteFile(String filePath);

  String selectUrlByIds(String ossIds);
}
