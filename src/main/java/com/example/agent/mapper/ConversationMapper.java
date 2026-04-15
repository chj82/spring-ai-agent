package com.example.agent.mapper;

import com.example.agent.model.entity.ConversationEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConversationMapper {

    int insert(ConversationEntity entity);

    ConversationEntity findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<ConversationEntity> findByUserId(@Param("userId") Long userId);

    int updateTitle(@Param("id") Long id, @Param("userId") Long userId, @Param("title") String title);

    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    int updateStats(
        @Param("id") Long id,
        @Param("userId") Long userId,
        @Param("delta") Integer delta,
        @Param("lastMessageAt") LocalDateTime lastMessageAt
    );
}
