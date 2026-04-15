# AI 通用智能助手设计方案

## 1. 项目目标

构建一个支持以下能力的通用智能助手平台：

- 用户账号密码登录
- 与 AI 助手进行多轮对话
- 查看历史会话与消息记录
- 支持 MCP 配置与接入
- 支持工具加载与调用
- 支持 Skill 读取与注入
- 支持后台管理用户、Skill、工具、MCP
- 后端采用 `Spring Boot + Spring AI Alibaba + MyBatis + MySQL`
- 前端采用 `Next.js + React`

## 2. 总体架构

建议采用前后端分离 + 单体优先、后续可演进微服务的方式。

### 2.1 架构分层

#### 后端分层

- `portal-api`
  - 面向前台用户端的接口
  - 登录、会话列表、消息发送、历史记录
- `admin-api`
  - 面向管理后台的接口
  - 用户管理、Skill 管理、工具管理、MCP 管理
- `agent-core`
  - Agent 编排核心
  - 会话上下文构建、模型调用、工具执行、Skill 注入、MCP 客户端调度
- `agent-memory`
  - 会话管理、消息持久化、官方记忆接入、上下文窗口控制
- `agent-tool`
  - 工具定义、工具注册、工具鉴权、工具执行
- `agent-skill`
  - Skill 加载、Skill 模板渲染、Skill 版本管理
- `agent-mcp`
  - MCP Server 配置、连接、可用能力发现、调用适配
- `infra`
  - MyBatis、Redis、认证、安全、审计日志、公共组件

#### 前端分层

- `apps/web`
  - 用户端
  - 登录、聊天页、会话列表、历史记录
- `apps/admin`
  - 管理后台
  - 用户、Skill、工具、MCP 的 CRUD 与发布
- `packages/ui`
  - 通用 UI 组件
- `packages/api`
  - 前端接口封装

## 3. 后端核心模块设计

### 3.1 用户与认证模块

功能：

- 后台账号密码登录
- 前端客户账号密码登录
- JWT 或 Session 鉴权
- 用户状态控制
- 角色权限控制

建议：

- 后台管理用户与前端客户用户分表存储
- 后台默认初始化一个管理元账号：`admin / 123456`
- 前端客户账号独立管理
- 密码使用 `BCrypt`
- 登录后签发 `JWT Access Token + Refresh Token`

### 3.2 会话管理模块

功能：

- 创建会话
- 查询会话列表
- 查询消息历史
- 删除/归档会话
- 会话标题自动生成
- 上下文窗口控制

核心设计：

- `conversation` 表存会话元信息
- `conversation_message` 表存消息内容
- 业务会话表保存完整原始消息，供客户端展示与后台审计
- 模型上下文记忆优先对接 Spring AI / Spring AI Alibaba 官方记忆机制
- 支持消息角色：`system/user/assistant/tool`
- 支持消息状态：`pending/success/fail`

### 3.3 Agent 核心编排模块

这是整个系统的关键。

一次对话请求的处理链建议如下：

1. 校验用户身份与会话权限
2. 读取会话上下文
3. 读取当前助手配置
4. 加载绑定的 Skill
5. 加载启用的工具
6. 加载启用的 MCP 连接能力
7. 组装 Prompt
8. 调用模型
9. 如模型触发工具调用，则进入工具执行链
10. 汇总工具结果并继续补全回答
11. 保存 assistant 回复
12. 更新会话标题与官方记忆状态

建议抽象一个统一入口：

- `AgentOrchestrator`

建议内部拆分：

- `ConversationContextService`
- `PromptAssembleService`
- `ToolDispatchService`
- `SkillRenderService`
- `McpCapabilityService`
- `ModelInvokeService`
- `MemoryCompressService`

### 3.4 Skill 模块

Skill 的本质是“可配置的提示词能力包”。

建议支持以下内容：

- 名称
- 标识编码
- 描述
- 系统提示词模板
- 变量定义
- 版本号
- 是否启用

Skill 注入方式建议：

- 作为系统提示词的一部分注入
- 支持绑定到某个助手模板
- 支持多个 Skill 组合装配

例如：

- `customer-service`
- `code-assistant`
- `data-analyst`
- `travel-planner`

### 3.5 工具模块

工具建议统一抽象，不要把 Spring AI 原生 Tool 调用和业务工具直接耦合死。

建议设计：

- 工具元数据：名称、编码、描述、参数 schema、状态
- 工具实现方式：
  - 本地 Java Bean 工具
  - HTTP 工具
  - 脚本型工具
  - MCP 暴露的远端工具

统一接口建议：

```java
public interface AgentToolExecutor {
    ToolResult execute(ToolExecuteRequest request);
}
```

工具注册中心：

- `ToolRegistry`

工具执行器：

