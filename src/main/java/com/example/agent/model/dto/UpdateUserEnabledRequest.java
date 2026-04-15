package com.example.agent.model.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserEnabledRequest {

    /** 用户ID */
    @NotNull
    private Long id;
    /** 是否启用 */
    @NotNull
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
