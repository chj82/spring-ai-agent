package com.example.agent.model.vo;

public class ChatReplyVO {

    /** 会话ID */
    private Long conversationId;
    /** 助手消息ID */
    private Long assistantMessageId;
    /** 助手回复内容 */
    private String reply;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getAssistantMessageId() {
        return assistantMessageId;
    }

    public void setAssistantMessageId(Long assistantMessageId) {
        this.assistantMessageId = assistantMessageId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
