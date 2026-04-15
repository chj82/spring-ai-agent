package com.example.agent.ai.memory;

import com.example.agent.mapper.ConversationMessageMapper;
import com.example.agent.model.entity.ConversationMessageEntity;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class OfficialChatMemoryService {

    private final ChatMemory chatMemory;
    private final ConversationMessageMapper conversationMessageMapper;

    public OfficialChatMemoryService(ChatMemory chatMemory, ConversationMessageMapper conversationMessageMapper) {
        this.chatMemory = chatMemory;
        this.conversationMessageMapper = conversationMessageMapper;
    }

    public void appendUserMessage(Long conversationId, String content) {
        chatMemory.add(conversationId.toString(), List.of(new UserMessage(content)));
    }

    public void appendAssistantMessage(Long conversationId, String content) {
        chatMemory.add(conversationId.toString(), List.of(new AssistantMessage(content)));
    }

    public void rebuildFromConversation(Long conversationId, Long userId) {
        List<ConversationMessageEntity> recentMessages = conversationMessageMapper.findRecentMessages(conversationId, userId, 20);
        chatMemory.clear(conversationId.toString());
        Collections.reverse(recentMessages);
        for (ConversationMessageEntity entity : recentMessages) {
            Message message = "ASSISTANT".equalsIgnoreCase(entity.getRole())
                ? new AssistantMessage(entity.getContent())
                : new UserMessage(entity.getContent());
            chatMemory.add(conversationId.toString(), List.of(message));
        }
    }

    public void clear(Long conversationId) {
        chatMemory.clear(conversationId.toString());
    }
}
