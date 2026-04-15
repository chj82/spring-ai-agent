package com.example.agent.security;

import com.example.agent.common.config.AppSecurityProperties;
import com.example.agent.common.context.LoginUser;
import com.example.agent.common.exception.BizException;
import com.example.agent.common.utils.TokenUtils;
import com.example.agent.mapper.AdminUserMapper;
import com.example.agent.mapper.AppUserMapper;
import com.example.agent.mapper.UserSessionMapper;
import com.example.agent.model.entity.AdminUserEntity;
import com.example.agent.model.entity.AppUserEntity;
import com.example.agent.model.entity.UserSessionEntity;
import com.example.agent.model.enums.ActorType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {

    private final UserSessionMapper userSessionMapper;
    private final AppSecurityProperties properties;
    private final AdminUserMapper adminUserMapper;
    private final AppUserMapper appUserMapper;

    public UserSessionService(
        UserSessionMapper userSessionMapper,
        AppSecurityProperties properties,
        AdminUserMapper adminUserMapper,
        AppUserMapper appUserMapper
    ) {
        this.userSessionMapper = userSessionMapper;
        this.properties = properties;
        this.adminUserMapper = adminUserMapper;
        this.appUserMapper = appUserMapper;
    }

    public String createSession(ActorType actorType, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UserSessionEntity entity = new UserSessionEntity();
        entity.setSessionId(TokenUtils.generateToken());
        entity.setActorType(actorType.name());
        entity.setUserId(userId);
        entity.setExpiresAt(now.plusSeconds(properties.getTokenValiditySeconds()));
        entity.setLastActiveAt(now);
        entity.setRevoked(Boolean.FALSE);
        entity.setDeleted(Boolean.FALSE);
        userSessionMapper.insert(entity);
        return entity.getSessionId();
    }

    public UserSessionEntity requireValidToken(String token) {
        UserSessionEntity session = userSessionMapper.findBySessionId(token);
        if (session == null || Boolean.TRUE.equals(session.getRevoked())) {
            throw new BizException(401, "登录已失效");
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(401, "登录已过期");
        }
        return session;
    }

    public boolean shouldRenew(UserSessionEntity session) {
        return session.getExpiresAt().minusSeconds(properties.getRenewThresholdSeconds()).isBefore(LocalDateTime.now());
    }

    public void touchSession(String sessionId, boolean renew) {
        LocalDateTime now = LocalDateTime.now();
        if (renew) {
            userSessionMapper.renewActivity(sessionId, now, now.plusSeconds(properties.getTokenValiditySeconds()));
            return;
        }
        userSessionMapper.updateActivity(sessionId, now);
    }

    public void revokeSession(String sessionId) {
        userSessionMapper.revokeBySessionId(sessionId);
    }

    public void revokeUserSessions(ActorType actorType, Long userId) {
        userSessionMapper.revokeByActorAndUserId(actorType.name(), userId);
    }

    public LoginUser loadLoginUser(UserSessionEntity session) {
        ActorType actorType = ActorType.valueOf(session.getActorType());
        if (actorType == ActorType.ADMIN) {
            AdminUserEntity user = adminUserMapper.findById(session.getUserId());
            if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
                throw new BizException(401, "账号已失效");
            }
            return new LoginUser(
                session.getSessionId(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                ActorType.ADMIN,
                Boolean.TRUE.equals(user.getSuperAdmin())
            );
        }

        AppUserEntity user = appUserMapper.findById(session.getUserId());
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            throw new BizException(401, "账号已失效");
        }
        return new LoginUser(
            session.getSessionId(),
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            ActorType.APP,
            false
        );
    }
}
