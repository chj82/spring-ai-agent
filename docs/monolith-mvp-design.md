# 单体版 AI 助手 MVP 设计

## 1. 目标范围

本阶段只实现以下能力：

1. 用户体系
2. 后台用户管理
3. 客户端账号密码登录
4. Cookie 存储 token 的会话管理
5. 用户侧会话管理
6. AI 大模型对话
7. 会话记忆与会话压缩

暂不实现：

- Tool
- Skill
- MCP
- RAG
- 多助手复杂编排
- 多租户

## 2. 总体设计原则

采用单体项目，后端统一使用一个 Spring Boot 服务，前端分为两个 Next.js 应用或一个 Next.js 应用的两套路由。

本期先把最小闭环做稳定：

- 用户可以登录
- 用户可以创建和管理会话
- 用户可以发起多轮对话
- 模型能够带上下文回复
- 长会话能够自动压缩，避免上下文无限增长

## 3. 技术栈建议

### 后端

- `Spring Boot 3.x`
- `Spring Security`
- `Spring AI Alibaba`
- `MyBatis`
- `MySQL 8.x`
- `Redis`
- `jjwt` 或同类 JWT 组件

### 前端

- `Next.js`
- `React`
- `TypeScript`
- `Ant Design`

## 4. 单体项目结构

建议先做成一个后端单体：

```text
spring-ai-agent-server
├── controller
│   ├── admin
│   │   ├── auth
│   │   ├── adminuser
│   │   └── appuser
│   └── app
│       ├── auth
│       ├── chat
│       └── conversation
├── service
├── manager
├── mapper
├── model
│   ├── entity
│   ├── dto
│   ├── vo
│   └── enum
├── security
├── config
├── ai
│   ├── orchestrator
│   ├── memory
│   ├── prompt
│   └── model
└── common
```

其中职责如下：

- `controller.admin.auth`
  - 后台登录、登出、当前后台用户信息
- `controller.admin.adminuser`
  - 后台用户管理
- `controller.admin.appuser`
  - 前端客户管理
- `controller.app.auth`
  - 前端客户登录、登出、当前客户信息
- `controller.app.conversation`
  - 用户侧会话管理
- `controller.app.chat`
  - 用户侧聊天
- `ai.orchestrator`
  - 聊天主链路编排
- `ai.memory`
  - 官方 ChatMemory 接入、上下文组装、记忆重建
- `security`
  - JWT、Cookie、认证拦截

## 5. 核心业务模块

### 5.1 认证与会话模块

本期建议采用：

- 前端账号密码登录
- 后端签发 JWT
- JWT 写入 HttpOnly Cookie
- Cookie 有效期 1 天
- 活跃自动续期 1 天

#### 为什么这样做

- 比浏览器本地存储更安全
- 前后端分离场景也容易落地
- 后续支持后台和用户端统一鉴权

#### 认证建议

- Access Token 有效期：`24 小时`
- 每次用户请求若 token 剩余时间小于 `12 小时`，自动续期到未来 `24 小时`
- Cookie 配置：
  - `HttpOnly = true`
  - `SameSite = Lax`
  - 生产环境 `Secure = true`

#### 自动续期策略

实现一个认证过滤器：

1. 从 Cookie 读取 token
2. 校验 token 有效性
3. 解析用户信息
4. 若 token 临近过期，则重新签发并回写 Cookie

### 5.2 用户管理模块

本期用户体系明确拆成两套账号，不放在同一张表：

- 后台管理用户
- 前端客户用户

这样拆分的好处：

- 后台和前台登录入口天然隔离
- 后台账号权限模型更清晰
- 前端客户字段可按业务独立扩展
- 避免后台用户和客户账号混在一起带来脏数据和权限风险

#### 后台管理用户

用途：

- 登录后台管理端
- 管理前端客户
- 后续管理助手、工具、Skill、MCP

本期后台用户功能：

- 后台账号密码登录
- 后台用户列表
- 创建后台用户
- 编辑后台用户
- 启用/禁用后台用户
- 重置后台用户密码

#### 前端客户用户

用途：

- 登录前端聊天系统
- 创建和管理自己的会话
- 与 AI 助手对话

本期前端客户功能：

- 账号密码登录
- 查看个人信息
- 使用聊天与会话功能

#### 默认初始化后台管理员

系统初始化时需要自动写入一个默认后台管理员账号：

- 用户名：`admin`
- 密码：`123456`

注意：

