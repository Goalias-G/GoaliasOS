package com.goalias.knowledge.chain.split;

import com.goalias.common.core.utils.StringUtils;
import com.goalias.knowledge.config.SplitterConfigProperties;
import com.goalias.knowledge.domain.KnowledgeInfo;
import com.goalias.knowledge.service.IKnowledgeInfoService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 字符文本分词器
 * 通用的文本分词器，支持自定义分隔符和滑动窗口切分
 * @author Goalias
 */
@Component
@Slf4j
@Primary
@RequiredArgsConstructor
public class CharacterTextSplitter implements TextSplitter {

    @Lazy
    @Resource
    private IKnowledgeInfoService knowledgeInfoService;

    private final SplitterConfigProperties splitterConfigProperties;

    /**
     * 对文本内容进行分词
     * 
     * @param content 文本内容
     * @param kid 知识库 ID
     * @return 分词后的文本块列表
     */
    @Override
    public List<String> split(String content, String kid) {
        if (StringUtils.isBlank(content)) {
            log.warn("[CharacterTextSplitter] 文本内容为空，返回空列表");
            return new ArrayList<>();
        }

        // 获取配置（优先使用数据库配置，其次使用配置文件，最后使用默认值）
        SplitConfig config = getSplitConfig(kid);
        
        log.debug("[CharacterTextSplitter] 使用配置 - 块大小: {}, 重叠: {}, 分隔符: '{}'", 
                config.textBlockSize, config.overlapChar, config.knowledgeSeparator);

        List<String> chunkList = new ArrayList<>();

        // 如果内容包含自定义分隔符，优先按分隔符切分
        if (StringUtils.isNotBlank(config.knowledgeSeparator) 
                && content.contains(config.knowledgeSeparator)) {
            log.debug("[CharacterTextSplitter] 使用自定义分隔符切分");
            String[] chunks = content.split(config.knowledgeSeparator);
            chunkList.addAll(Arrays.asList(chunks));
        } else {
            log.debug("[CharacterTextSplitter] 使用滑动窗口切分");
            chunkList = splitByWindow(content, config);
        }

        log.info("[CharacterTextSplitter] 切分完成，共 {} 个文本块", chunkList.size());
        return chunkList;
    }

    /**
     * 获取分词配置
     * 优先级：数据库配置 > 配置文件 > 默认值
     */
    private SplitConfig getSplitConfig(String kid) {
        SplitConfig config = new SplitConfig();
        
        // 从配置文件获取默认值
        SplitterConfigProperties.DefaultConfig defaultConfig = splitterConfigProperties.getDefaultConfig();
        config.textBlockSize = defaultConfig.getTextBlockSize();
        config.overlapChar = defaultConfig.getOverlapChar();
        config.knowledgeSeparator = defaultConfig.getKnowledgeSeparator();
        config.questionSeparator = defaultConfig.getQuestionSeparator();
        config.enableSmartBoundary = defaultConfig.getEnableSmartBoundary();
        config.sentenceEndMarkers = defaultConfig.getSentenceEndMarkers();
        config.smartBoundaryRange = defaultConfig.getSmartBoundaryRange();

        // 尝试从数据库获取配置（如果存在则覆盖）
        try {
            if (StringUtils.isNotBlank(kid)) {
                KnowledgeInfo knowledgeInfo = knowledgeInfoService.queryById(Long.valueOf(kid));
                if (knowledgeInfo != null) {
                    // 数据库配置覆盖默认配置
                    if (Objects.nonNull(knowledgeInfo.getTextBlockSize())) {
                        config.textBlockSize = Math.toIntExact(knowledgeInfo.getTextBlockSize());
                    }
                    if (knowledgeInfo.getOverlapChar() != null) {
                        config.overlapChar = Math.toIntExact(knowledgeInfo.getOverlapChar());
                    }
                    if (StringUtils.isNotBlank(knowledgeInfo.getKnowledgeSeparator())) {
                        config.knowledgeSeparator = knowledgeInfo.getKnowledgeSeparator();
                    }
                    if (StringUtils.isNotBlank(knowledgeInfo.getQuestionSeparator())) {
                        config.questionSeparator = knowledgeInfo.getQuestionSeparator();
                    }
                    log.debug("[CharacterTextSplitter] 使用数据库配置覆盖默认值");
                }
            }
        } catch (Exception e) {
            log.warn("[CharacterTextSplitter] 获取数据库配置失败，使用默认配置: {}", e.getMessage());
        }

        return config;
    }

    /**
     * 使用滑动窗口切分文本
     * 支持智能边界切分，在句子边界处切分以保持语义完整性
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

            // 如果启用了智能边界切分，尝试在句子边界处切分
            if (config.enableSmartBoundary && end < len) {
                int smartEnd = findSmartBoundary(content, end, config);
                if (smartEnd > begin && smartEnd < len) {
                    end = smartEnd;
                    log.debug("[CharacterTextSplitter] 使用智能边界切分，调整结束位置: {} -> {}", 
                            config.textBlockSize * (i + 1) + config.overlapChar, end);
                }
            }

            String chunk = content.substring(begin, end);
            chunkList.add(chunk);
            
            i++;
            right = i * config.textBlockSize;
        }

        return chunkList;
    }

    /**
     * 查找智能切分边界
     * 在目标位置附近寻找最近的句子结束标记
     * 
     * @param content 文本内容
     * @param targetPos 目标切分位置
     * @param config 切分配置
     * @return 调整后的切分位置
     */
    private int findSmartBoundary(String content, int targetPos, SplitConfig config) {
        int searchStart = Math.max(0, targetPos - config.smartBoundaryRange);
        int searchEnd = Math.min(content.length(), targetPos + config.smartBoundaryRange);
        
        // 在搜索范围内查找句子结束标记
        int bestPos = targetPos;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = searchStart; i < searchEnd; i++) {
            char c = content.charAt(i);
            if (config.sentenceEndMarkers.indexOf(c) >= 0) {
                int distance = Math.abs(i - targetPos);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestPos = i + 1; // 在标记后切分
                }
            }
        }
        
        // 如果找到了合适的边界，返回调整后的位置
        if (bestPos != targetPos) {
            log.debug("[CharacterTextSplitter] 找到智能边界，距离目标位置: {} 字符", minDistance);
            return bestPos;
        }
        
        return targetPos;
    }

    /**
     * 分词配置内部类
     */
    private static class SplitConfig {
        int textBlockSize;
        int overlapChar;
        String knowledgeSeparator;
        String questionSeparator;
        boolean enableSmartBoundary;
        String sentenceEndMarkers;
        int smartBoundaryRange;
    }
}
