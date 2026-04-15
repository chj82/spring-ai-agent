package com.example.agent.service.admin;

import com.example.agent.common.context.LoginUser;
import com.example.agent.common.exception.BizException;
import com.example.agent.common.request.PageRequest;
import com.example.agent.common.response.PageResult;
import com.example.agent.mapper.AdminUserMapper;
import com.example.agent.model.dto.CreateAdminUserRequest;
import com.example.agent.model.dto.ResetPasswordRequest;
import com.example.agent.model.dto.UpdateAdminUserRequest;
import com.example.agent.model.dto.UpdateUserEnabledRequest;
import com.example.agent.model.dto.UserQueryRequest;
import com.example.agent.model.entity.AdminUserEntity;
import com.example.agent.model.enums.ActorType;
import com.example.agent.model.vo.UserVO;
import com.example.agent.security.LoginCryptoService;
import com.example.agent.security.UserSessionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginCryptoService loginCryptoService;
    private final UserSessionService userSessionService;

    public AdminUserService(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder,
                            LoginCryptoService loginCryptoService,
                            UserSessionService userSessionService) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginCryptoService = loginCryptoService;
        this.userSessionService = userSessionService;
    }

    public PageResult<UserVO> list(UserQueryRequest query, PageRequest pageRequest) {
        List<UserVO> records = adminUserMapper.findPage(query, pageRequest).stream().map(this::toVo).toList();
        long total = adminUserMapper.countPage(query);
        return PageResult.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    public UserVO create(LoginUser operator, CreateAdminUserRequest request) {
        if (adminUserMapper.findByUsername(request.getUsername()) != null) {
            throw new BizException("后台用户名已存在");
        }
        String plainPassword = decodeAndValidatePassword(request.getEncryptedPassword());
        AdminUserEntity entity = new AdminUserEntity();
        entity.setUsername(request.getUsername());
        entity.setNickname(request.getNickname());
        entity.setPasswordHash(passwordEncoder.encode(plainPassword));
        entity.setEnabled(Boolean.TRUE);
        entity.setSuperAdmin(Boolean.FALSE);
        entity.setCreateBy(operator.getUserId());
        entity.setUpdateBy(operator.getUserId());
        entity.setCreateByName(resolveOperatorName(operator));
        entity.setUpdateByName(resolveOperatorName(operator));
        entity.setDeleted(Boolean.FALSE);
        adminUserMapper.insert(entity);
        return toVo(adminUserMapper.findById(entity.getId()));
    }

    public UserVO update(LoginUser operator, Long id, UpdateAdminUserRequest request) {
        ensureExists(id);
        adminUserMapper.updateNickname(id, request.getNickname(), operator.getUserId(), resolveOperatorName(operator));
        return toVo(adminUserMapper.findById(id));
    }

    public UserVO update(LoginUser operator, UpdateAdminUserRequest request) {
        return update(operator, request.getId(), request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(LoginUser operator, Long id, UpdateUserEnabledRequest request) {
        AdminUserEntity user = requireExistingUser(id);
        if (Boolean.FALSE.equals(request.getEnabled()) && Boolean.TRUE.equals(user.getSuperAdmin())) {
            throw new BizException("超级管理员不允许禁用");
        }
        adminUserMapper.updateEnabled(id, request.getEnabled(), operator.getUserId(), resolveOperatorName(operator));
        if (Boolean.FALSE.equals(request.getEnabled())) {
            userSessionService.revokeUserSessions(ActorType.ADMIN, id);
        }
    }

    public void updateEnabled(LoginUser operator, UpdateUserEnabledRequest request) {
        updateEnabled(operator, request.getId(), request);
    }

    public void resetPassword(LoginUser operator, Long id, ResetPasswordRequest request) {
        ensureExists(id);
        String plainPassword = decodeAndValidatePassword(request.getEncryptedPassword());
        adminUserMapper.updatePassword(id, passwordEncoder.encode(plainPassword),
                operator.getUserId(), resolveOperatorName(operator));
    }

    public void resetPassword(LoginUser operator, ResetPasswordRequest request) {
        resetPassword(operator, request.getId(), request);
    }

    private void ensureExists(Long id) {
        requireExistingUser(id);
    }

    private AdminUserEntity requireExistingUser(Long id) {
        AdminUserEntity entity = adminUserMapper.findById(id);
        if (entity == null) {
            throw new BizException("后台用户不存在");
        }
        return entity;
    }

    private UserVO toVo(AdminUserEntity entity) {
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEnabled(entity.getEnabled());
        vo.setSuperAdmin(entity.getSuperAdmin());
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