- 数据库存储必须是 `BCrypt` 哈希后的密码
- 该账号仅用于系统初始化
- 生产环境首次部署后应强制修改默认密码

### 5.3 会话管理模块

用户侧能力：

- 新建会话
- 删除会话
- 修改会话标题
- 查看会话列表
- 查看会话消息记录
- 基于某个会话进行多轮对话

关键点：

- 会话只属于某个用户
- 删除会话默认做软删除
- 标题支持手动修改
- 首轮对话后可自动生成默认标题

### 5.4 AI 对话模块

本期只做基础模型对话：

- 用户发消息
- 系统读取官方记忆并组装上下文
- 调用大模型
- 返回回复
- 保存消息

本期不接 Tool 和 Skill，所以编排会更简单。

### 5.5 双轨会话记忆设计

这一期采用“双轨会话”方案：

1. 业务原始会话
2. 官方运行时记忆

#### 业务原始会话

这套数据由我们自己维护，作为正式业务数据源，作用是：

- 前端展示会话列表
- 展示完整历史消息
- 支持删除会话
- 支持修改标题
- 支持后台审计和管理

对应表：

- `ai_conversation`
- `ai_conversation_message`

#### 官方运行时记忆

这套能力交给 Spring AI / Spring AI Alibaba 官方实现，作用是：

- 给大模型提供上下文窗口
- 控制最近消息数量
- 利用官方的记忆窗口机制
- 后续平滑演进到 Agent Framework

推荐使用：

- Spring AI ChatMemory
- 基于官方 Memory 的窗口记忆
- 会话 ID 绑定业务 `conversationId`

#### 两套数据的边界

- 业务原始会话是“完整真相”
- 官方运行时记忆是“模型当前可见上下文”
- 原始会话永远不依赖官方记忆反查
- 官方记忆可以丢失后按业务原始会话重建

这样设计的好处：

- 用户功能稳定，不受底层记忆机制变化影响
- 模型上下文交给官方机制维护，减少自研复杂度
- 后续接流程编排、暂停恢复时更容易兼容 Spring AI Alibaba
### 5.6 记忆与压缩模块

长会话不能无限传历史消息，因此要做上下文控制。

本期建议分两层看：

1. 业务原始会话层
2. 官方运行时记忆层

#### 业务原始会话层

- 永久保存用户和 assistant 的原始消息
- 作为客户端历史展示的唯一来源

#### 官方运行时记忆层

- 使用 Spring AI 官方 ChatMemory 维护模型上下文
- 保留最近若干轮消息给模型
- 压缩、窗口裁剪优先依赖官方记忆机制

推荐策略：

- 业务库中永远保存完整消息
- 官方记忆中保留最近 `12` 条消息
- 官方记忆不足或失效时，可从业务原始消息重建

## 6. 数据库表设计

本期建议至少建 4 张核心表。

### 6.1 后台用户表 `sys_admin_user`

```sql
CREATE TABLE `sys_admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '后台登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `is_super_admin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超级管理员:1是,0否',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理用户表';
```

字段说明：

- `username`
  - 后台唯一登录账号
- `password_hash`
  - 使用 `BCrypt`
- `is_super_admin`
  - 第一版可以先用它控制默认管理元账号
- `status`
  - 禁用后不能登录后台

### 6.2 前端客户表 `sys_app_user`

```sql
CREATE TABLE `sys_app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '前端登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用,0禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端客户用户表';
```

字段说明：

- `username`
  - 前端客户唯一登录账号
- `password_hash`
  - 使用 `BCrypt`
- `status`
  - 禁用后不能登录前端

### 6.3 会话表 `ai_conversation`

```sql
CREATE TABLE `ai_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '会话标题',
  `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息数',
  `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否,1是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_last_message_at` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话表';
```

字段说明：

- `message_count`
  - 用于快速统计会话消息数量
- `last_message_at`
  - 用于会话列表按最近活跃时间排序
- `deleted`
  - 软删除

### 6.4 消息表 `ai_conversation_message`

```sql
CREATE TABLE `ai_conversation_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `role` VARCHAR(32) NOT NULL COMMENT '角色:user/assistant/system',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `token_usage` INT DEFAULT NULL COMMENT 'token消耗',
  `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';
```

字段说明：

- `role`
  - 当前阶段只需要 `user / assistant / system`
- `token_usage`
  - 可选，用于后面分析模型消耗

## 7. 登录与 Cookie 会话方案

### 7.1 登录流程

