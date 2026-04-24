package com.example.agent.service.chat;

import com.example.agent.mapper.LlmCallLogMapper;
import com.example.agent.model.entity.LlmCallLogEntity;
import org.springframework.stereotype.Service;

/**
 * LLM 调用日志服务。
 * 负责生成轮次、创建待完成日志，以及在调用结束后回填结果。
 */
@Service
public class LlmCallLogService {

    private final LlmCallLogMapper llmCallLogMapper;

    public LlmCallLogService(LlmCallLogMapper llmCallLogMapper) {
        this.llmCallLogMapper = llmCallLogMapper;
    }

    /**
     * 创建一条待完成日志。
     * 这里先落一条 PENDING 记录，便于后续在流式场景下异步补全结果。
     */
    public LlmCallLogEntity createPendingLog(LlmCallLogEntity entity) {
        entity.setRoundNo(resolveRoundNo(entity.getConversationId()));
        entity.setStatus("PENDING");
        entity.setDeleted(Boolean.FALSE);
        llmCallLogMapper.insert(entity);
        return entity;
    }

    /** 调用成功后回填模型响应内容与统计信息。 */
    public void markSuccess(LlmCallLogEntity entity) {
        entity.setStatus("SUCCESS");
        llmCallLogMapper.updateSuccess(entity);
    }

    /** 调用异常时只回填错误信息，避免覆盖已经记录的请求参数。 */
    public void markError(Long id, String errorMessage) {
        if (id == null) {
            return;
        }
        llmCallLogMapper.updateError(id, errorMessage);
    }

    /**
     * 轮次按会话维度递增。
     * 没有关联会话时，默认按第 1 轮处理。
     */
    private Integer resolveRoundNo(Long conversationId) {
        if (conversationId == null) {
            return 1;
        }
        Integer maxRoundNo = llmCallLogMapper.findMaxRoundNoByConversationId(conversationId);
        return maxRoundNo == null ? 1 : maxRoundNo + 1;
    }
}
