package com.example.agent.service.app;

import com.example.agent.ai.memory.OfficialChatMemoryService;
import com.example.agent.common.exception.BizException;
import com.example.agent.mapper.ConversationMapper;
import com.example.agent.mapper.ConversationMessageMapper;
import com.example.agent.model.dto.ConversationIdRequest;
import com.example.agent.model.dto.CreateConversationRequest;
import com.example.agent.model.dto.UpdateConversationTitleRequest;
import com.example.agent.model.entity.ConversationEntity;
import com.example.agent.model.entity.ConversationMessageEntity;
import com.example.agent.model.vo.ConversationMessageVO;
import com.example.agent.model.vo.ConversationVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    private final OfficialChatMemoryService officialChatMemoryService;

    public ConversationService(
        ConversationMapper conversationMapper,
        ConversationMessageMapper conversationMessageMapper,
        OfficialChatMemoryService officialChatMemoryService
    ) {
        this.conversationMapper = conversationMapper;
        this.conversationMessageMapper = conversationMessageMapper;
        this.officialChatMemoryService = officialChatMemoryService;
    }

    public List<ConversationVO> list(Long userId) {
        return conversationMapper.findByUserId(userId).stream().map(this::toVo).toList();
    }

    public ConversationVO create(Long userId, CreateConversationRequest request) {
        ConversationEntity entity = new ConversationEntity();
        entity.setUserId(userId);
        entity.setTitle(request.getTitle());
        entity.setMessageCount(0);
        entity.setDeleted(Boolean.FALSE);
        conversationMapper.insert(entity);
        return toVo(conversationMapper.findByIdAndUserId(entity.getId(), userId));
    }

    public ConversationVO get(Long userId, Long conversationId) {
        return toVo(requireConversation(userId, conversationId));
    }

    public ConversationVO get(Long userId, ConversationIdRequest request) {
        return get(userId, request.getId());
    }

    public void updateTitle(Long userId, Long conversationId, UpdateConversationTitleRequest request) {
        requireConversation(userId, conversationId);
        conversationMapper.updateTitle(conversationId, userId, request.getTitle());
    }

    public void updateTitle(Long userId, UpdateConversationTitleRequest request) {
        updateTitle(userId, request.getId(), request);
    }

    public void delete(Long userId, Long conversationId) {
        requireConversation(userId, conversationId);
        conversationMapper.softDelete(conversationId, userId);
        officialChatMemoryService.clear(conversationId);
    }

    public void delete(Long userId, ConversationIdRequest request) {
        delete(userId, request.getId());
    }

    public List<ConversationMessageVO> messages(Long userId, Long conversationId) {
        requireConversation(userId, conversationId);
        return conversationMessageMapper.findByConversationId(conversationId, userId).stream().map(this::toMessageVo).toList();
    }

    public List<ConversationMessageVO> messages(Long userId, ConversationIdRequest request) {
        return messages(userId, request.getId());
    }

    private ConversationEntity requireConversation(Long userId, Long conversationId) {
        ConversationEntity entity = conversationMapper.findByIdAndUserId(conversationId, userId);
        if (entity == null) {
            throw new BizException("会话不存在");
        }
        return entity;
    }

    private ConversationVO toVo(ConversationEntity entity) {
        ConversationVO vo = new ConversationVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setMessageCount(entity.getMessageCount());
        vo.setLastMessageAt(entity.getLastMessageAt());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private ConversationMessageVO toMessageVo(ConversationMessageEntity entity) {
        ConversationMessageVO vo = new ConversationMessageVO();
        vo.setId(entity.getId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setTokenUsage(entity.getTokenUsage());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
