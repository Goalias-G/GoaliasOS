---
name: goalias-ai-chat-flow
description: Analyze the full GoaliasOS AI chat implementation flow from ChatController through SSE, prompt assembly, knowledge retrieval, model selection, provider invocation, function calling, billing, persistence, retry, and user-context updates. Use when the user asks to explain, trace, document, review, or debug the AI dialogue pipeline, especially around ChatController.java, /chat/send, /chat/simple, SseServiceImpl, IChatService implementations, Function Calling, or chat billing.
---

# GoaliasOS AI Chat Flow

Use this skill to produce a step-by-step code analysis of GoaliasOS AI dialogue behavior. Keep the explanation grounded in actual files and line references from the current workspace.

## Required Reading Order

Read these files first, then branch as needed:

1. `os-web/web-chat/src/main/java/com/goalias/chat/chat/controller/chat/ChatController.java`
2. `os-common/common-chat/src/main/java/com/goalias/common/chat/request/ChatRequest.java`
3. `os-web/web-chat/src/main/java/com/goalias/chat/chat/service/impl/SseServiceImpl.java`
4. `os-web/web-chat/src/main/java/com/goalias/chat/chat/factory/ChatServiceFactory.java`
5. `os-web/web-chat/src/main/java/com/goalias/chat/chat/service/proxy/BillingChatServiceProxy.java`
6. Provider implementations under `os-web/web-chat/src/main/java/com/goalias/chat/chat/service/impl/*ChatServiceImpl.java`
7. Function Calling files:
   - `FunctionCallsFactory.java`
   - `FunctionCallResponseHandler.java`
   - `FunctionCallExecutor.java`
   - tool providers under `os-web/web-chat/src/main/java/com/goalias/chat/chat/tools/`
8. Billing and async listeners:
   - `ChatCostServiceImpl.java`
   - `BillingEventListener.java`
   - `UserContextListener.java`
9. Model and knowledge services:
   - `ChatModelServiceImpl.java`
   - `KnowledgeInfoServiceImpl.java`
   - `VectorStoreService` implementations

For a compact static map, read `references/chat-flow-map.md` after the source files when you need a checklist.

## Analysis Workflow

1. Start with the API surface:
   - Explain `/chat/send` as the SSE streaming path.
   - Explain `/chat/simple` as the synchronous response path.
   - Mention `@GoaliasFallback` on `/chat/send` and its QPS fallback behavior.

2. Trace request enrichment in `SseServiceImpl.sseChat`:
   - Capture Sa-Token token and store it in `ChatRequest`.
   - Set current user id from `LoginHelper`.
   - Call `buildChatMessageList`.
   - Set role to `user`.
   - Create a chat session when `sessionId` is absent.
   - Save the user message before invoking the model.

3. Explain prompt construction:
   - The last message is treated as the user prompt.
   - If `kid` is absent, use the normal chat prompt template.
   - If `kid` exists, query knowledge info, select embedding model, retrieve vector fragments, and inject retrieved knowledge into the user message.
   - Load user context from Redis key `CacheNames.CHAT_USER_CONTEXT + userId`.
   - Insert the final system prompt at the head of `messages`.

4. Explain model routing:
   - `hasAttachment=true` selects category `image`.
   - `autoSelectModel=true` selects category `chat`.
   - Otherwise select the exact `model`.
   - Resolve provider via `ChatModel.providerName`.
   - `ChatServiceFactory.getChatService(providerName)` wraps the provider with `BillingChatServiceProxy`.

5. Explain streaming provider execution:
   - Qwen and GLM services build LangChain4j streaming models from `ChatModel`.
   - Convert internal `Message` objects to LangChain4j messages through `IChatService.toLangChainToolRequest`.
   - Attach tool specifications from `FunctionCallsFactory`.
   - Use `FunctionCallResponseHandler` for partial tokens, complete responses, tool calls, and errors.

6. Explain Function Calling:
   - `FunctionCallsFactory` scans Spring beans implementing `OsToolProvider`.
   - Methods annotated with `@OsTool` become LangChain4j `ToolSpecification`s.
   - `FunctionCallResponseHandler` streams normal partial responses immediately.
   - On tool requests, execute each tool through `FunctionCallExecutor`.
   - Pass the captured token into async tool execution so `TtlTokenContext` can recover user identity.
   - Send tool progress events to SSE, combine tool results, feed them back to the model, and continue until no more tool calls remain.

7. Explain billing and persistence:
   - The user message is saved before AI invocation.
   - `BillingChatServiceProxy` checks balance before delegating.
   - Its wrapped emitter forwards chunks and accumulates AI text.
   - On completion, it publishes a billing event and saves the assistant message.
   - `BillingEventListener` handles the async event and calls `ChatCostServiceImpl.deductToken`.
   - Token usage statistics are recorded to Redis by `ChatServiceHelper.recordTokenUsage`.

8. Explain retry and degradation:
   - If automatic model selection is enabled, `ChatRetryHelper` retries the current model up to three times.
   - If still failing, it selects a lower-priority fallback model in the same category.
   - The helper depends on lower-level stream failures calling `RetryNotifier.notifyFailure`.

9. Compare `/chat/simple`:
   - It calls `SseServiceImpl.simpleChat`.
   - It forces `autoSelectModel=true`.
   - Optional prompt template content is appended when a template enum is provided.
   - Provider `simpleChat` uses a non-streaming LangChain4j model and returns text directly.
   - Check whether the external caller has already supplied valid messages; do not assume `simpleChat` performs the same enrichment as `sseChat`.

## Output Shape

Prefer this structure:

1. **入口概览**: controller endpoints and request shape.
2. **主链路时序**: numbered steps from HTTP request to final response.
3. **关键分支**: knowledge base, auto model selection, manual model selection, attachments, tools, retry.
4. **数据落库与计费**: user message, assistant message, async billing, Redis token usage.
5. **文件职责表**: file path and responsibility.
6. **风险点或可优化点**: only include concrete issues grounded in source.

When useful, include a Mermaid sequence diagram. Keep it readable and do not invent components that are not present in the code.

## Review Focus

When the user asks for review or optimization, inspect these risks first:

- `messages` may be empty or last message may not be text-like.
- `simpleChat` path may not build system prompt, user id, session, or knowledge context like `sseChat`.
- `BillingSseEmitter` only accumulates content formats it can parse; provider event formats may affect assistant message persistence.
- Tool execution depends on token context propagation and timeout handling.
- `FunctionCallsFactory.scanAndBuildTools` should not duplicate tool specs if invoked multiple times.
- Retry only works when downstream failures notify `RetryNotifier`.
- Knowledge retrieval failure falls back to normal chat prompt; verify whether that is desired for the scenario.
