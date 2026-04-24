# ColaCode 本地开发环境配置指南

## 概述

本文档说明如何在**不启动 Nacos** 的情况下，使用 `local` profile 本地独立运行 ColaCode 各个微服务。

## 前置依赖

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 必须 |
| Maven | 3.9+ | 必须 |
| MySQL | 5.7+ / 8.0+ | 必须，需提前创建 `colacode` 数据库 |
| Redis | 6+ / 7+ | 必须，Sa-Token 依赖 Redis 存储会话 |

### 可选中间件（不启动时部分功能降级）

| 中间件 | 影响范围 | 降级行为 |
|--------|---------|---------|
| Elasticsearch | 题目全文搜索 | 降级为数据库 LIKE 模糊查询 |
| RocketMQ | 异步点赞、ES 同步 | 功能不可用或同步执行 |
| MinIO | 对象存储 | 使用本地文件系统存储 |
| AI 服务 | AI 面试引擎 | 使用本地 DATABASE 引擎 |

## 环境变量（可选）

所有配置均已提供默认值，如需覆盖可通过环境变量：

```bash
# 数据库
set BOOT3_DB_URL=jdbc:mysql://127.0.0.1:3306/colacode?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false
set BOOT3_DB_USERNAME=root
set BOOT3_DB_PASSWORD=123456

# Redis
set BOOT3_REDIS_HOST=127.0.0.1
set BOOT3_REDIS_PORT=6379
set BOOT3_REDIS_PASSWORD=

# 服务端口（如需调整）
set BOOT3_SERVER_PORT=3111

# Elasticsearch（可选）
set BOOT3_ES_HOST=127.0.0.1
set BOOT3_ES_PORT=9200

# RocketMQ（可选）
set BOOT3_ROCKETMQ_NAMESERVER=127.0.0.1:9876

# AI 服务（可选）
set BOOT3_AI_ENABLED=false
set BOOT3_AI_BASE_URL=http://localhost:3020
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < doc/sql/colacode-init.sql
```

### 2. 编译公共模块

```bash
mvn clean install -pl colacode-common -am -DskipTests
```

### 3. 启动服务（每个服务独立终端，或 IDEA 中设置 Profile=local）

**推荐启动顺序：**

```bash
# 1. 认证服务（基础服务，先启动）
mvn -pl colacode-auth spring-boot:run -Dspring-boot.run.profiles=local

# 2. 题库服务
mvn -pl colacode-subject spring-boot:run -Dspring-boot.run.profiles=local

# 3. 练习服务
mvn -pl colacode-practice spring-boot:run -Dspring-boot.run.profiles=local

# 4. 面试服务（AI 默认关闭，使用本地题库引擎）
mvn -pl colacode-interview spring-boot:run -Dspring-boot.run.profiles=local

# 5. 社区服务
mvn -pl colacode-circle spring-boot:run -Dspring-boot.run.profiles=local

# 6. OSS 服务
mvn -pl colacode-oss spring-boot:run -Dspring-boot.run.profiles=local

# 7. 微信服务（可选）
mvn -pl colacode-wx spring-boot:run -Dspring-boot.run.profiles=local

# 8. AI 服务（可选，需配置 API Key）
mvn -pl colacode-ai spring-boot:run -Dspring-boot.run.profiles=local

# 9. 网关（最后启动）
mvn -pl colacode-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. 访问服务

| 服务 | 地址 |
|------|------|
| 网关（统一入口） | http://localhost:5000 |
| 网关 Swagger 文档 | http://localhost:5000/swagger-ui.html |
| 认证服务 | http://localhost:3111 |
| 题库服务 | http://localhost:3110 |
| 练习服务 | http://localhost:3113 |
| 面试服务 | http://localhost:3016 |
| 社区服务 | http://localhost:3114 |
| OSS 服务 | http://localhost:4100 |
| 微信服务 | http://localhost:3112 |
| AI 服务 | http://localhost:3020 |

## 本地 Profile 配置说明

每个服务的 `application-local.yml` 已包含完整的独立运行配置：

- **Nacos**：`discovery.enabled: false` / `config.enabled: false`，完全关闭 Nacos 依赖
- **数据库**：本地 MySQL 直连配置
- **Redis**：本地 Redis 直连配置（Sa-Token 会话存储必需）
- **Feign**：本地直连 URL（如 practice → subject）
- **中间件**：Elasticsearch、RocketMQ、AI 等提供本地默认值，不启动时不影响核心功能

## 常见问题

### Q1: 不启动 Redis 可以吗？

**不可以。** Sa-Token 默认使用 Redis 存储登录会话，不启动 Redis 会导致登录功能异常。如需无 Redis 方案，需将 Sa-Token 改为内存模式（需移除 `sa-token-redis-jackson` 依赖）。

### Q2: 如何启用 AI 面试？

1. 启动 `colacode-ai` 服务
2. 配置有效的 API Key（`BOOT3_AI_API_KEY`）
3. 启动 interview 时设置 `BOOT3_AI_ENABLED=true`

### Q3: 如何切换为 Nacos 模式？

去掉 `-Dspring-boot.run.profiles=local` 参数，或显式指定 `-Dspring-boot.run.profiles=default`，服务将使用 `bootstrap.yml` 中的 Nacos 配置。

### Q4: 某个服务启动报错 "Connection refused"？

检查该服务依赖的其他服务是否已启动。例如：
- `practice` 依赖 `subject`
- `interview` 可选依赖 `ai`
- 网关依赖所有业务服务

本地模式下，各服务通过固定端口直连，需确保被调用服务已启动。
