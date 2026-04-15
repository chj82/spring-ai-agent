package com.example.agent.service.auth;

import com.example.agent.common.exception.BizException;
import com.example.agent.mapper.AdminUserMapper;
import com.example.agent.mapper.AppUserMapper;
import com.example.agent.model.dto.LoginRequest;
import com.example.agent.model.entity.AdminUserEntity;
import com.example.agent.model.entity.AppUserEntity;
import com.example.agent.model.enums.ActorType;
import com.example.agent.model.vo.LoginUserVO;
import com.example.agent.security.CookieService;
import com.example.agent.security.LoginCryptoService;
import com.example.agent.security.UserSessionService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminUserMapper adminUserMapper;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final UserSessionService userSessionService;
    private final LoginCryptoService loginCryptoService;

    public AuthService(
        AdminUserMapper adminUserMapper,
        AppUserMapper appUserMapper,
        PasswordEncoder passwordEncoder,
        CookieService cookieService,
        UserSessionService userSessionService,
        LoginCryptoService loginCryptoService
    ) {
        this.adminUserMapper = adminUserMapper;
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.cookieService = cookieService;
        this.userSessionService = userSessionService;
        this.loginCryptoService = loginCryptoService;
    }

    public LoginUserVO adminLogin(LoginRequest request, HttpServletResponse response) {
        String plainPassword = loginCryptoService.decryptPassword(request.getEncryptedPassword());
        AdminUserEntity user = adminUserMapper.findByUsername(request.getUsername());
        if (user == null || Boolean.FALSE.equals(user.getEnabled()) || !passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }
        adminUserMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());
        String token = userSessionService.createSession(ActorType.ADMIN, user.getId());
        cookieService.writeAdminToken(response, token);
        return buildLoginVo(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            ActorType.ADMIN,
            Boolean.TRUE.equals(user.getSuperAdmin())
        );
    }

    public LoginUserVO appLogin(LoginRequest request, HttpServletResponse response) {
        String plainPassword = loginCryptoService.decryptPassword(request.getEncryptedPassword());
        AppUserEntity user = appUserMapper.findByUsername(request.getUsername());
        if (user == null || Boolean.FALSE.equals(user.getEnabled()) || !passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }
        appUserMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());
        String token = userSessionService.createSession(ActorType.APP, user.getId());
        cookieService.writeAppToken(response, token);
        return buildLoginVo(user.getId(), user.getUsername(), user.getNickname(), ActorType.APP, false);
    }

    public void adminLogout(String sessionId, HttpServletResponse response) {
        userSessionService.revokeSession(sessionId);
        cookieService.clearAdminToken(response);
    }

    public void appLogout(String sessionId, HttpServletResponse response) {
        userSessionService.revokeSession(sessionId);
        cookieService.clearAppToken(response);
    }

    private LoginUserVO buildLoginVo(Long userId, String username, String nickname, ActorType actorType, boolean superAdmin) {
        LoginUserVO vo = new LoginUserVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setNickname(nickname);
        vo.setActorType(actorType.name());
        vo.setSuperAdmin(superAdmin);
        return vo;
    }
}
