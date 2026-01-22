package com.goalias.knowledge.chain.split;

import com.goalias.common.core.utils.StringUtils;
import com.goalias.knowledge.config.SplitterConfigProperties;
import com.goalias.knowledge.domain.KnowledgeInfo;
import com.goalias.knowledge.service.IKnowledgeInfoService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Excel 文本分词器
 * 专门用于处理 Excel 文件转换后的文本内容
 * @author Goalias
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExcelTextSplitter implements TextSplitter {

    @Lazy
    @Resource
    private IKnowledgeInfoService knowledgeInfoService;
    
    private final SplitterConfigProperties splitterConfigProperties;

    private final CharacterTextSplitter characterTextSplitter;

    /**
     * 对 Excel 文本内容进行分词
     * 
     * @param content Excel 转换后的文本内容
     * @param kid 知识库 ID
     * @return 分词后的文本块列表
     */
    @Override
    public List<String> split(String content, String kid) {
        if (StringUtils.isBlank(content)) {
            log.warn("[ExcelTextSplitter] Excel 文本内容为空，返回空列表");
            return new ArrayList<>();
        }

        // 获取配置（优先使用数据库配置，其次使用配置文件，最后使用默认值）
        SplitConfig config = getSplitConfig(kid);
        
        log.debug("[ExcelTextSplitter] 使用配置 - 块大小: {}, 重叠: {}, 分隔符: '{}'", 
                config.textBlockSize, config.overlapChar, config.knowledgeSeparator);

        List<String> chunkList = new ArrayList<>();

        // 如果内容包含自定义分隔符，优先按分隔符切分
        if (StringUtils.isNotBlank(config.knowledgeSeparator) 
                && content.contains(config.knowledgeSeparator)) {
            log.debug("[ExcelTextSplitter] 使用自定义分隔符切分");
            String[] chunks = content.split(config.knowledgeSeparator);
            chunkList.addAll(Arrays.asList(chunks));
        } else {
            chunkList = characterTextSplitter.split(content, kid);
        }

        log.info("[ExcelTextSplitter] 切分完成，共 {} 个文本块", chunkList.size());
        return chunkList;
    }

    /**
     * 获取分词配置
     * 优先级：数据库配置 > 配置文件 > 默认值
     */
    private SplitConfig getSplitConfig(String kid) {
        SplitConfig config = new SplitConfig();
        
        // 从配置文件获取默认值
        SplitterConfigProperties.ExcelConfig excelConfig = splitterConfigProperties.getExcelConfig();
        config.textBlockSize = excelConfig.getTextBlockSize();
        config.overlapChar = excelConfig.getOverlapChar();
        config.knowledgeSeparator = excelConfig.getKnowledgeSeparator();

        // 尝试从数据库获取配置（如果存在则覆盖）
        try {
            if (StringUtils.isNotBlank(kid)) {
                KnowledgeInfo knowledgeInfo = knowledgeInfoService.queryById(Long.valueOf(kid));
                if (knowledgeInfo != null) {
                    // 数据库配置覆盖默认配置
                    if (Objects.nonNull(knowledgeInfo.getTextBlockSize())) {
                        config.textBlockSize = Math.toIntExact(knowledgeInfo.getTextBlockSize());
                    }
                    if (Objects.nonNull(knowledgeInfo.getOverlapChar())) {
                        config.overlapChar = Math.toIntExact(knowledgeInfo.getOverlapChar());
                    }
                    if (StringUtils.isNotBlank(knowledgeInfo.getKnowledgeSeparator())) {
                        config.knowledgeSeparator = knowledgeInfo.getKnowledgeSeparator();
                    }
                    log.debug("[ExcelTextSplitter] 使用数据库配置覆盖默认值");
                }
            }
        } catch (Exception e) {
            log.warn("[ExcelTextSplitter] 获取数据库配置失败，使用默认配置: {}", e.getMessage());
        }

        return config;
    }

    /**
     * 使用滑动窗口切分文本
     * 
     * @param content 文本内容
     * @param config 切分配置
     * @return 切分后的文本块列表
     */
    private List<String> splitByWindow(String content, SplitConfig config) {
        List<String> chunkList = new ArrayList<>();
        int len = content.length();
        int i = 0;
        int right = 0;

        while (right < len) {
            // 计算当前块的起始位置（考虑重叠）
            int begin = i * config.textBlockSize - config.overlapChar;
            if (begin < 0) {
                begin = 0;
            }

            // 计算当前块的结束位置（考虑重叠）
            int end = config.textBlockSize * (i + 1) + config.overlapChar;
            if (end > len) {
                end = len;
            }

            String chunk = content.substring(begin, end);
            chunkList.add(chunk);
            
            i++;
            right = i * config.textBlockSize;
        }

        return chunkList;
    }

    /**
     * 分词配置内部类
     */
    private static class SplitConfig {
        int textBlockSize;
        int overlapChar;
        String knowledgeSeparator;
    }
}
