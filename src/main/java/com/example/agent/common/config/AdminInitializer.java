package com.example.agent.common.config;

import com.example.agent.mapper.AdminUserMapper;
import com.example.agent.model.entity.AdminUserEntity;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class AdminInitializer {

    @Bean
    public ApplicationRunner adminUserApplicationRunner(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder) {
        return args -> {
            AdminUserEntity superAdmin = adminUserMapper.findSuperAdmin();
            if (superAdmin != null) {
                return;
            }
            AdminUserEntity entity = new AdminUserEntity();
            entity.setUsername("admin");
            entity.setNickname("管理员");
            entity.setPasswordHash(passwordEncoder.encode("123456"));
            entity.setEnabled(Boolean.TRUE);
            entity.setSuperAdmin(Boolean.TRUE);
            entity.setLastLoginTime(LocalDateTime.now());
            entity.setCreateBy(0L);
            entity.setUpdateBy(0L);
            entity.setCreateByName("系统初始化");
            entity.setUpdateByName("系统初始化");
            entity.setDeleted(Boolean.FALSE);
            adminUserMapper.insert(entity);
        };
    }
}
