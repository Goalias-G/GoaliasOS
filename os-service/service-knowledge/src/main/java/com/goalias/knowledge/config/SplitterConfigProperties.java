package com.goalias.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文本分词器配置属性
 * @author Goalias
 */
@Data
@Component
@ConfigurationProperties(prefix = "splitter")
public class SplitterConfigProperties {

    /**
     * 默认分词配置
     */
    private DefaultConfig defaultConfig = new DefaultConfig();

    /**
     * 代码分词配置
     */
    private CodeConfig codeConfig = new CodeConfig();

    /**
     * Excel 分词配置
     */
    private ExcelConfig excelConfig = new ExcelConfig();

    /**
     * Markdown 分词配置
     */
    private MarkdownConfig markdownConfig = new MarkdownConfig();

    /**
     * 默认分词配置
     */
    @Data
    public static class DefaultConfig {
        /**
         * 文本块大小（字符数）
         * 默认值：500
         */
        private Integer textBlockSize = 500;

        /**
         * 重叠字符数
         * 默认值：50
         */
        private Integer overlapChar = 50;

        /**
         * 知识分隔符
         * 默认值：双换行符
         */
        private String knowledgeSeparator = "。";

        /**
         * 提问分隔符
         * 默认值：问号
         */
        private String questionSeparator = "?";

        /**
         * 是否启用智能边界切分
         * true: 尝试在句子边界切分，保持句子完整性
         * false: 严格按字符数切分
         * 默认值：true
         */
        private Boolean enableSmartBoundary = true;

        /**
         * 句子结束标记
         * 用于识别句子边界，在这些标记处优先切分
         * 默认值：句号、问号、感叹号、换行符
         */
        private String sentenceEndMarkers = "。？！\n.?!";

        /**
         * 智能边界搜索范围
         * 在目标切分位置前后多少字符范围内搜索句子边界
         * 默认值：100（即在目标位置前后100字符内寻找最近的句子边界）
         */
        private Integer smartBoundaryRange = 100;
    }

    /**
     * 代码分词配置
     * 支持多种编程语言的代码结构识别
     */
    @Data
    public static class CodeConfig {
        /**
         * 代码块大小（字符数）
         * 默认值：1000（代码通常需要更大的上下文）
         */
        private Integer textBlockSize = 1000;

        /**
         * 重叠字符数
         * 默认值：100
         */
        private Integer overlapChar = 100;

        /**
         * 是否保留代码结构（类、方法、函数边界）
         * 默认值：true
         */
        private Boolean preserveStructure = true;

        /**
         * 编程语言类型
         * 支持：java, python, javascript, shell, auto
         * auto: 自动检测（根据文件扩展名）
         * 默认值：auto
         */
        private String languageType = "auto";

        /**
         * Java 方法分隔符正则表达式
         * 用于识别 Java 方法边界
         */
        private String javaMethodRegex = "(public|private|protected)\\s+(static\\s+)?\\w+\\s+\\w+\\s*\\(";

        /**
         * Java 类分隔符正则表达式
         * 用于识别 Java 类、接口、枚举声明
         */
        private String javaClassRegex = "(public|private|protected)?\\s*(static\\s+)?(class|interface|enum)\\s+\\w+";

        /**
         * Python 函数/类分隔符正则表达式
         * 用于识别 Python 的 def 和 class
         */
        private String pythonFunctionRegex = "^(def|class)\\s+\\w+";

        /**
         * JavaScript/TypeScript 函数分隔符正则表达式
         * 用于识别函数声明、箭头函数、class
         */
        private String jsFunctionRegex = "(function\\s+\\w+|const\\s+\\w+\\s*=|class\\s+\\w+)";

        /**
         * Shell 函数分隔符正则表达式
         * 用于识别 Shell 函数定义
         */
        private String shellFunctionRegex = "^(function\\s+\\w+|\\w+\\s*\\(\\s*\\))";

        /**
         * 通用代码块分隔符
         * 当无法识别特定语言时使用，基于空行
         */
        private String genericCodeBlockSeparator = "\n\n";
    }

    @Data
    public static class ExcelConfig {
        /**
         * Excel 文本块大小（字符数）
         * 默认值：1000（Excel 通常包含大量表格数据）
         */
        private Integer textBlockSize = 1000;

        /**
         * 重叠字符数
         * 默认值：500
         */
        private Integer overlapChar = 500;

        /**
         * Excel 分隔符
         * 默认值：# （用于分隔不同的工作表或数据块）
         */
        private String knowledgeSeparator = "#";
    }

    /**
     * Markdown 分词配置
     */
    @Data
    public static class MarkdownConfig {
        /**
         * Markdown 文本块大小（字符数）
         * 默认值：800（Markdown 文档通常需要保留段落完整性）
         */
        private Integer textBlockSize = 800;

        /**
         * 重叠字符数
         * 默认值：80
         */
        private Integer overlapChar = 80;

        /**
         * 是否保留 Markdown 结构（标题、代码块边界）
         * 默认值：true
         */
        private Boolean preserveStructure = true;

        /**
         * 一级标题分隔符正则表达式
         * 用于识别 # 标题
         */
        private String h1SeparatorRegex = "^#\\s+.+$";

        /**
         * 二级标题分隔符正则表达式
         * 用于识别 ## 标题
         */
        private String h2SeparatorRegex = "^##\\s+.+$";

        /**
         * 三级标题分隔符正则表达式
         * 用于识别 ### 标题
         */
        private String h3SeparatorRegex = "^###\\s+.+$";

        /**
         * 代码块分隔符
         * 用于识别代码块边界
         */
        private String codeBlockSeparator = "```";

        /**
         * 分隔线标记
         * 用于识别 Markdown 分隔线
         */
        private String horizontalRuleSeparator = "---";
    }
}



