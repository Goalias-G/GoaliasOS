package com.goalias.knowledge.chain.loader;

import com.goalias.knowledge.chain.split.TextSplitter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Markdown 文件加载器
 * 用于读取 Markdown 文件内容
 * 
 * @author Goalias
 */
@Component
@AllArgsConstructor
@Slf4j
public class MarkDownFileLoader implements ResourceLoader {
    
    private final TextSplitter textSplitter;

    /**
     * 从输入流中读取 Markdown 内容
     * 
     * @param inputStream Markdown 文件输入流
     * @return Markdown 文本内容
     */
    @Override
    public String getContent(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            log.debug("[MarkdownFileLoader] 成功读取 Markdown 内容，长度: {}", content.length());
            return content.toString();
            
        } catch (IOException e) {
            log.error("[MarkdownFileLoader] 读取 Markdown 文件失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 将 Markdown 内容切分为文本块
     * 
     * @param content Markdown 文本内容
     * @param kid 知识库 ID
     * @return 切分后的文本块列表
     */
    @Override
    public List<String> getChunkList(String content, String kid) {
        return textSplitter.split(content, kid);
    }
}
