package com.example.agent.mapper;

import com.example.agent.common.request.PageRequest;
import com.example.agent.model.entity.AdminUserEntity;
import com.example.agent.model.dto.UserQueryRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserMapper {

    AdminUserEntity findByUsername(String username);

    AdminUserEntity findById(Long id);

    AdminUserEntity findSuperAdmin();

    List<AdminUserEntity> findAll();

    List<AdminUserEntity> findPage(@Param("query") UserQueryRequest query, @Param("page") PageRequest pageRequest);

    long countPage(@Param("query") UserQueryRequest query);

    int insert(AdminUserEntity entity);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname,
                       @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled,
                      @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                       @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
