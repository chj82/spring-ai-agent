package com.example.agent.mapper;

import com.example.agent.common.request.PageRequest;
import com.example.agent.model.dto.UserQueryRequest;
import com.example.agent.model.entity.AppUserEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserMapper {

    AppUserEntity findByUsername(String username);

    AppUserEntity findById(Long id);

    List<AppUserEntity> findAll();

    List<AppUserEntity> findPage(@Param("query") UserQueryRequest query, @Param("page") PageRequest pageRequest);

    long countPage(@Param("query") UserQueryRequest query);

    int insert(AppUserEntity entity);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname,
                       @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled,
                      @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash,
                       @Param("updateBy") Long updateBy, @Param("updateByName") String updateByName);

    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
