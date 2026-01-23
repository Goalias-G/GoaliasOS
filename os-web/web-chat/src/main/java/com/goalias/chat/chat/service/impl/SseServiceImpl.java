package com.goalias.chat.chat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.goalias.chat.chat.factory.ChatServiceFactory;
import com.goalias.chat.chat.service.IChatCostService;
import com.goalias.chat.chat.service.IChatService;
import com.goalias.chat.chat.service.ISseService;
import com.goalias.chat.chat.support.ChatRetryHelper;
import com.goalias.chat.chat.support.RetryNotifier;
import com.goalias.chat.chat.util.SSEUtil;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.domain.PromptTemplate;
import com.goalias.chat.domain.bo.ChatSessionBo;
import com.goalias.chat.enums.promptTemplateEnum;
import com.goalias.chat.service.IChatModelService;
import com.goalias.chat.service.IChatSessionService;
import com.goalias.chat.service.IPromptTemplateService;
import com.goalias.common.chat.entity.chat.Message;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.utils.DateUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.knowledge.domain.KnowledgeInfo;
import com.goalias.knowledge.domain.bo.QueryVectorBo;
import com.goalias.knowledge.service.IKnowledgeInfoService;
import com.goalias.knowledge.service.VectorStoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Goalias
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements ISseService {

    private final VectorStoreService vectorStoreService;

    private final IChatCostService chatCostService;

    private final IChatModelService chatModelService;

    private final ChatServiceFactory chatServiceFactory;

    private final IChatSessionService chatSessionService;

    private final IKnowledgeInfoService knowledgeInfoService;

    private ChatModel chatModel;

    // 提示词模板服务
    private final IPromptTemplateService promptTemplateService;


    @Override
    public SseEmitter sseChat(ChatRequest chatRequest, HttpServletRequest request) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        try {
            // 记录当前会话令牌，供异步线程使用
            try {
                chatRequest.setToken(StpUtil.getTokenValue());
            } catch (Exception ignore) {
                // 保底：无token场景下忽略
            }
            // 构建消息列表
            buildChatMessageList(chatRequest);
            // 设置对话角色
            chatRequest.setRole(Message.Role.USER.getName());

            if (LoginHelper.isLogin()) {

                // 设置用户id
                chatRequest.setUserId(LoginHelper.getUserId());

                // 设置会话id
                if (chatRequest.getSessionId() == null) {
                    ChatSessionBo chatSessionBo = new ChatSessionBo();
                    chatSessionBo.setUserId(chatCostService.getUserId());
                    chatSessionBo.setSessionTitle(getFirst10Characters(chatRequest.getPrompt()));
                    chatSessionBo.setSessionContent(chatRequest.getPrompt());
                    chatSessionService.insertByBo(chatSessionBo);
                    chatRequest.setSessionId(chatSessionBo.getId());
                }
                
                // 保存用户消息
                chatCostService.saveMessage(chatRequest);
            }
            // 自动选择模型并获取对应的聊天服务
            IChatService chatService = autoSelectModelAndGetService(chatRequest);

            // 用户消息只保存不计费，AI回复由BillingChatServiceProxy自动处理计费
//             chatCostService.publishBillingEvent(chatRequest); // 用户输入不计费
            if (Boolean.TRUE.equals(chatRequest.getAutoSelectModel())) {
                ChatModel currentModel = this.chatModel;
                String currentCategory = currentModel.getCategory();
                ChatRetryHelper.executeWithRetry(
                        currentModel,
                        currentCategory,
                        chatModelService,
                        sseEmitter,
                        (modelForTry, onFailure) -> {
                            // 替换请求中的模型名称
                            chatRequest.setModel(modelForTry.getModelName());
                            // 以 emitter 实例为唯一键注册失败回调
                            RetryNotifier.setFailureCallback(sseEmitter, onFailure);
                            try {
                                autoSelectServiceByCategoryAndInvoke(chatRequest, sseEmitter,
                                        modelForTry.getProviderName());
                            } finally {
                                // 不在此处清理，待下游结束/失败时清理
                            }
                        }
                );
            } else {
                // 不重试不降级，直接调用
                chatService.chat(chatRequest, sseEmitter);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SSEUtil.sendErrorEvent(sseEmitter, e.getMessage());
        }
        return sseEmitter;
    }

    /**
     * 自动选择模型并获取对应的聊天服务
     */
    private IChatService autoSelectModelAndGetService(ChatRequest chatRequest) {
        try {
            if (Boolean.TRUE.equals(chatRequest.getHasAttachment())) {
                chatModel = selectModelByCategory("image");
            } else if (Boolean.TRUE.equals(chatRequest.getAutoSelectModel())) {
                chatModel = selectModelByCategory("chat");
            } else {
                chatModel = chatModelService.selectModelByName(chatRequest.getModel());
            }

            if (chatModel == null) {
                throw new IllegalStateException("未找到模型名称：" + chatRequest.getModel());
            }
            // 自动设置请求参数中的模型名称
            chatRequest.setModel(chatModel.getModelName());
            // 直接返回对应的聊天服务
            return chatServiceFactory.getChatService(chatModel.getProviderName());
        } catch (Exception e) {
            log.error("模型选择和服务获取失败: {}", e.getMessage(), e);
            throw new IllegalStateException("模型选择和服务获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据给定分类获取服务并发起调用（避免在降级时重复选择模型）
     */
    private void autoSelectServiceByCategoryAndInvoke(ChatRequest chatRequest, SseEmitter sseEmitter, String providerName) {
        IChatService service = chatServiceFactory.getChatService(providerName);
        service.chat(chatRequest, sseEmitter);
    }

    /**
     * 根据分类选择优先级最高的模型
     */
    private ChatModel selectModelByCategory(String category) {
        ChatModel model = chatModelService.selectModelByCategoryWithHighestPriority(category);
        if (model == null) {
            throw new IllegalStateException("未找到" + category + "分类的模型配置");
        }
        return model;
    }

    /**
     * 获取对话标题
     *
     * @param str 原字符
     * @return 截取后的字符
     */
    public static String getFirst10Characters(String str) {
        // 判断字符串长度
        if (str.length() > 10) {
            // 如果长度大于10，截取前10个字符
            return str.substring(0, 10);
        } else {
            // 如果长度不足10，返回整个字符串
            return str;
        }
    }

    /**
     * 构建消息列表
     */
    private void buildChatMessageList(ChatRequest chatRequest) {
        List<Message> messages = chatRequest.getMessages();

        // 处理知识库相关逻辑
        String sysPrompt = processKnowledgeBase(chatRequest, messages);

        // 设置系统提示词
        Message sysMessage = Message.builder()
                .content(sysPrompt)
                .role(Message.Role.SYSTEM)
                .build();
        messages.add(0, sysMessage);

        chatRequest.setSysPrompt(sysPrompt);

        // 用户对话内容 ??为什么不直接是入参
        String chatString = null;
        // 获取用户对话信息
        Object content = messages.get(messages.size() - 1).getContent();
        if (content instanceof List<?> listContent) {
            if (CollectionUtil.isNotEmpty(listContent)) {
                chatString = listContent.get(0).toString();
            }
        } else {
            chatString = content.toString();
        }
        chatRequest.setPrompt(chatString);
    }

    /**
     * 处理知识库相关逻辑
     */
    private String processKnowledgeBase(ChatRequest chatRequest, List<Message> messages) {
        if (StringUtils.isEmpty(chatRequest.getKid())) {
            return getPromptTemplatePrompt(promptTemplateEnum.CHAT.getDesc());
        }

        try {
            // 查询知识库信息
            KnowledgeInfo knowledgeInfo = knowledgeInfoService.queryById(Long.valueOf(chatRequest.getKid()));
            if (Objects.isNull(knowledgeInfo)) {
                log.warn("知识库信息不存在，kid: {}", chatRequest.getKid());
                return getPromptTemplatePrompt(promptTemplateEnum.CHAT.getDesc());
            }

            // 查询向量模型配置信息
            ChatModel chatModel = chatModelService.selectModelByName(knowledgeInfo.getEmbeddingModelName());
            if (Objects.isNull(chatModel)) {
                log.warn("向量模型配置不存在，模型名称: {}", knowledgeInfo.getEmbeddingModelName());
                return getPromptTemplatePrompt(promptTemplateEnum.CHAT.getDesc());
            }

            // 构建向量查询参数
            QueryVectorBo queryVectorBo = buildQueryVectorBo(chatRequest, knowledgeInfo, chatModel);

            // 获取向量查询结果
            List<String> nearestList = vectorStoreService.getQueryVector(queryVectorBo);

            // 添加知识库消息到上下文
            addKnowledgeMessages(messages, nearestList);

            // 返回知识库系统提示词
            return getKnowledgeSystemPrompt(knowledgeInfo);

        } catch (Exception e) {
            log.error("处理知识库信息失败: {}", e.getMessage(), e);
            return getPromptTemplatePrompt(promptTemplateEnum.CHAT.getDesc());
        }
    }

    /**
     * 构建向量查询参数
     */
    private QueryVectorBo buildQueryVectorBo(ChatRequest chatRequest, KnowledgeInfo knowledgeInfo,
                                             ChatModel chatModel) {
        String content = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent().toString();

        QueryVectorBo queryVectorBo = new QueryVectorBo();
        queryVectorBo.setQuery(content);
        queryVectorBo.setKid(chatRequest.getKid());
        queryVectorBo.setApiKey(chatModel.getApiKey());
        queryVectorBo.setBaseUrl(chatModel.getApiHost());
        queryVectorBo.setVectorModelName(knowledgeInfo.getVectorModelName());
        queryVectorBo.setEmbeddingModelName(knowledgeInfo.getEmbeddingModelName());
        queryVectorBo.setMaxResults(Math.toIntExact(knowledgeInfo.getRetrieveLimit()));

        return queryVectorBo;
    }

    /**
     * 添加知识库消息到上下文
     */
    private void addKnowledgeMessages(List<Message> messages, List<String> nearestList) {
        for (String prompt : nearestList) {
            Message userMessage = Message.builder()
                    .content(prompt)
                    .role(Message.Role.USER)
                    .build();
            messages.add(userMessage);
        }
    }


    /**
     * 获取提示词模板提示词
     */
    private String getPromptTemplatePrompt(String category) {
        PromptTemplate promptTemplate = promptTemplateService.queryByCategory(category);
        if (Objects.isNull(promptTemplate) || StringUtils.isEmpty(promptTemplate.getTemplateContent())) {
            return getDefaultSystemPrompt();
        }
        return promptTemplate.getTemplateContent();
    }

    /**
     * 获取默认系统提示词
     */
    private String getDefaultSystemPrompt() {
        String sysPrompt = chatModel != null ? chatModel.getSystemPrompt() : null;
        if (StringUtils.isEmpty(sysPrompt)) {
            sysPrompt = "你是一个由 Goalias(系统开发者英文名) 开发的 GoaliasOS 系统助手，名字叫 GoaliasOS AI。"
                    + "你擅长中英文对话，能够理解并处理各种问题，提供安全、有帮助、准确的回答。"
                    + "语言风格灵动、自然、幽默。"
                    + "当前时间：" + DateUtils.dateTimeNow()
                    + "#注意：回复之前注意结合上下文和工具返回内容进行回复。";
        }
        return sysPrompt;
    }

    /**
     * 获取知识库系统提示词
     */
    private String getKnowledgeSystemPrompt(KnowledgeInfo knowledgeInfo) {
        String sysPrompt = knowledgeInfo.getSystemPrompt();
        if (StringUtils.isEmpty(sysPrompt)) {
            sysPrompt = "###角色设定\n" +
                    "你是一个由 Goalias(系统开发者英文名) 开发的 GoaliasOS 系统助手，名字叫 GoaliasOS AI，专注于利用上下文中的信息来提供准确和相关的回答。\n" +
                    "###指令\n" +
                    "当用户的问题与上下文知识匹配时，利用上下文信息进行回答。如果问题与上下文不匹配，运用自身的推理能力生成合适的回答。\n" +
                    "###限制\n" +
                    "确保回答清晰简洁，避免提供不必要的细节。始终保持语气友好，语言风格灵动、自然、幽默。\n" +
                    "当前时间：" + DateUtils.dateTimeNow();
        }
        return sysPrompt;
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) {
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
