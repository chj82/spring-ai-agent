package com.example.agent.controller.app;

import com.example.agent.common.context.LoginUser;
import com.example.agent.common.context.UserContext;
import com.example.agent.common.response.ApiResponse;
import com.example.agent.model.dto.LoginRequest;
import com.example.agent.model.vo.LoginUserVO;
import com.example.agent.service.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

    private final AuthService authService;

    public AppAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ApiResponse.success(authService.appLogin(request, response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        LoginUser loginUser = UserContext.getRequiredAppUser();
        authService.appLogout(loginUser.getToken(), response);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me() {
        LoginUser loginUser = UserContext.getRequiredAppUser();
        LoginUserVO vo = new LoginUserVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setNickname(loginUser.getNickname());
        vo.setActorType(loginUser.getActorType().name());
        vo.setSuperAdmin(false);
        return ApiResponse.success(vo);
    }
}
