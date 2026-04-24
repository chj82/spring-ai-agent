package com.example.agent.service.chat;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.example.agent.ai.model.AiChatService;
import com.example.agent.common.exception.BizException;
import com.example.agent.mapper.ConversationMapper;
import com.example.agent.mapper.ConversationMessageMapper;
import com.example.agent.model.dto.ChatSendRequest;
import com.example.agent.model.entity.ConversationEntity;
import com.example.agent.model.entity.ConversationMessageEntity;
import com.example.agent.model.enums.MessageRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ChatService {

    private static final long SSE_TIMEOUT_MILLIS = 0L;

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final AiChatService aiChatService;

    public ChatService(
        ConversationMapper conversationMapper,
        ConversationMessageMapper conversationMessageMapper,
        AiChatService aiChatService
    ) {
        this.conversationMapper = conversationMapper;
        this.conversationMessageMapper = conversationMessageMapper;
        this.aiChatService = aiChatService;
    }

    public SseEmitter send(Long userId, ChatSendRequest request) {
        ConversationEntity conversation = conversationMapper.findByIdAndUserId(request.getConversationId(), userId);
        if (conversation == null) {
            throw new BizException("会话不存在");
        }

        saveMessage(request.getConversationId(), userId, MessageRole.USER.name(), request.getMessage());
        conversationMapper.updateStats(request.getConversationId(), userId, 1, LocalDateTime.now());
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        StringBuilder replyBuilder = new StringBuilder();
        AtomicBoolean closed = new AtomicBoolean(false);

        if (!sendEvent(emitter, "start", Map.of("conversationId", request.getConversationId()))) {
            emitter.complete();
            return emitter;
        }

        Disposable disposable = aiChatService.streamChat(userId, request.getConversationId(), request.getMessage())
            .doOnNext(nodeOutput -> handleStreamOutput(nodeOutput, emitter, replyBuilder, closed))
            .doOnError(throwable -> handleStreamError(throwable, emitter, closed))
            .doOnComplete(() -> handleStreamComplete(userId, request.getConversationId(), emitter, replyBuilder, closed))
            .subscribe();

        bindEmitterLifecycle(emitter, disposable, closed);
        return emitter;
    }

    private ConversationMessageEntity saveMessage(
        Long conversationId,
        Long userId,
        String role,
        String content
    ) {
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setConversationId(conversationId);
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setStatus("SUCCESS");
        entity.setDeleted(Boolean.FALSE);
        conversationMessageMapper.insert(entity);
        return entity;
    }

    private boolean sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private String buildModelErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "模型调用失败";
        }
        return "模型调用失败: " + message;
    }

    private String extractAssistantDelta(NodeOutput nodeOutput, String currentReply) {
        Optional<List> messagesOptional = nodeOutput.state().value("messages");
        if (messagesOptional.isEmpty()) {
            return "";
        }

        List messages = messagesOptional.get();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Object candidate = messages.get(i);
            if (candidate instanceof AssistantMessage assistantMessage) {
                String text = assistantMessage.getText();
                if (text == null || text.isBlank()) {
                    return "";
                }
                if (currentReply.isEmpty()) {
                    return text;
                }
                if (text.startsWith(currentReply)) {
                    return text.substring(currentReply.length());
                }
                return "";
            }
            if (candidate instanceof Message) {
                continue;
            }
        }
        return "";
    }

    private void handleStreamOutput(
        NodeOutput nodeOutput,
        SseEmitter emitter,
        StringBuilder replyBuilder,
        AtomicBoolean closed
    ) {
        String content = extractAssistantDelta(nodeOutput, replyBuilder.toString());
        if (StringUtils.isBlank(content)) {
            return;
        }
        replyBuilder.append(content);
        if (!sendEvent(emitter, "delta", Map.of("content", content))) {
            completeEmitter(emitter, closed);
        }
    }

    private void handleStreamError(Throwable throwable, SseEmitter emitter, AtomicBoolean closed) {
        if (closed.compareAndSet(false, true)) {
            sendEvent(emitter, "error", Map.of("message", buildModelErrorMessage(throwable)));
            emitter.complete();
        }
    }

    private void handleStreamComplete(
        Long userId,
        Long conversationId,
        SseEmitter emitter,
        StringBuilder replyBuilder,
        AtomicBoolean closed
    ) {
        if (closed.compareAndSet(false, true)) {
            ConversationMessageEntity assistantMessage = saveMessage(
                conversationId,
                userId,
                MessageRole.ASSISTANT.name(),
                replyBuilder.toString()
            );
            conversationMapper.updateStats(conversationId, userId, 1, LocalDateTime.now());

            Map<String, Object> doneData = new LinkedHashMap<>();
            doneData.put("conversationId", conversationId);
            doneData.put("assistantMessageId", assistantMessage.getId());
            doneData.put("reply", replyBuilder.toString());
            sendEvent(emitter, "done", doneData);
            emitter.complete();
        }
    }

    private void bindEmitterLifecycle(SseEmitter emitter, Disposable disposable, AtomicBoolean closed) {
        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> disposeAndComplete(disposable, emitter, closed));
        emitter.onError(error -> disposeAndCompleteWithError(disposable, emitter, error, closed));
    }

    private void disposeAndComplete(Disposable disposable, SseEmitter emitter, AtomicBoolean closed) {
        disposable.dispose();
        completeEmitter(emitter, closed);
    }

    private void disposeAndCompleteWithError(
        Disposable disposable,
        SseEmitter emitter,
        Throwable error,
        AtomicBoolean closed
    ) {
        disposable.dispose();
        if (closed.compareAndSet(false, true)) {
            emitter.completeWithError(error);
        }
    }

    private void completeEmitter(SseEmitter emitter, AtomicBoolean closed) {
        if (closed.compareAndSet(false, true)) {
            emitter.complete();
        }
    }
}
