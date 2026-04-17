package com.example.agent.service.chat;

import com.example.agent.ai.memory.OfficialChatMemoryService;
import com.example.agent.ai.model.AiChatService;
import com.example.agent.common.exception.BizException;
import com.example.agent.mapper.ConversationMapper;
import com.example.agent.mapper.ConversationMessageMapper;
import com.example.agent.model.dto.ChatSendRequest;
import com.example.agent.model.entity.ConversationEntity;
import com.example.agent.model.entity.ConversationMessageEntity;
import com.example.agent.model.enums.MessageRole;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Service
public class ChatService {

    private static final long SSE_TIMEOUT_MILLIS = 0L;

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final OfficialChatMemoryService officialChatMemoryService;
    private final AiChatService aiChatService;

    public ChatService(
        ConversationMapper conversationMapper,
        ConversationMessageMapper conversationMessageMapper,
        OfficialChatMemoryService officialChatMemoryService,
        AiChatService aiChatService
    ) {
        this.conversationMapper = conversationMapper;
        this.conversationMessageMapper = conversationMessageMapper;
        this.officialChatMemoryService = officialChatMemoryService;
        this.aiChatService = aiChatService;
    }

    public SseEmitter send(Long userId, ChatSendRequest request) {
        ConversationEntity conversation = conversationMapper.findByIdAndUserId(request.getConversationId(), userId);
        if (conversation == null) {
            throw new BizException("会话不存在");
        }

        if ((conversation.getMessageCount() == null || conversation.getMessageCount() == 0) && conversation.getLastMessageAt() == null) {
            officialChatMemoryService.clear(request.getConversationId());
        }

        saveMessage(request.getConversationId(), userId, MessageRole.USER.name(), request.getMessage(), null);
        conversationMapper.updateStats(request.getConversationId(), userId, 1, LocalDateTime.now());
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        StringBuilder replyBuilder = new StringBuilder();
        AtomicBoolean closed = new AtomicBoolean(false);

        if (!sendEvent(emitter, "start", Map.of("conversationId", request.getConversationId()))) {
            emitter.complete();
            return emitter;
        }

        Disposable disposable = aiChatService.streamChat(request.getConversationId(), request.getMessage()).subscribe(
            delta -> {
                replyBuilder.append(delta);
                if (!sendEvent(emitter, "delta", Map.of("content", delta))) {
                    closed.set(true);
                    emitter.complete();
                }
            },
            throwable -> {
                if (closed.compareAndSet(false, true)) {
                    sendEvent(emitter, "error", Map.of("message", buildModelErrorMessage(throwable)));
                    emitter.complete();
                }
            },
            () -> {
                if (closed.compareAndSet(false, true)) {
                    ConversationMessageEntity assistantMessage = saveMessage(
                        request.getConversationId(),
                        userId,
                        MessageRole.ASSISTANT.name(),
                        replyBuilder.toString(),
                        null
                    );
                    conversationMapper.updateStats(request.getConversationId(), userId, 1, LocalDateTime.now());

                    Map<String, Object> doneData = new LinkedHashMap<>();
                    doneData.put("conversationId", request.getConversationId());
                    doneData.put("assistantMessageId", assistantMessage.getId());
                    doneData.put("reply", replyBuilder.toString());
                    sendEvent(emitter, "done", doneData);
                    emitter.complete();
                }
            }
        );

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            disposable.dispose();
            if (closed.compareAndSet(false, true)) {
                emitter.complete();
            }
        });
        emitter.onError(error -> {
            disposable.dispose();
            if (closed.compareAndSet(false, true)) {
                emitter.completeWithError(error);
            }
        });
        return emitter;
    }

    private ConversationMessageEntity saveMessage(
        Long conversationId,
        Long userId,
        String role,
        String content,
        Integer tokenUsage
    ) {
        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setConversationId(conversationId);
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setTokenUsage(tokenUsage);
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
}
