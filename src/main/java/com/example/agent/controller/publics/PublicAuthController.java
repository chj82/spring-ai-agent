package com.example.agent.controller.publics;

import com.example.agent.common.response.ApiResponse;
import com.example.agent.model.vo.LoginPublicKeyVO;
import com.example.agent.security.LoginCryptoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
public class PublicAuthController {

    private final LoginCryptoService loginCryptoService;

    public PublicAuthController(LoginCryptoService loginCryptoService) {
        this.loginCryptoService = loginCryptoService;
    }

    @GetMapping("/login-public-key")
    public ApiResponse<LoginPublicKeyVO> loginPublicKey() {
        return ApiResponse.success(loginCryptoService.getLoginPublicKey());
    }
}