本期需要两套登录流程：

- 后台登录
- 前端客户登录

#### 后台登录流程

1. 管理员输入账号密码
2. 调用 `POST /api/admin/auth/login`
3. 后端校验 `sys_admin_user`
4. 生成后台 JWT
5. 将后台 JWT 写入 Cookie
6. 跳转后台首页

#### 前端客户登录流程

1. 客户输入账号密码
2. 调用 `POST /api/app/auth/login`
3. 后端校验 `sys_app_user`
4. 生成前端 JWT
5. 将前端 JWT 写入 Cookie
6. 跳转聊天页

### 7.2 Cookie 建议

建议后台和前端客户使用不同 Cookie，避免串号：

- 后台 Cookie：`AI_AGENT_ADMIN_TOKEN`
- 前端 Cookie：`AI_AGENT_APP_TOKEN`
- 路径：`/`
- `HttpOnly = true`
- `SameSite = Lax`
- `Max-Age = 86400`

### 7.3 活跃续期

在认证拦截器中执行：

- 若 token 剩余有效期小于 `12 小时`
- 重新生成新 token
- 重写 Cookie，过期时间再延长 `24 小时`

### 7.4 登出流程

1. 后端清除 Cookie
2. 前端跳转登录页

## 8. 后端接口设计

### 8.1 认证接口

#### `POST /api/app/auth/login`

请求：

```json
{
  "username": "test",
  "password": "123456"
}
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "test",
    "nickname": "测试用户",
    "roleCode": "USER"
  }
}
```

#### `POST /api/app/auth/logout`

作用：

- 清除 Cookie

#### `GET /api/app/auth/me`

作用：

- 获取当前登录用户信息

#### `POST /api/admin/auth/login`

作用：

- 后台账号密码登录

#### `POST /api/admin/auth/logout`

作用：

- 清除后台 Cookie

#### `GET /api/admin/auth/me`

作用：

- 获取当前后台登录用户信息

### 8.2 管理后台用户接口

#### `GET /api/admin/admin-users`

- 后台用户分页列表

#### `POST /api/admin/admin-users`

- 创建后台用户

#### `PUT /api/admin/admin-users/{id}`

- 修改后台用户信息

#### `PUT /api/admin/admin-users/{id}/status`

- 启用/禁用后台用户

#### `PUT /api/admin/admin-users/{id}/reset-password`

- 重置后台用户密码

### 8.3 管理前端客户接口

#### `GET /api/admin/app-users`

- 前端客户分页列表

#### `POST /api/admin/app-users`

- 创建前端客户

#### `PUT /api/admin/app-users/{id}`

- 修改前端客户信息

#### `PUT /api/admin/app-users/{id}/status`

- 启用/禁用前端客户

#### `PUT /api/admin/app-users/{id}/reset-password`

- 重置前端客户密码

### 8.4 用户侧会话接口

#### `GET /api/conversations`

- 查询当前用户会话列表

#### `POST /api/conversations`

- 新建会话

请求：

```json
{
  "title": "新的对话"
}
```

#### `PUT /api/conversations/{id}/title`

- 修改会话标题

#### `DELETE /api/conversations/{id}`

- 删除会话

#### `GET /api/conversations/{id}`

- 查询会话详情

#### `GET /api/conversations/{id}/messages`

- 查询会话消息记录

### 8.5 聊天接口

#### `POST /api/chat/send`

请求：

```json
{
  "conversationId": 1001,
  "message": "帮我总结一下今天的工作"
}
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversationId": 1001,
    "assistantMessageId": 2002,
    "reply": "今天你的工作重点可以概括为..."
  }
}
```

## 9. 聊天主链路设计

建议抽象统一入口：

```java
public interface ChatOrchestrator {
    ChatReply chat(Long userId, ChatRequest request);
}
```

一次请求的处理过程：

1. 校验当前前端客户登录状态
2. 校验 `conversationId` 是否属于当前用户
3. 保存用户原始消息到业务库
4. 将用户消息写入官方 ChatMemory
5. 按 `conversationId` 从官方 ChatMemory 获取模型上下文
6. 必要时从业务原始会话重建官方记忆
7. 调用大模型
8. 保存 assistant 原始消息到业务库
9. 将 assistant 消息写回官方 ChatMemory
10. 更新会话信息
11. 按策略执行官方记忆重建
12. 返回结果

## 10. Prompt 组装策略

本期先简化，不引入 Skill。

### 10.1 Prompt 组成

