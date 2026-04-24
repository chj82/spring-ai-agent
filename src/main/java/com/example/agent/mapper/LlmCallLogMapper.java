package com.example.agent.mapper;

import com.example.agent.model.entity.LlmCallLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * LLM 调用日志 Mapper。
 */
@Mapper
public interface LlmCallLogMapper {

    /** 新增一条调用日志。 */
    int insert(LlmCallLogEntity entity);

    /** 查询某个会话当前最大的调用轮次。 */
    Integer findMaxRoundNoByConversationId(@Param("conversationId") Long conversationId);

    /** 调用成功后更新响应结果。 */
    int updateSuccess(LlmCallLogEntity entity);

    /** 调用失败后更新错误信息。 */
    int updateError(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
