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
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 文本分词器
 * @author Goalias
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MarkdownTextSplitter implements TextSplitter {

    @Lazy
    @Resource
    private IKnowledgeInfoService knowledgeInfoService;
    
    private final SplitterConfigProperties splitterConfigProperties;

    private final CharacterTextSplitter characterTextSplitter;

    /**
     * 对 Markdown 内容进行分词
     * 
     * @param content Markdown 内容
     * @param kid 知识库 ID
     * @return 分词后的文本块列表
     */
    @Override
    public List<String> split(String content, String kid) {
        if (StringUtils.isBlank(content)) {
            log.warn("[MarkdownTextSplitter] Markdown 内容为空，返回空列表");
            return new ArrayList<>();
        }

        // 获取配置（优先使用数据库配置，其次使用配置文件，最后使用默认值）
        SplitConfig config = getSplitConfig(kid);
        
        log.debug("[MarkdownTextSplitter] 使用配置 - 块大小: {}, 重叠: {}, 保留结构: {}", 
                config.textBlockSize, config.overlapChar, config.preserveStructure);

        // 如果启用了结构保留，尝试按 Markdown 结构切分
        if (config.preserveStructure) {
            List<String> structuredChunks = splitByMarkdownStructure(content, config);
            if (!structuredChunks.isEmpty()) {
                log.info("[MarkdownTextSplitter] 按 Markdown 结构切分成功，共 {} 个块", structuredChunks.size());
                return structuredChunks;
            }
            log.debug("[MarkdownTextSplitter] Markdown 结构切分失败，回退到字符切分");
        }

        // 回退到基于字符的切分
        return characterTextSplitter.split(content, kid);
    }

    /**
     * 获取分词配置
     * 优先级：数据库配置 > 配置文件 > 默认值
     */
    private SplitConfig getSplitConfig(String kid) {
        SplitConfig config = new SplitConfig();
        
        // 从配置文件获取默认值
        SplitterConfigProperties.MarkdownConfig markdownConfig = splitterConfigProperties.getMarkdownConfig();
        config.textBlockSize = markdownConfig.getTextBlockSize();
        config.overlapChar = markdownConfig.getOverlapChar();
        config.preserveStructure = markdownConfig.getPreserveStructure();
        config.h1SeparatorRegex = markdownConfig.getH1SeparatorRegex();
        config.h2SeparatorRegex = markdownConfig.getH2SeparatorRegex();
        config.h3SeparatorRegex = markdownConfig.getH3SeparatorRegex();
        config.codeBlockSeparator = markdownConfig.getCodeBlockSeparator();
        config.horizontalRuleSeparator = markdownConfig.getHorizontalRuleSeparator();

        // 尝试从数据库获取配置（如果存在则覆盖）
        try {
            if (StringUtils.isNotBlank(kid)) {
                KnowledgeInfo knowledgeInfo = knowledgeInfoService.queryById(Long.valueOf(kid));
                if (knowledgeInfo != null) {
                    if (Objects.nonNull(knowledgeInfo.getTextBlockSize())) {
                        config.textBlockSize = Math.toIntExact(knowledgeInfo.getTextBlockSize());
                    }
                    if (Objects.nonNull(knowledgeInfo.getOverlapChar())) {
                        config.overlapChar = Math.toIntExact(knowledgeInfo.getOverlapChar());
                    }
                    log.debug("[MarkdownTextSplitter] 使用数据库配置覆盖默认值");
                }
            }
        } catch (Exception e) {
            log.warn("[MarkdownTextSplitter] 获取数据库配置失败，使用默认配置: {}", e.getMessage());
        }

        return config;
    }

    /**
     * 按 Markdown 结构切分（标题、代码块、分隔线）
     */
    private List<String> splitByMarkdownStructure(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            // 首先按分隔线切分（最高优先级）
            if (content.contains(config.horizontalRuleSeparator)) {
                String[] sections = content.split(Pattern.quote(config.horizontalRuleSeparator));
                for (String section : sections) {
                    if (StringUtils.isNotBlank(section)) {
                        // 如果分隔线切分后的内容仍然过大，继续按标题切分
                        if (section.length() > config.textBlockSize) {
                            chunks.addAll(splitByHeaders(section, config));
                        } else {
                            chunks.add(section.trim());
                        }
                    }
                }
                return chunks;
            }
            
            // 如果没有分隔线，按标题切分
            return splitByHeaders(content, config);
            
        } catch (Exception e) {
            log.warn("[MarkdownTextSplitter] 按 Markdown 结构切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 按标题边界切分
     */
    private List<String> splitByHeaders(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            // 尝试按一级标题切分
            List<Integer> h1Positions = findHeaderPositions(content, config.h1SeparatorRegex);
            
            if (h1Positions.size() > 1) {
                // 有多个一级标题，按一级标题切分
                for (int i = 0; i < h1Positions.size() - 1; i++) {
                    int start = h1Positions.get(i);
                    int end = h1Positions.get(i + 1);
                    String section = content.substring(start, end);
                    
                    if (section.length() > config.textBlockSize) {
                        // 如果一级标题下的内容过大，按二级标题切分
                        chunks.addAll(splitByH2Headers(section, config));
                    } else {
                        chunks.add(section.trim());
                    }
                }
                // 处理最后一个一级标题
                String lastSection = content.substring(h1Positions.get(h1Positions.size() - 1));
                if (lastSection.length() > config.textBlockSize) {
                    chunks.addAll(splitByH2Headers(lastSection, config));
                } else {
                    chunks.add(lastSection.trim());
                }
                return chunks;
            }
            
            // 如果没有足够的一级标题，尝试按二级标题切分
            return splitByH2Headers(content, config);
            
        } catch (Exception e) {
            log.warn("[MarkdownTextSplitter] 按标题边界切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 按二级标题切分
     */
    private List<String> splitByH2Headers(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            List<Integer> h2Positions = findHeaderPositions(content, config.h2SeparatorRegex);
            
            if (h2Positions.size() > 1) {
                for (int i = 0; i < h2Positions.size() - 1; i++) {
                    int start = h2Positions.get(i);
                    int end = h2Positions.get(i + 1);
                    String section = content.substring(start, end);
                    
                    if (section.length() > config.textBlockSize) {
                        // 如果二级标题下的内容过大，按三级标题切分
                        chunks.addAll(splitByH3Headers(section, config));
                    } else {
                        chunks.add(section.trim());
                    }
                }
                // 处理最后一个二级标题
                String lastSection = content.substring(h2Positions.get(h2Positions.size() - 1));
                if (lastSection.length() > config.textBlockSize) {
                    chunks.addAll(splitByH3Headers(lastSection, config));
                } else {
                    chunks.add(lastSection.trim());
                }
                return chunks;
            }
            
            // 如果没有足够的二级标题，尝试按三级标题切分
            return splitByH3Headers(content, config);
            
        } catch (Exception e) {
            log.warn("[MarkdownTextSplitter] 按二级标题切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 按三级标题切分
     */
    private List<String> splitByH3Headers(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            List<Integer> h3Positions = findHeaderPositions(content, config.h3SeparatorRegex);
            
            if (h3Positions.size() > 1) {
                for (int i = 0; i < h3Positions.size() - 1; i++) {
                    int start = h3Positions.get(i);
                    int end = h3Positions.get(i + 1);
                    String section = content.substring(start, end);
                    
                    if (section.length() > config.textBlockSize) {
                        // 如果三级标题下的内容仍然过大，按字符切分
                        chunks.addAll(splitByCharacter(section, config));
                    } else {
                        chunks.add(section.trim());
                    }
                }
                // 处理最后一个三级标题
                String lastSection = content.substring(h3Positions.get(h3Positions.size() - 1));
                if (lastSection.length() > config.textBlockSize) {
                    chunks.addAll(splitByCharacter(lastSection, config));
                } else {
                    chunks.add(lastSection.trim());
                }
                return chunks;
            }
            
            // 如果没有足够的三级标题，回退到字符切分
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.warn("[MarkdownTextSplitter] 按三级标题切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查找标题位置
     */
    private List<Integer> findHeaderPositions(String content, String headerRegex) {
        List<Integer> positions = new ArrayList<>();
        positions.add(0); // 添加起始位置
        
        Pattern pattern = Pattern.compile(headerRegex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        
        return positions;
    }

    /**
     * 按字符切分（回退方案）
     */
    private List<String> splitByCharacter(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        int len = content.length();
        int i = 0;
        
        while (i * config.textBlockSize < len) {
            int begin = i * config.textBlockSize - config.overlapChar;
            if (begin < 0) {
                begin = 0;
            }
            
            int end = (i + 1) * config.textBlockSize + config.overlapChar;
            if (end > len) {
                end = len;
            }
            
            String chunk = content.substring(begin, end);
            chunks.add(chunk);
            i++;
        }
        
        log.info("[MarkdownTextSplitter] 按字符切分完成，共 {} 个块", chunks.size());
        return chunks;
    }

    /**
     * 分词配置内部类
     */
    private static class SplitConfig {
        int textBlockSize;
        int overlapChar;
        boolean preserveStructure;
        String h1SeparatorRegex;
        String h2SeparatorRegex;
        String h3SeparatorRegex;
        String codeBlockSeparator;
        String horizontalRuleSeparator;
    }
}
