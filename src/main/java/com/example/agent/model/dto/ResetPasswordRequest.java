package com.example.agent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResetPasswordRequest {

    /** 用户ID */
    @NotNull
    private Long id;
    /** 使用前端公钥加密后的新密码密文 */
    @NotBlank
    private String encryptedPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
}
