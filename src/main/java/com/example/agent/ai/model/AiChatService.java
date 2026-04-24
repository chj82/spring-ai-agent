package com.example.agent.ai.model;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.agent.common.exception.BizException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatService {

    private final ReactAgent reactAgent;

    public AiChatService(ReactAgent reactAgent) {
        this.reactAgent = reactAgent;
    }

    public Flux<NodeOutput> streamChat(Long userId, Long conversationId, String userMessage) {
        return Flux.defer(() -> {
            try {
                RunnableConfig config = RunnableConfig.builder()
                    .threadId(conversationId.toString())
                    .addMetadata("user_id", userId.toString())
                    .addMetadata("conversation_id", conversationId.toString())
                    .addMetadata("thread_id", conversationId.toString())
                    .build();
                return reactAgent.stream(userMessage, config);
            } catch (Exception exception) {
                return Flux.error(new BizException(500, "模型调用失败: " + exception.getMessage()));
            }
        });
    }
}
