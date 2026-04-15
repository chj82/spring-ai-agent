package com.example.agent.security;

import com.example.agent.common.exception.BizException;
import com.example.agent.model.vo.LoginPublicKeyVO;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.MGF1ParameterSpec;
import org.springframework.stereotype.Service;

@Service
public class LoginCryptoService {

    private static final String ALGORITHM = "RSA-OAEP-256";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP_SHA256_PARAMETER_SPEC = new OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT
    );

    /** RSA 私钥 */
    private PrivateKey privateKey;
    /** PEM 格式公钥 */
    private String publicKeyPem;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKeyPem = buildPem((RSAPublicKey) keyPair.getPublic());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("初始化登录加密密钥失败", ex);
        }
    }

    public LoginPublicKeyVO getLoginPublicKey() {
        LoginPublicKeyVO vo = new LoginPublicKeyVO();
        vo.setAlgorithm(ALGORITHM);
        vo.setPublicKey(publicKeyPem);
        return vo;
    }

    public String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256_PARAMETER_SPEC);
            byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new BizException(400, "密码解密失败");
        }
    }

    private String buildPem(RSAPublicKey publicKey) {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
            .encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }
}
