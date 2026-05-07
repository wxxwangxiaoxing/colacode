# ColaCode

ColaCode 是一个基于 `Spring Cloud + Spring Boot 3` 的微服务项目，围绕题库、练习、面试、社区、认证、对象存储和微信集成构建。仓库同时包含独立的 AI 服务 `colacode-ai`，用于面试题生成、答案评分，以及编程题判题结果分析。

当前仓库状态：

- 后端主模块和本地 `local` profile 已可独立联调
- `practice` 的 Judge0 判题后端已接入异步 AI 分析链路
- `colacode-ai` 默认以 `mock` 模式启动，不再依赖仓库内硬编码 API Key
- 前端 `colacode-web` 目前只覆盖基础页面，未完成的业务页面仍需单独补齐

## 技术栈

### 后端

- Java 17
- Spring Boot 3.4.0
- Spring Cloud 2024.0.0
- Spring Cloud Alibaba / Nacos
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring AI
- MyBatis-Plus
- Sa-Token
- Redis / MySQL
- RocketMQ / Elasticsearch / MinIO
- SpringDoc OpenAPI
- Sentinel

### 前端

- Vue 3
- Vite
- TypeScript

## 仓库结构

```text
colacode/
├─ colacode-gateway/     # 网关服务
├─ colacode-auth/        # 认证与权限
├─ colacode-subject/     # 题库服务
├─ colacode-practice/    # 练习与 Judge0 判题
├─ colacode-interview/   # 面试服务
├─ colacode-circle/      # 社区服务
├─ colacode-oss/         # 对象存储服务
├─ colacode-wx/          # 微信集成服务
├─ colacode-ai/          # AI 服务
├─ colacode-common/      # 公共模块
├─ colacode-web/         # 前端工程
├─ deploy/               # Docker Compose 与部署文件
└─ doc/                  # 设计、联调、SQL、服务说明
```

## 模块与端口

| 模块 | 说明 | 默认端口 |
| --- | --- | --- |
| `colacode-gateway` | 统一入口、路由、鉴权、限流、OpenAPI 聚合 | `5000` |
| `colacode-auth` | 登录、注册、权限、RBAC | `3011` / local `3111` |
| `colacode-subject` | 题库、分类、标签、搜索 | `3010` / local `3110` |
| `colacode-practice` | 练习、提交、Judge0 判题、AI 判题分析 | `3013` / local `3113` |
| `colacode-interview` | 面试流程、题源、评分、AI 面试接入 | `3015` / local `3016` |
| `colacode-circle` | 社区、消息、敏感词、WebSocket | `3014` / local `3114` |
| `colacode-oss` | 本地/MinIO 存储适配 | `4000` / local `4100` |
| `colacode-wx` | 微信消息与事件处理 | `3012` / local `3112` |
| `colacode-ai` | 面试 AI 与判题 AI 分析 | `3020` |

## AI 与判题

### `colacode-ai`

已提供接口：

- `POST /ai/interview/question`
- `POST /ai/interview/score`
- `POST /ai/judge/analyse`
- `GET /ai/config/models`
- `GET /ai/config/current`

当前运行时已接通的模型只有：

- `mock`
- `openai`

默认使用 `mock`。如果要启用真实 OpenAI 模式，至少需要设置：

```bash
set COLACODE_AI_DEFAULT_MODEL=openai
set COLACODE_AI_OPENAI_ENABLED=true
set OPENAI_API_KEY=your_key
set OPENAI_MODEL=gpt-5.4
```

### `colacode-practice`

Judge0 判题完成后，可选触发异步 AI 分析并写回：

- `ai_status`
- `ai_feedback`

本地启用方式：

```bash
set BOOT3_AI_URL=http://127.0.0.1:3020
set BOOT3_JUDGE_AI_ENABLED=true
```

如需连通过题提交也生成反馈，可再开启：

```bash
set BOOT3_JUDGE_AI_INCLUDE_ACCEPTED=true
```

## 本地开发

推荐直接使用 `local` profile，不依赖 Nacos：

1. 准备 `JDK 17`、`Maven 3.9+`、`MySQL`、`Redis`
2. 执行数据库初始化脚本 `doc/sql/colacode-init.sql`
3. 参考 `doc/local-development-guide.md`
4. 按顺序启动 `auth -> subject -> practice/interview/other services -> gateway`

常用命令：

```bash
mvn clean install -pl colacode-common -am -DskipTests
mvn -pl colacode-auth spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl colacode-subject spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl colacode-practice spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl colacode-ai spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl colacode-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## 文档入口

- `doc/local-development-guide.md`
- `doc/services/01-Gateway/README.md`
- `doc/services/09-AI/README.md`
- `doc/新增ColaCode 在线判题系统（Judge0 版）设计文档.md`
- `doc/oj-next-steps.md`

## 说明

- 仓库中的很多默认地址是历史环境值或联调用示例，实际开发请优先通过环境变量或 Nacos 覆盖
- 后端编译必须使用 `JDK 17`
- 本次整理只补齐了后端与工程收尾，前端页面范围仍以当前 `colacode-web` 实际实现为准
