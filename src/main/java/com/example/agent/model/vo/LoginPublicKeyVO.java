package com.example.agent.model.vo;

public class LoginPublicKeyVO {

    /** 加密算法 */
    private String algorithm;
    /** 公钥内容，PEM 格式 */
    private String publicKey;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
