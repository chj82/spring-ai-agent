package com.example.agent.common.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.example.agent.ai.model.LlmLoggingModelInterceptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ReactAgent conversationAgent(
        ChatModel chatModel,
        DataSource dataSource,
        LlmLoggingModelInterceptor llmLoggingModelInterceptor,
        @Value("${app.chat.default-system-prompt}") String defaultSystemPrompt
    ) {
        MysqlSaver saver = MysqlSaver.builder()
            .dataSource(dataSource)
            .createOption(CreateOption.CREATE_IF_NOT_EXISTS)
            .build();

        return ReactAgent.builder()
            .name("conversation-agent")
            .model(chatModel)
            .systemPrompt(defaultSystemPrompt)
            .interceptors(llmLoggingModelInterceptor)
            .saver(saver)
            .build();
    }
}
