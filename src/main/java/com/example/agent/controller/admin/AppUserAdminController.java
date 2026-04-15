package com.example.agent.controller.admin;

import com.example.agent.common.context.LoginUser;
import com.example.agent.common.request.PageRequest;
import com.example.agent.common.request.PageRequestUtils;
import com.example.agent.common.context.UserContext;
import com.example.agent.common.response.ApiResponse;
import com.example.agent.common.response.PageResult;
import com.example.agent.model.dto.CreateAppUserRequest;
import com.example.agent.model.dto.ResetPasswordRequest;
import com.example.agent.model.dto.UpdateAppUserRequest;
import com.example.agent.model.dto.UpdateUserEnabledRequest;
import com.example.agent.model.dto.UserQueryRequest;
import com.example.agent.model.vo.UserVO;
import com.example.agent.service.admin.AppUserAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/app-users")
public class AppUserAdminController {

    private final AppUserAdminService appUserAdminService;

    public AppUserAdminController(AppUserAdminService appUserAdminService) {
        this.appUserAdminService = appUserAdminService;
    }

    @PostMapping("/list")
    public ApiResponse<PageResult<UserVO>> list(
            @RequestBody(required = false) UserQueryRequest query,
            @ModelAttribute PageRequest pageRequest) {
        UserContext.getRequiredAdminUser();
        UserQueryRequest actualQuery = query == null ? new UserQueryRequest() : query;
        PageRequest actualPageRequest = PageRequestUtils.normalize(pageRequest);
        return ApiResponse.success(appUserAdminService.list(actualQuery, actualPageRequest));
    }

    @PostMapping("/create")
    public ApiResponse<UserVO> create(@Valid @RequestBody CreateAppUserRequest request) {
        LoginUser operator = UserContext.getRequiredAdminUser();
        return ApiResponse.success(appUserAdminService.create(operator, request));
    }

    @PostMapping("/update")
    public ApiResponse<UserVO> update(@Valid @RequestBody UpdateAppUserRequest request) {
        LoginUser operator = UserContext.getRequiredAdminUser();
        return ApiResponse.success(appUserAdminService.update(operator, request));
    }

    @PostMapping("/update-enabled")
    public ApiResponse<Void> updateEnabled(@Valid @RequestBody UpdateUserEnabledRequest request) {
        LoginUser operator = UserContext.getRequiredAdminUser();
        appUserAdminService.updateEnabled(operator, request);
        return ApiResponse.success();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        LoginUser operator = UserContext.getRequiredAdminUser();
        appUserAdminService.resetPassword(operator, request);
        return ApiResponse.success();
    }
}
