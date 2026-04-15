package com.example.agent.model.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationIdRequest {

    /** 会话ID */
    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