- `LocalBeanToolExecutor`
- `HttpToolExecutor`
- `McpToolExecutor`

### 3.6 MCP 模块

MCP 模块建议不要直接写死协议细节在业务层，要做成独立适配层。

功能：

- MCP Server 配置管理
- 连接测试
- 拉取可用 tools/resources/prompts
- 将 MCP 能力映射为平台统一 Tool

核心对象：

- `McpServerConfig`
- `McpServerConnection`
- `McpCapabilityDescriptor`

建议支持配置：

- 名称
- 协议类型
- 地址
- 认证信息
- 超时时间
- 是否启用

### 3.7 助手配置模块

建议引入“助手 Assistant”这一层，不要让用户直接面向模型裸聊。

一个 Assistant 可以绑定：

- 模型
- 默认 system prompt
- 一组 Skill
- 一组 Tool
- 一组 MCP 服务
- 参数配置

这样可以让系统更通用，也方便后台管理。

## 4. 推荐数据库表设计

以下是第一版建议核心表。

### 4.1 用户与权限

- `sys_admin_user`
- `sys_app_user`

`sys_admin_user`

- `id`
- `username`
- `password_hash`
- `nickname`
- `is_super_admin`
- `status`
- `last_login_time`
- `created_at`
- `updated_at`

`sys_app_user`

- `id`
- `username`
- `password_hash`
- `nickname`
- `status`
- `last_login_time`
- `created_at`
- `updated_at`

### 4.2 助手与配置

- `ai_assistant`
- `ai_assistant_skill`
- `ai_assistant_tool`
- `ai_assistant_mcp`

`ai_assistant`

- `id`
- `name`
- `code`
- `description`
- `model_name`
- `system_prompt`
- `temperature`
- `max_tokens`
- `status`
- `created_at`
- `updated_at`

### 4.3 会话与消息

- `ai_conversation`
- `ai_conversation_message`

`ai_conversation`

- `id`
- `user_id`
- `assistant_id`
- `title`
- `status`
- `message_count`
- `last_message_at`
- `created_at`
- `updated_at`

`ai_conversation_message`

- `id`
- `conversation_id`
- `role`
- `message_type`
- `content`
- `tool_name`
- `tool_request`
- `tool_response`
- `token_usage`
- `status`
- `created_at`

### 4.4 Skill

- `ai_skill`
- `ai_skill_version`

`ai_skill`

- `id`
- `name`
- `code`
- `description`
- `current_version`
- `status`
- `created_at`
- `updated_at`

`ai_skill_version`

- `id`
- `skill_id`
- `version`
- `prompt_template`
- `input_schema`
- `status`
- `created_at`

### 4.5 工具

- `ai_tool`
- `ai_tool_binding`

`ai_tool`

- `id`
- `name`
- `code`
- `type`
- `description`
- `schema_json`
- `config_json`
- `status`
- `created_at`
- `updated_at`

### 4.6 MCP

- `ai_mcp_server`
- `ai_mcp_capability`

`ai_mcp_server`

- `id`
- `name`
- `code`
- `base_url`
- `protocol_type`
- `auth_type`
- `auth_config`
- `timeout_ms`
- `status`
- `created_at`
- `updated_at`

`ai_mcp_capability`

- `id`
- `mcp_server_id`
- `capability_type`
- `name`
- `capability_code`
- `schema_json`
- `raw_descriptor`
- `status`
- `updated_at`

## 5. 核心调用链设计

### 5.1 用户发送消息

前端调用：

- `POST /api/chat/send`

请求体：

- `assistantId`
- `conversationId`
- `message`

后端处理：

1. 保存用户消息
2. 拉取最近 N 轮消息
3. 注入 system prompt + skills
4. 注入可用 tools + MCP tools
5. 调用模型
6. 如发生工具调用，执行并回填
7. 生成最终回复
8. 保存 assistant 消息
9. 返回前端

### 5.2 工具调用链

建议增加统一事件日志，便于排障：

- 请求参数
- 执行时间
- 成功失败
- 返回结果摘要

可单独建表：

- `ai_tool_call_log`

### 5.3 上下文控制

随着会话增长，建议使用三层上下文策略：

- 固定系统设定
- 官方记忆窗口
- 业务原始消息重建机制

推荐策略：

- 优先使用官方记忆保留最近 10~20 轮消息
- 业务库中始终保存完整原始消息
- 官方记忆缺失时，从业务原始消息重建

## 6. 后台管理设计

后台建议做成标准管理台，至少包含以下菜单：

### 6.1 用户管理

- 用户列表
- 新增/编辑/禁用用户
- 重置密码
- 查看用户会话数

### 6.2 助手管理

- 创建助手
- 配置模型参数
- 绑定 Skill
- 绑定 Tool
- 绑定 MCP
- 发布/下线

### 6.3 Skill 管理

