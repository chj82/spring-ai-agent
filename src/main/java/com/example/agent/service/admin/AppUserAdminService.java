package com.example.agent.service.admin;

import com.example.agent.common.context.LoginUser;
import com.example.agent.common.request.PageRequest;
import com.example.agent.common.response.PageResult;
import com.example.agent.common.exception.BizException;
import com.example.agent.mapper.AppUserMapper;
import com.example.agent.model.dto.CreateAppUserRequest;
import com.example.agent.model.dto.ResetPasswordRequest;
import com.example.agent.model.dto.UpdateAppUserRequest;
import com.example.agent.model.dto.UpdateUserEnabledRequest;
import com.example.agent.model.dto.UserQueryRequest;
import com.example.agent.model.entity.AppUserEntity;
import com.example.agent.model.enums.ActorType;
import com.example.agent.model.vo.UserVO;
import com.example.agent.security.LoginCryptoService;
import com.example.agent.security.UserSessionService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserAdminService {

    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginCryptoService loginCryptoService;
    private final UserSessionService userSessionService;

    public AppUserAdminService(AppUserMapper appUserMapper, PasswordEncoder passwordEncoder,
                               LoginCryptoService loginCryptoService,
                               UserSessionService userSessionService) {
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginCryptoService = loginCryptoService;
        this.userSessionService = userSessionService;
    }

    public PageResult<UserVO> list(UserQueryRequest query, PageRequest pageRequest) {
        List<UserVO> records = appUserMapper.findPage(query, pageRequest).stream().map(this::toVo).toList();
        long total = appUserMapper.countPage(query);
        return PageResult.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    public List<UserVO> list() {
        return appUserMapper.findAll().stream().map(this::toVo).toList();
    }

    public UserVO create(LoginUser operator, CreateAppUserRequest request) {
        if (appUserMapper.findByUsername(request.getUsername()) != null) {
            throw new BizException("前端客户用户名已存在");
        }
        String plainPassword = decodeAndValidatePassword(request.getEncryptedPassword());
        AppUserEntity entity = new AppUserEntity();
        entity.setUsername(request.getUsername());
        entity.setNickname(request.getNickname());
        entity.setPasswordHash(passwordEncoder.encode(plainPassword));
        entity.setEnabled(Boolean.TRUE);
        entity.setCreateBy(operator.getUserId());
        entity.setUpdateBy(operator.getUserId());
        entity.setCreateByName(resolveOperatorName(operator));
        entity.setUpdateByName(resolveOperatorName(operator));
        entity.setDeleted(Boolean.FALSE);
        appUserMapper.insert(entity);
        return toVo(appUserMapper.findById(entity.getId()));
    }

    public UserVO update(LoginUser operator, UpdateAppUserRequest request) {
        ensureExists(request.getId());
        appUserMapper.updateNickname(request.getId(), request.getNickname(), operator.getUserId(), resolveOperatorName(operator));
        return toVo(appUserMapper.findById(request.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(LoginUser operator, UpdateUserEnabledRequest request) {
        ensureExists(request.getId());
        appUserMapper.updateEnabled(request.getId(), request.getEnabled(), operator.getUserId(), resolveOperatorName(operator));
        if (Boolean.FALSE.equals(request.getEnabled())) {
            userSessionService.revokeUserSessions(ActorType.APP, request.getId());
        }
    }

    public void resetPassword(LoginUser operator, ResetPasswordRequest request) {
        ensureExists(request.getId());
        String plainPassword = decodeAndValidatePassword(request.getEncryptedPassword());
        appUserMapper.updatePassword(request.getId(), passwordEncoder.encode(plainPassword),
                operator.getUserId(), resolveOperatorName(operator));
    }

    private void ensureExists(Long id) {
        requireExistingUser(id);
    }

    private AppUserEntity requireExistingUser(Long id) {
        AppUserEntity entity = appUserMapper.findById(id);
        if (entity == null) {
            throw new BizException("前端客户不存在");
        }
        return entity;
    }

    private UserVO toVo(AppUserEntity entity) {
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEnabled(entity.getEnabled());
        vo.setLastLoginTime(entity.getLastLoginTime());
        vo.setCreateBy(entity.getCreateBy());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setCreateByName(entity.getCreateByName());
        vo.setUpdateByName(entity.getUpdateByName());
        vo.setDeleted(entity.getDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private String resolveOperatorName(LoginUser loginUser) {
        if (loginUser.getNickname() != null && !loginUser.getNickname().isBlank()) {
            return loginUser.getNickname();
        }
        return loginUser.getUsername();
    }

    private String decodeAndValidatePassword(String encryptedPassword) {
        String plainPassword = loginCryptoService.decryptPassword(encryptedPassword);
        if (plainPassword.length() < 6 || plainPassword.length() > 32) {
            throw new BizException(400, "密码长度必须为6-32位");
        }
        return plainPassword;
    }
}
