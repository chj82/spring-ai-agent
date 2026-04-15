package com.example.agent.mapper;

import com.example.agent.model.entity.UserSessionEntity;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserSessionMapper {

    int insert(UserSessionEntity entity);

    UserSessionEntity findBySessionId(String sessionId);

    int revokeBySessionId(String sessionId);

    int revokeByActorAndUserId(@Param("actorType") String actorType, @Param("userId") Long userId);

    int updateActivity(
        @Param("sessionId") String sessionId,
        @Param("lastActiveAt") LocalDateTime lastActiveAt
    );

    int renewActivity(
        @Param("sessionId") String sessionId,
        @Param("lastActiveAt") LocalDateTime lastActiveAt,
        @Param("expiresAt") LocalDateTime expiresAt
    );
}