- Skill 列表
- 创建 Skill
- 编辑 Prompt 模板
- 版本发布
- 启用/停用

### 6.4 工具管理

- 工具列表
- 工具配置
- 参数 schema
- 测试执行
- 绑定到助手

### 6.5 MCP 管理

- MCP Server 列表
- 连接测试
- 能力同步
- 启用/停用

### 6.6 会话管理

- 查看用户会话
- 查看消息明细
- 关键字检索
- 敏感内容审计

## 7. 前端页面建议

### 7.1 用户端

- `/login`
  - 账号密码登录
- `/chat`
  - 左侧会话列表
  - 中间聊天窗口
  - 顶部助手切换
  - 支持流式输出
- `/history`
  - 历史记录检索与查看
- `/profile`
  - 个人信息

### 7.2 管理端

- `/admin/login`
- `/admin/dashboard`
- `/admin/users`
- `/admin/assistants`
- `/admin/skills`
- `/admin/tools`
- `/admin/mcp`
- `/admin/conversations`

## 8. 推荐后端工程结构

建议先从单仓多模块 Maven 工程开始：

```text
spring-ai-agent
├── agent-server
├── agent-admin-api
├── agent-portal-api
├── agent-core
├── agent-domain
├── agent-infra
├── agent-client-mcp
├── agent-client-llm
├── agent-plugin-tool
└── docs
```

如果第一阶段想更快落地，也可以先做成单体：

```text
spring-ai-agent-server
├── controller
├── service
├── manager
├── mapper
├── model
├── config
├── agent
│   ├── orchestrator
│   ├── tool
│   ├── skill
│   ├── mcp
│   └── memory
└── security
```

第一版我更建议单体分层，先把业务跑通，再拆模块。

## 9. 推荐前端工程结构

```text
frontend
├── apps
│   ├── web
│   └── admin
├── packages
│   ├── ui
│   ├── api
│   ├── types
│   └── utils
```

用户端和管理端如果团队小，也可以先做成同一个 Next.js 应用，通过路由区分：

- `/chat`
- `/admin/*`

## 10. 第一阶段最小可用版本 MVP

建议第一阶段不要把所有能力一次做满，优先打通核心闭环。

### MVP 范围

- 用户登录
- 助手管理
- 基础对话
- 会话列表
- 历史消息
- Skill 注入
- 本地工具调用
- MCP Server 基础接入

### 暂时不做

- 多租户
- 复杂权限系统
- 工作流编排
- 知识库 RAG
- 插件市场
- 复杂监控报表

## 11. 分阶段实施建议

### 阶段 1：基础平台

- 完成用户登录与权限
- 完成助手管理
- 完成会话与消息持久化
- 跑通模型基础聊天

### 阶段 2：Agent 能力

- 支持 Skill 动态加载
- 支持本地工具调用
- 支持工具日志记录
- 完成官方记忆窗口控制与重建

### 阶段 3：MCP 能力

- MCP Server 配置
- 能力同步
- MCP tool 统一接入

### 阶段 4：后台增强

- 管理后台完善
- 审计日志
- 用户行为分析

### 阶段 5：高级能力

- 知识库
- 工作流
- 多助手编排
- 多模型路由

## 12. 技术选型建议

### 后端

- `Spring Boot 3.x`
- `Spring AI Alibaba`
- `Spring Security`
- `MyBatis Plus` 或 `MyBatis`
- `MySQL 8.x`
- `Redis`
- `Jackson`
- `Lombok`

### 前端

- `Next.js`
- `React`
- `TypeScript`
- `Ant Design` 或 `Shadcn/ui`
- `SWR` 或 `TanStack Query`

## 13. 我建议你这样落地

如果是你自己从 0 到 1 开始做，我建议按下面顺序推进：

1. 先定义数据库表
2. 先实现登录、用户、助手、会话、消息四类基础 CRUD
3. 再做聊天接口，先不接工具
4. 然后接入 Skill
5. 再抽象 ToolRegistry 和 ToolExecutor
6. 最后接入 MCP
7. 管理后台与用户端并行推进

## 14. 关键设计原则

- Assistant 是配置中心，不要让 Tool/Skill/MCP 直接散落在用户会话里
- Agent 编排要有统一入口，避免对话逻辑分散在 Controller
- Tool、Skill、MCP 都要做统一抽象
- 会话消息必须完整持久化，后期排障和审计非常重要
- 第一版优先单体架构，避免一开始过度拆分
- MVP 先跑通，再逐步增强

## 15. 下一步建议

如果继续往下做，最合理的顺序是：

1. 先输出数据库 ER 设计
2. 再搭 Spring Boot 后端骨架
3. 再搭 Next.js 前端骨架
4. 然后实现登录 + 会话 + 聊天主链路
5. 最后补 Skill / Tool / MCP 管理