- system prompt
- official chat memory messages
- current user message

### 10.2 system prompt 示例

```text
你是一个专业、友好、简洁的 AI 助手。
请基于已有会话上下文回答用户问题。
```

### 10.3 最近消息窗口

建议：

- 优先由官方 ChatMemory 维护最近 `12` 条消息
- 若需要重建，则从业务原始会话读取最近消息重放

## 11. 会话压缩策略

### 11.1 为什么需要压缩

如果每次都把完整历史消息传给模型：

- token 成本会越来越高
- 响应变慢
- 容易超出上下文窗口

### 11.2 本期压缩思路

优先使用官方记忆窗口能力做模型上下文控制。

### 11.3 建议触发条件

当满足任一条件时执行官方记忆重建：

- 消息条数超过 `20`
- 官方记忆缺失
- 会话切换后需要恢复上下文

### 11.4 实现方式

做两个服务：

- `OfficialChatMemoryService`
  - 对接 Spring AI 官方 ChatMemory

处理原则：

- 业务数据库永远保存完整消息
- 官方记忆只维护模型需要的上下文窗口
- 官方记忆失效时，可以从业务消息重建

## 12. Spring AI Alibaba 接入建议

建议封装一个模型调用服务，而不是在业务代码里直接写死。

例如：

```java
public interface AiChatService {
    AiChatResult chat(List<ChatMessage> messages);
}
```

后续好处：

- 切模型更方便
- 参数统一管理
- 能够补充日志、重试、耗时统计

本期再增加一个建议：

```java
public interface OfficialChatMemoryService {
    void addUserMessage(Long conversationId, String content);
    void addAssistantMessage(Long conversationId, String content);
    List<ChatMessage> getMessages(Long conversationId);
    void rebuildFromConversation(Long conversationId);
}
```

这里的 `conversationId` 直接映射为官方记忆的会话标识，避免维护两套无关 ID。

## 13. 前端页面设计

### 13.1 用户端

- `/login`
  - 账号密码登录
- `/chat`
  - 左侧会话列表
  - 右侧聊天窗口
  - 支持新建会话
  - 支持重命名会话
  - 支持删除会话
  - 支持历史消息展示

### 13.2 后台

- `/admin/login`
- `/admin/admin-users`
- `/admin/app-users`

本期后台优先做两类账号管理：

- 后台管理用户管理
- 前端客户用户管理

## 14. 权限控制建议

### 14.1 用户端接口

要求：

- 必须登录
- 只能访问自己的会话和消息

### 14.2 管理端接口

要求：

- 必须登录后台账号
- 必须是后台管理员

## 15. 推荐开发顺序

### 第一步：数据库和基础工程

- 建表
- 配置 MyBatis
- 配置 Spring Security
- 配置 JWT 与 Cookie

### 第二步：用户与登录

- 后台管理员初始化
- 后台登录接口
- 前端客户登录接口
- 后台用户管理接口
- 前端客户管理接口
- 当前用户接口
- 登出接口

### 第三步：会话管理

- 新建会话
- 会话列表
- 删除会话
- 修改标题
- 消息历史查询

### 第四步：AI 对话

- 接入 Spring AI Alibaba
- 接入官方 ChatMemory
- 跑通基础多轮对话
- 保存消息

### 第五步：记忆与压缩

- 接入官方记忆窗口
- 实现官方记忆重建逻辑

## 16. 第一版必须注意的坑

### 16.1 不要把 JWT 放在 localStorage

这一版既然明确使用 Cookie，就不要混用两套方案。

### 16.2 不要把聊天编排逻辑写在 Controller

要收口到 `ChatOrchestrator`。

### 16.3 不要把后台账号和前端客户放一张表

这会让登录、权限和后续扩展都越来越混乱。

### 16.4 不要删除原始历史消息

压缩只是为了控制模型上下文，不是做数据裁剪。

### 16.5 不要把官方 ChatMemory 当成业务历史库

官方记忆用于模型上下文，客户端历史展示必须来自业务表。

## 17. 这一期完成后的系统边界

本期完成后，你会得到一个可用的 AI 助手基础平台：

- 有双账号体系
- 有后台用户管理
- 有前端客户管理
- 有登录认证
- 有聊天会话管理
- 有多轮上下文对话
- 有业务原始会话
- 有官方运行时记忆
- 有基础记忆窗口与重建能力

在这个基础上，下一期再接：

- Tool
- Skill
- MCP
- RAG
