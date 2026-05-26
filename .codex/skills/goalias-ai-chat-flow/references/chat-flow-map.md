# GoaliasOS AI Chat Flow Map

## Main Streaming Path

```text
ChatController.sseChat
  -> SseServiceImpl.sseChat
    -> capture token and user id
    -> buildChatMessageList
      -> processKnowledgeBase
        -> KnowledgeInfoService
        -> ChatModelService vector model selection
        -> VectorStoreService query
      -> Redis user context
      -> PromptTemplateService system prompt
    -> create ChatSession if missing
    -> ChatCostService.saveMessage(user)
    -> autoSelectModelAndGetService
      -> ChatModelService model lookup
      -> ChatServiceFactory.getChatService
      -> BillingChatServiceProxy(provider)
    -> ChatRetryHelper if autoSelectModel=true
    -> provider.chat
      -> LangChain4j StreamingChatModel
      -> FunctionCallResponseHandler
        -> stream partial response
        -> record token usage
        -> execute tools if requested
        -> feedback tool results to AI
        -> complete emitter
    -> BillingSseEmitter.complete
      -> publish billing event
      -> save assistant message
    -> BillingEventListener
      -> ChatCostService.deductToken
```

## Important Files

| File | Role |
| --- | --- |
| `ChatController.java` | HTTP endpoints: `/chat/send`, `/chat/simple`; streaming fallback. |
| `ChatRequest.java` | Request DTO and runtime-enriched fields: token, prompt, sysPrompt, userId, role, messageId. |
| `SseServiceImpl.java` | Core orchestration: prompt assembly, knowledge retrieval, session creation, user-message persistence, model routing, retry. |
| `ChatServiceFactory.java` | Provider lookup by `providerName`; wraps original services with billing proxy. |
| `BillingChatServiceProxy.java` | Balance check, SSE wrapping, assistant response accumulation, assistant-message persistence, billing event publication. |
| `QwenChatServiceImpl.java` | Qwen streaming and simple chat implementation. |
| `GlmChatServiceImpl.java` | GLM streaming and simple chat implementation. |
| `FunctionCallsFactory.java` | Scans `OsToolProvider` beans and builds LangChain4j tool specifications. |
| `FunctionCallResponseHandler.java` | Handles streaming tokens, complete responses, tool execution loops, tool result feedback. |
| `FunctionCallExecutor.java` | Reflective Java tool execution with timeout and token context propagation. |
| `ChatCostServiceImpl.java` | Message persistence, pre-check balance, billing event publication, balance deduction. |
| `BillingEventListener.java` | Async billing listener. |
| `UserContextListener.java` | Async user-context extraction and Redis update. |
| `ChatRetryHelper.java` | Retry and fallback model scheduling. |

## Branch Checklist

- `kid` empty: normal chat prompt.
- `kid` present: knowledge retrieval and prompt enhancement.
- `hasAttachment=true`: category `image`.
- `autoSelectModel=true`: category `chat`, retry and fallback enabled.
- manual model: exact model lookup, no retry branch in `sseChat`.
- tool call detected: execute tools, send tool progress events, feed results back to AI.
- no tool call: complete stream and trigger billing proxy completion.
- provider error: `ChatServiceHelper.onStreamError`, possibly retry through `RetryNotifier`.
