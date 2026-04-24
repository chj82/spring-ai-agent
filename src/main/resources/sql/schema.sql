CREATE TABLE IF NOT EXISTS `sys_admin_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '后台登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `enabled` bit(1) NOT NULL DEFAULT 0 COMMENT '是否启用:true启用,false禁用',
  `super_admin` bit(1) NOT NULL DEFAULT 0 COMMENT '是否超级管理员:1是,0否',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT(20) DEFAULT NULL COMMENT '修改人ID',
  `create_by_name` VARCHAR(64) DEFAULT NULL COMMENT '创建人名称',
  `update_by_name` VARCHAR(64) DEFAULT NULL COMMENT '修改人名称',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理用户表';

CREATE TABLE IF NOT EXISTS `sys_app_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '前端登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `enabled` bit(1) NOT NULL DEFAULT 0 COMMENT '是否启用:true启用,false禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `update_by` BIGINT(20) DEFAULT NULL COMMENT '修改人ID',
  `create_by_name` VARCHAR(64) DEFAULT NULL COMMENT '创建人名称',
  `update_by_name` VARCHAR(64) DEFAULT NULL COMMENT '修改人名称',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端客户用户表';

CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '会话标题',
  `message_count` INT(11) NOT NULL DEFAULT 0 COMMENT '消息数',
  `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_user_id` (`user_id`),
  KEY `idx_conversation_last_message_at` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';

CREATE TABLE IF NOT EXISTS `ai_conversation_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` BIGINT(20) NOT NULL COMMENT '会话ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
  `role` VARCHAR(32) NOT NULL COMMENT '角色:user/assistant/system',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_conversation_id` (`conversation_id`),
  KEY `idx_message_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';

CREATE TABLE IF NOT EXISTS `ai_llm_call_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` BIGINT(20) DEFAULT NULL COMMENT '会话ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `thread_id` VARCHAR(64) DEFAULT NULL COMMENT 'Agent线程ID',
  `agent_name` VARCHAR(64) NOT NULL COMMENT 'Agent名称',
  `round_no` INT(11) NOT NULL DEFAULT 1 COMMENT '会话内模型调用轮次',
  `status` VARCHAR(32) NOT NULL COMMENT '状态:PENDING/SUCCESS/ERROR',
  `request_payload_json` LONGTEXT NOT NULL COMMENT '请求载荷JSON',
  `response_payload_json` LONGTEXT DEFAULT NULL COMMENT '响应载荷JSON',
  `prompt_tokens` INT(11) DEFAULT NULL COMMENT '提示词tokens',
  `completion_tokens` INT(11) DEFAULT NULL COMMENT '输出tokens',
  `total_tokens` INT(11) DEFAULT NULL COMMENT '总tokens',
  `error_message` LONGTEXT DEFAULT NULL COMMENT '错误信息',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_llm_call_conversation_id` (`conversation_id`),
  KEY `idx_llm_call_user_id` (`user_id`),
  KEY `idx_llm_call_thread_id` (`thread_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM调用日志表';

CREATE TABLE IF NOT EXISTS `sys_user_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话令牌ID',
  `actor_type` VARCHAR(16) NOT NULL COMMENT '账号类型:ADMIN/APP',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `last_active_at` DATETIME NOT NULL COMMENT '最后活跃时间',
  `revoked`  bit(1)  NOT NULL DEFAULT 0 COMMENT '是否已撤销:0否,1是',
  `deleted` bit(1) NOT NULL DEFAULT 0 COMMENT '是否删除:true是,false否',
  `created_at` DATETIME NOT NULL COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_actor_user` (`actor_type`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录会话表';
