package com.example.agent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateConversationTitleRequest {

    /** 会话ID */
    @NotNull
    private Long id;
    /** 会话标题 */
    @NotBlank
    @Size(max = 255)
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
