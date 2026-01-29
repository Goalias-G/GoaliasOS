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
 * 代码文本分词器
 * 支持多种编程语言的代码结构识别和智能切分
 * 
 * 支持的语言：
 * - Java: 类、方法、接口
 * - Python: 函数、类
 * - JavaScript: 函数、类、箭头函数
 * - Shell: 函数定义
 * 
 * 配置优先级：
 * 1. 数据库中的知识库配置（如果存在）
 * 2. application.yml 中的 splitter.code 配置
 * 3. 硬编码的默认值
 * 
 * @author Goalias
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CodeTextSplitter implements TextSplitter {

    @Lazy
    @Resource
    private IKnowledgeInfoService knowledgeInfoService;
    
    private final SplitterConfigProperties splitterConfigProperties;

    private final CharacterTextSplitter characterTextSplitter;

    /**
     * 对代码内容进行分词
     * 
     * @param content 代码内容
     * @param kid 知识库 ID
     * @return 分词后的代码块列表
     */
    @Override
    public List<String> split(String content, String kid) {
        if (StringUtils.isBlank(content)) {
            log.warn("[CodeTextSplitter] 代码内容为空，返回空列表");
            return new ArrayList<>();
        }

        // 获取配置（优先使用数据库配置，其次使用配置文件，最后使用默认值）
        SplitConfig config = getSplitConfig(kid);
        
        log.debug("[CodeTextSplitter] 使用配置 - 块大小: {}, 重叠: {}, 保留结构: {}, 语言: {}", 
                config.textBlockSize, config.overlapChar, config.preserveStructure, config.languageType);

        // 如果启用了结构保留，尝试按代码结构切分
        if (config.preserveStructure) {
            List<String> structuredChunks = splitByCodeStructure(content, config);
            if (!structuredChunks.isEmpty()) {
                log.info("[CodeTextSplitter] 按代码结构切分成功，共 {} 个块", structuredChunks.size());
                return structuredChunks;
            }
            log.debug("[CodeTextSplitter] 代码结构切分失败，回退到字符切分");
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
        SplitterConfigProperties.CodeConfig codeConfig = splitterConfigProperties.getCodeConfig();
        config.textBlockSize = codeConfig.getTextBlockSize();
        config.overlapChar = codeConfig.getOverlapChar();
        config.preserveStructure = codeConfig.getPreserveStructure();
        config.languageType = codeConfig.getLanguageType();
        
        // 加载各语言的正则表达式
        config.javaMethodRegex = codeConfig.getJavaMethodRegex();
        config.javaClassRegex = codeConfig.getJavaClassRegex();
        config.pythonFunctionRegex = codeConfig.getPythonFunctionRegex();
        config.jsFunctionRegex = codeConfig.getJsFunctionRegex();
        config.shellFunctionRegex = codeConfig.getShellFunctionRegex();
        config.genericCodeBlockSeparator = codeConfig.getGenericCodeBlockSeparator();

        // 尝试从数据库获取配置（如果存在则覆盖）
        try {
            if (StringUtils.isNotBlank(kid)) {
                KnowledgeInfo knowledgeInfo = knowledgeInfoService.queryByKid(kid);
                if (knowledgeInfo != null) {
                    if (Objects.nonNull(knowledgeInfo.getTextBlockSize())) {
                        config.textBlockSize = Math.toIntExact(knowledgeInfo.getTextBlockSize());
                    }
                    if (Objects.nonNull(knowledgeInfo.getOverlapChar())) {
                        config.overlapChar = Math.toIntExact(knowledgeInfo.getOverlapChar());
                    }
                    log.debug("[CodeTextSplitter] 使用数据库配置覆盖默认值");
                }
            }
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] 获取数据库配置失败，使用默认配置: {}", e.getMessage());
        }

        return config;
    }

    /**
     * 按代码结构切分
     * 根据语言类型选择对应的切分策略
     */
    private List<String> splitByCodeStructure(String content, SplitConfig config) {
        String language = config.languageType.toLowerCase();
        
        log.debug("[CodeTextSplitter] 使用语言类型: {}", language);
        
        return switch (language) {
            case "java" -> splitJavaCode(content, config);
            case "python" -> splitPythonCode(content, config);
            case "javascript", "js" -> splitJavaScriptCode(content, config);
            case "shell", "bash", "sh" -> splitShellCode(content, config);
            case "auto" -> splitByGenericStructure(content, config);
            default -> {
                log.warn("[CodeTextSplitter] 不支持的语言类型: {}，使用通用切分", language);
                yield splitByGenericStructure(content, config);
            }
        };
    }

    /**
     * 切分 Java 代码
     * 优先按类边界切分，类内容过大时按方法切分
     */
    private List<String> splitJavaCode(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            // 尝试按类边界切分
            Pattern classPattern = Pattern.compile(config.javaClassRegex, Pattern.MULTILINE);
            List<Integer> classPositions = findPatternPositions(content, classPattern);
            
            if (classPositions.size() > 2) { // 至少有一个类定义
                for (int i = 0; i < classPositions.size() - 1; i++) {
                    int start = classPositions.get(i);
                    int end = classPositions.get(i + 1);
                    String classContent = content.substring(start, end);
                    
                    // 如果类内容过大，进一步按方法切分
                    if (classContent.length() > config.textBlockSize) {
                        chunks.addAll(splitJavaMethods(classContent, config));
                    } else {
                        chunks.add(classContent.trim());
                    }
                }
                return chunks;
            }
            
            // 如果没有类定义，尝试按方法切分
            return splitJavaMethods(content, config);
            
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] Java 代码切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 按 Java 方法边界切分
     */
    private List<String> splitJavaMethods(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            Pattern methodPattern = Pattern.compile(config.javaMethodRegex, Pattern.MULTILINE);
            List<Integer> methodPositions = findPatternPositions(content, methodPattern);
            
            if (methodPositions.size() > 2) { // 至少有一个方法定义
                for (int i = 0; i < methodPositions.size() - 1; i++) {
                    int start = methodPositions.get(i);
                    int end = methodPositions.get(i + 1);
                    String methodContent = content.substring(start, end);
                    
                    // 如果方法内容仍然过大，按字符切分
                    if (methodContent.length() > config.textBlockSize) {
                        chunks.addAll(splitByCharacter(methodContent, config));
                    } else {
                        chunks.add(methodContent.trim());
                    }
                }
                return chunks;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] Java 方法切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 切分 Python 代码
     * 按函数和类定义切分
     */
    private List<String> splitPythonCode(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            Pattern functionPattern = Pattern.compile(config.pythonFunctionRegex, Pattern.MULTILINE);
            List<Integer> functionPositions = findPatternPositions(content, functionPattern);
            
            if (functionPositions.size() > 2) { // 至少有一个函数/类定义
                for (int i = 0; i < functionPositions.size() - 1; i++) {
                    int start = functionPositions.get(i);
                    int end = functionPositions.get(i + 1);
                    String functionContent = content.substring(start, end);
                    
                    // 如果函数内容过大，按字符切分
                    if (functionContent.length() > config.textBlockSize) {
                        chunks.addAll(splitByCharacter(functionContent, config));
                    } else {
                        chunks.add(functionContent.trim());
                    }
                }
                return chunks;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] Python 代码切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 切分 JavaScript 代码
     * 支持函数声明、箭头函数、类定义
     */
    private List<String> splitJavaScriptCode(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            Pattern functionPattern = Pattern.compile(config.jsFunctionRegex, Pattern.MULTILINE);
            List<Integer> functionPositions = findPatternPositions(content, functionPattern);
            
            if (functionPositions.size() > 2) { // 至少有一个函数/类定义
                for (int i = 0; i < functionPositions.size() - 1; i++) {
                    int start = functionPositions.get(i);
                    int end = functionPositions.get(i + 1);
                    String functionContent = content.substring(start, end);
                    
                    // 如果函数内容过大，按字符切分
                    if (functionContent.length() > config.textBlockSize) {
                        chunks.addAll(splitByCharacter(functionContent, config));
                    } else {
                        chunks.add(functionContent.trim());
                    }
                }
                return chunks;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] JavaScript 代码切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 切分 Shell 脚本
     * 按函数定义切分
     */
    private List<String> splitShellCode(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            Pattern functionPattern = Pattern.compile(config.shellFunctionRegex, Pattern.MULTILINE);
            List<Integer> functionPositions = findPatternPositions(content, functionPattern);
            
            if (functionPositions.size() > 2) { // 至少有一个函数定义
                for (int i = 0; i < functionPositions.size() - 1; i++) {
                    int start = functionPositions.get(i);
                    int end = functionPositions.get(i + 1);
                    String functionContent = content.substring(start, end);
                    
                    // 如果函数内容过大，按字符切分
                    if (functionContent.length() > config.textBlockSize) {
                        chunks.addAll(splitByCharacter(functionContent, config));
                    } else {
                        chunks.add(functionContent.trim());
                    }
                }
                return chunks;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] Shell 脚本切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 通用代码结构切分
     * 按空行切分（适用于无法识别的语言）
     */
    private List<String> splitByGenericStructure(String content, SplitConfig config) {
        List<String> chunks = new ArrayList<>();
        
        try {
            // 按双换行符（空行）切分
            String[] blocks = content.split(config.genericCodeBlockSeparator);
            
            for (String block : blocks) {
                if (StringUtils.isNotBlank(block)) {
                    if (block.length() > config.textBlockSize) {
                        // 如果块过大，按字符切分
                        chunks.addAll(splitByCharacter(block, config));
                    } else {
                        chunks.add(block.trim());
                    }
                }
            }
            
            return chunks;
        } catch (Exception e) {
            log.warn("[CodeTextSplitter] 通用代码切分失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查找正则表达式匹配的所有位置
     * 
     * @param content 文本内容
     * @param pattern 正则表达式模式
     * @return 匹配位置列表（包含起始和结束位置）
     */
    private List<Integer> findPatternPositions(String content, Pattern pattern) {
        List<Integer> positions = new ArrayList<>();
        positions.add(0); // 添加起始位置
        
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        
        positions.add(content.length()); // 添加结束位置
        return positions;
    }

    /**
     * 按字符切分（回退方案）
     * 使用滑动窗口，支持重叠
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
        
        log.debug("[CodeTextSplitter] 按字符切分完成，共 {} 个块", chunks.size());
        return chunks;
    }

    /**
     * 分词配置内部类
     */
    private static class SplitConfig {
        int textBlockSize;
        int overlapChar;
        boolean preserveStructure;
        String languageType;
        
        // 各语言的正则表达式
        String javaMethodRegex;
        String javaClassRegex;
        String pythonFunctionRegex;
        String jsFunctionRegex;
        String shellFunctionRegex;
        String genericCodeBlockSeparator;
    }
}
