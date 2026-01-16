package com.goalias.knowledge.domain.bo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Goalias
 */
@Data
public class KnowledgeInfoUploadBo {

    private String kid;

    private MultipartFile file;

}
