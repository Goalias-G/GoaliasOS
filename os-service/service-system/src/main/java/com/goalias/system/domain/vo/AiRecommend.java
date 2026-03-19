package com.goalias.system.domain.vo;

import lombok.Data;

@Data
public class AiRecommend {

    private String greeting;

    private KnowledgeInfo psychology;

    private KnowledgeInfo knowledge;

    private String lifeAnalysis;


    @Data
    public static class KnowledgeInfo {
        private String title;
        private String content;
    }

}
