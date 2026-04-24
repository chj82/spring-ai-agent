package com.example.agent.model.entity;

import java.time.LocalDateTime;

/**
 * LLM 调用日志实体。
 * 用于记录一次真实模型调用的请求、响应和统计信息。
 */
public class LlmCallLogEntity {

    /** 主键ID */
    private Long id;
    /** 会话ID */
    private Long conversationId;
    /** 用户ID */
    private Long userId;
    /** Agent 线程ID，通常与会话ID一致 */
    private String threadId;
    /** Agent 名称 */
    private String agentName;
    /** 当前会话内的模型调用轮次 */
    private Integer roundNo;
    /** 调用状态：PENDING/SUCCESS/ERROR */
    private String status;
    /** 请求载荷 JSON */
    private String requestPayloadJson;
    /** 响应载荷 JSON */
    private String responsePayloadJson;
    /** 输入 token 数 */
    private Integer promptTokens;
    /** 输出 token 数 */
    private Integer completionTokens;
    /** 总 token 数 */
    private Integer totalTokens;
    /** 错误信息 */
    private String errorMessage;
    /** 删除标记 */
    private Boolean deleted;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestPayloadJson() {
        return requestPayloadJson;
    }

    public void setRequestPayloadJson(String requestPayloadJson) {
        this.requestPayloadJson = requestPayloadJson;
    }

    public String getResponsePayloadJson() {
        return responsePayloadJson;
    }

    public void setResponsePayloadJson(String responsePayloadJson) {
        this.responsePayloadJson = responsePayloadJson;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
