package com.example.agent.controller.admin;

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
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ApiResponse.success(authService.adminLogin(request, response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        LoginUser loginUser = UserContext.getRequiredAdminUser();
        authService.adminLogout(loginUser.getToken(), response);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserVO> me() {
        LoginUser loginUser = UserContext.getRequiredAdminUser();
        LoginUserVO vo = new LoginUserVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setNickname(loginUser.getNickname());
        vo.setActorType(loginUser.getActorType().name());
        vo.setSuperAdmin(loginUser.isSuperAdmin());
        return ApiResponse.success(vo);
    }
}
