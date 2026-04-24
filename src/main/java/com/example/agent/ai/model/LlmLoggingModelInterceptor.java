package com.example.agent.ai.model;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.example.agent.model.entity.LlmCallLogEntity;
import com.example.agent.service.chat.LlmCallLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * LLM 调用日志拦截器。
 * 在 Agent 发起真实模型调用前后记录请求、响应、工具调用和 token 信息。
 */
@Component
public class LlmLoggingModelInterceptor extends ModelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LlmLoggingModelInterceptor.class);

    private final ObjectMapper objectMapper;
    private final LlmCallLogService llmCallLogService;
    private final String agentName = "conversation-agent";

    public LlmLoggingModelInterceptor(
        ObjectMapper objectMapper,
        LlmCallLogService llmCallLogService
    ) {
        this.objectMapper = objectMapper;
        this.llmCallLogService = llmCallLogService;
    }

    @Override
    public String getName() {
        return "llm-call-log";
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        // 先落一条 PENDING 日志，确保即使流式调用中途失败，也能保留请求现场。
        LlmCallLogEntity entity = buildPendingEntity(request);
        llmCallLogService.createPendingLog(entity);

        try {
            ModelResponse response = handler.call(request);
            Object message = response.getMessage();
            if (message instanceof Flux<?> flux) {
                // 流式调用要等到流结束后，才能拿到较完整的返回文本和 usage。
                return ModelResponse.of(wrapStreamResponse(entity, flux));
            }

            fillSuccessFields(entity, response.getChatResponse(), message);
            llmCallLogService.markSuccess(entity);
            return response;
        }
        catch (Exception exception) {
            llmCallLogService.markError(entity.getId(), exception.getMessage());
            throw exception;
        }
    }

    /**
     * 包装流式响应。
     * 这里不会打断原有流式输出，只是在流动过程中旁路汇总结果，结束时再统一落库。
     */
    private Flux<ChatResponse> wrapStreamResponse(LlmCallLogEntity entity, Flux<?> flux) {
        StreamCapture capture = new StreamCapture();
        return flux.cast(ChatResponse.class)
            .doOnNext(chatResponse -> capture.accept(chatResponse))
            .doOnComplete(() -> {
                fillStreamSuccessFields(entity, capture);
                llmCallLogService.markSuccess(entity);
            })
            .doOnError(throwable -> llmCallLogService.markError(entity.getId(), throwable.getMessage()));
    }

    /** 构建待落库的请求日志快照。 */
    private LlmCallLogEntity buildPendingEntity(ModelRequest request) {
        LlmCallLogEntity entity = new LlmCallLogEntity();
        entity.setConversationId(parseLong(request.getContext().get("conversation_id")));
        entity.setUserId(parseLong(request.getContext().get("user_id")));
        entity.setThreadId(resolveThreadId(request.getContext()));
        entity.setAgentName(agentName);
        entity.setRequestPayloadJson(toJson(buildRequestPayload(request)));
        return entity;
    }

    /** 非流式调用成功后的回填逻辑。 */
    private void fillSuccessFields(LlmCallLogEntity entity, ChatResponse chatResponse, Object message) {
        entity.setResponsePayloadJson(toJson(buildResponsePayload(chatResponse, message)));
        applyUsage(entity, chatResponse == null ? null : chatResponse.getMetadata().getUsage());
        entity.setErrorMessage(null);
    }

    /** 流式调用完成后的回填逻辑。 */
    private void fillStreamSuccessFields(LlmCallLogEntity entity, StreamCapture capture) {
        entity.setResponsePayloadJson(toJson(buildStreamResponsePayload(capture)));
        applyUsage(entity, capture.usage);
        entity.setErrorMessage(null);
    }

    /** 将 usage 中的 token 统计写回实体。 */
    private void applyUsage(LlmCallLogEntity entity, Usage usage) {
        if (usage == null) {
            return;
        }
        entity.setPromptTokens(usage.getPromptTokens());
        entity.setCompletionTokens(usage.getCompletionTokens());
        entity.setTotalTokens(usage.getTotalTokens());
    }

    private String extractFinishReason(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getMetadata() == null) {
            return null;
        }
        ChatGenerationMetadata metadata = chatResponse.getResult().getMetadata();
        return metadata.getFinishReason();
    }

    private String extractMessageText(Object message) {
        if (message instanceof AbstractMessage abstractMessage) {
            return abstractMessage.getText();
        }
        return message == null ? null : message.toString();
    }

    private List<Map<String, Object>> extractToolCalls(Object message) {
        if (!(message instanceof AssistantMessage assistantMessage) || !assistantMessage.hasToolCalls()) {
            return List.of();
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", toolCall.id());
            item.put("type", toolCall.type());
            item.put("name", toolCall.name());
            item.put("arguments", toolCall.arguments());
            results.add(item);
        }
        return results;
    }

    /** 将一次模型调用的请求现场统一收敛为一个 JSON 结构。 */
    private Map<String, Object> buildRequestPayload(ModelRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("systemMessage", request.getSystemMessage() == null ? null : request.getSystemMessage().getText());
        result.put("messages", summarizeMessages(request.getMessages()));
        result.put("tools", summarizeTools(request));
        result.put("options", summarizeOptions(request.getOptions()));
        result.put("context", request.getContext());
        return result;
    }

    /** 将非流式响应统一收敛为一个 JSON 结构。 */
    private Map<String, Object> buildResponsePayload(ChatResponse chatResponse, Object message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", extractMessageText(message));
        result.put("toolCalls", extractToolCalls(message));
        result.put("finishReason", extractFinishReason(chatResponse));
        result.put("metadata", summarizeResponseMetadata(chatResponse));
        return result;
    }

    /** 将流式响应的汇总结果统一收敛为一个 JSON 结构。 */
    private Map<String, Object> buildStreamResponsePayload(StreamCapture capture) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", capture.responseText.toString());
        result.put("toolCalls", capture.toolCalls);
        result.put("finishReason", capture.finishReason);
        result.put("metadata", capture.responseMetadata);
        return result;
    }

    /**
     * 将请求消息转换成稳定、可读的摘要结构。
     * 这里不直接存框架对象，避免后续升级时序列化结构不稳定。
     */
    private List<Map<String, Object>> summarizeMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", message.getMessageType().name());
            if (message instanceof AbstractMessage abstractMessage) {
                item.put("text", abstractMessage.getText());
                item.put("metadata", abstractMessage.getMetadata());
            }
            if (message instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCalls()) {
                item.put("toolCalls", extractToolCalls(assistantMessage));
            }
            if (message instanceof ToolResponseMessage toolResponseMessage) {
                List<Map<String, Object>> responses = new ArrayList<>();
                for (ToolResponseMessage.ToolResponse response : toolResponseMessage.getResponses()) {
                    Map<String, Object> responseItem = new LinkedHashMap<>();
                    responseItem.put("id", response.id());
                    responseItem.put("name", response.name());
                    responseItem.put("responseData", response.responseData());
                    responses.add(responseItem);
                }
                item.put("toolResponses", responses);
            }
            results.add(item);
        }
        return results;
    }

    /** 记录当前轮次可用的工具名和描述，便于排查模型为什么会选中某个工具。 */
    private Map<String, Object> summarizeTools(ModelRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("toolNames", request.getTools());
        result.put("toolDescriptions", request.getToolDescriptions());
        return result;
    }

    /** 提取对排查最有价值的模型参数，避免把完整 options 对象直接入库。 */
    private Map<String, Object> summarizeOptions(ChatOptions options) {
        if (options == null) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("model", options.getModel());
        result.put("temperature", options.getTemperature());
        result.put("maxTokens", options.getMaxTokens());
        result.put("topP", options.getTopP());
        result.put("topK", options.getTopK());
        result.put("frequencyPenalty", options.getFrequencyPenalty());
        result.put("presencePenalty", options.getPresencePenalty());
        result.put("stopSequences", options.getStopSequences());
        return result;
    }

    /** 提取响应元信息，便于后续排查具体命中了哪个模型或响应ID。 */
    private Map<String, Object> summarizeResponseMetadata(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getMetadata() == null) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", chatResponse.getMetadata().getId());
        result.put("model", chatResponse.getMetadata().getModel());
        result.put("usage", summarizeUsage(chatResponse.getMetadata().getUsage()));
        return result;
    }

    private Map<String, Object> summarizeUsage(Usage usage) {
        if (usage == null) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("promptTokens", usage.getPromptTokens());
        result.put("completionTokens", usage.getCompletionTokens());
        result.put("totalTokens", usage.getTotalTokens());
        result.put("nativeUsage", usage.getNativeUsage());
        return result;
    }

    /** 优先使用显式 thread_id，没有时退化为 conversation_id。 */
    private String resolveThreadId(Map<String, Object> context) {
        Object threadId = context.get("thread_id");
        if (threadId != null) {
            return threadId.toString();
        }
        Object conversationId = context.get("conversation_id");
        return conversationId == null ? null : conversationId.toString();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        }
        catch (NumberFormatException exception) {
            return null;
        }
    }

    /** 序列化失败时返回空 JSON，避免日志逻辑影响主调用链路。 */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException exception) {
            log.warn("Failed to serialize LLM log payload", exception);
            return "{}";
        }
    }

    /**
     * 流式结果捕获器。
     * 用于在不影响 SSE 输出的前提下，增量汇总完整文本、tool calls 和 usage。
     */
    private static final class StreamCapture {
        private final StringBuilder responseText = new StringBuilder();
        private List<Map<String, Object>> toolCalls = List.of();
        private String finishReason;
        private Usage usage;
        private Map<String, Object> responseMetadata = Map.of();

        private void accept(ChatResponse chatResponse) {
            if (chatResponse == null || chatResponse.getResult() == null) {
                return;
            }

            AssistantMessage output = chatResponse.getResult().getOutput();
            if (output != null) {
                mergeText(output.getText());
                if (output.hasToolCalls()) {
                    List<Map<String, Object>> currentToolCalls = new ArrayList<>();
                    for (AssistantMessage.ToolCall toolCall : output.getToolCalls()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", toolCall.id());
                        item.put("type", toolCall.type());
                        item.put("name", toolCall.name());
                        item.put("arguments", toolCall.arguments());
                        currentToolCalls.add(item);
                    }
                    this.toolCalls = currentToolCalls;
                }
            }

            if (chatResponse.getResult().getMetadata() != null) {
                this.finishReason = chatResponse.getResult().getMetadata().getFinishReason();
            }
            if (chatResponse.getMetadata() != null) {
                this.usage = chatResponse.getMetadata().getUsage();
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("id", chatResponse.getMetadata().getId());
                metadata.put("model", chatResponse.getMetadata().getModel());
                if (this.usage != null) {
                    Map<String, Object> usageMap = new LinkedHashMap<>();
                    usageMap.put("promptTokens", this.usage.getPromptTokens());
                    usageMap.put("completionTokens", this.usage.getCompletionTokens());
                    usageMap.put("totalTokens", this.usage.getTotalTokens());
                    usageMap.put("nativeUsage", this.usage.getNativeUsage());
                    metadata.put("usage", usageMap);
                }
                this.responseMetadata = metadata;
            }
        }

        /**
         * 尽量把流式增量合并成最终文本。
         * 当前框架下常见情况是后续片段会包含前缀全文，所以优先做前缀覆盖。
         */
        private void mergeText(String text) {
            if (text == null || text.isBlank()) {
                return;
            }
            String current = responseText.toString();
            if (current.isEmpty()) {
                responseText.append(text);
                return;
            }
            if (text.startsWith(current)) {
                responseText.setLength(0);
                responseText.append(text);
                return;
            }
            responseText.append(text);
        }
    }
}
