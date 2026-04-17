package com.example.agent.ai.model;

import com.example.agent.common.exception.BizException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(
        ChatClient.Builder chatClientBuilder,
        ChatMemory chatMemory,
        @Value("${app.chat.default-system-prompt}") String defaultSystemPrompt
    ) {
        this.chatClient = chatClientBuilder
            .defaultSystem(defaultSystemPrompt)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }

    public String chat(Long conversationId, String userMessage) {
        try {
            return chatClient.prompt()
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .user(userMessage)
                .call()
                .content();
        } catch (Exception exception) {
            throw new BizException(500, "模型调用失败: " + exception.getMessage());
        }
    }

    public Flux<String> streamChat(Long conversationId, String userMessage) {
        try {
            return chatClient.prompt()
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .user(userMessage)
                .stream()
                .content();
        } catch (Exception exception) {
            throw new BizException(500, "模型调用失败: " + exception.getMessage());
        }
    }
}
