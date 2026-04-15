package com.example.agent.mapper;

import com.example.agent.model.entity.ConversationMessageEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConversationMessageMapper {

    int insert(ConversationMessageEntity entity);

    List<ConversationMessageEntity> findByConversationId(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId
    );

    List<ConversationMessageEntity> findRecentMessages(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId,
        @Param("limit") Integer limit
    );
}
