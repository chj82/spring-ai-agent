package com.example.agent.model.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    /** 登录账号 */
    @NotBlank
    private String username;

    /** 使用前端公钥加密后的密码密文 */
    @NotBlank
    private String encryptedPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
}
