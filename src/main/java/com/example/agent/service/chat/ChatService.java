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
import com.example.agent.model.vo.ChatReplyVO;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

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

    @Transactional
    public ChatReplyVO send(Long userId, ChatSendRequest request) {
        ConversationEntity conversation = conversationMapper.findByIdAndUserId(request.getConversationId(), userId);
        if (conversation == null) {
            throw new BizException("会话不存在");
        }

        if ((conversation.getMessageCount() == null || conversation.getMessageCount() == 0) && conversation.getLastMessageAt() == null) {
            officialChatMemoryService.clear(request.getConversationId());
        }

        saveMessage(request.getConversationId(), userId, MessageRole.USER.name(), request.getMessage(), null);
        String reply = aiChatService.chat(request.getConversationId(), request.getMessage());
        ConversationMessageEntity assistantMessage = saveMessage(
            request.getConversationId(),
            userId,
            MessageRole.ASSISTANT.name(),
            reply,
            null
        );
        conversationMapper.updateStats(request.getConversationId(), userId, 2, LocalDateTime.now());

        ChatReplyVO vo = new ChatReplyVO();
        vo.setConversationId(request.getConversationId());
        vo.setAssistantMessageId(assistantMessage.getId());
        vo.setReply(reply);
        return vo;
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
}
