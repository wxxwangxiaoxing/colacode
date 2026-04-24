# ColaCode

ColaCode 是一个基于 **Spring Cloud + Spring Boot 3** 的微服务项目，围绕题库、练习、面试、社区、认证、文件存储与微信集成等场景构建，同时提供独立的 AI 服务用于面试题生成与答案评分。

仓库当前同时包含：
- 后端 Maven 多模块工程
- 前端 `Vue 3 + Vite` 工程
- Nacos 配置示例
- SQL 初始化脚本
- Docker Compose 部署文件
- 各模块设计与联调文档

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
- Redis
- MySQL
- RocketMQ
- Elasticsearch
- MinIO
- SpringDoc OpenAPI
- Sentinel

### 前端
- Vue 3
- Vite
- Pinia
- Vue Router
- Axios
- Ant Design Vue
- pnpm

## 仓库结构

```text
colacode/
├── colacode-gateway/      # 网关服务
├── colacode-auth/         # 认证与权限服务
├── colacode-subject/      # 题库服务
├── colacode-practice/     # 练习服务
├── colacode-interview/    # 面试服务
├── colacode-circle/       # 社区服务
├── colacode-oss/          # 对象存储服务
├── colacode-wx/           # 微信集成服务
├── colacode-ai/           # AI 服务
├── colacode-common/       # 公共模块
├── colacode-web/          # 前端工程
├── deploy/                # Docker Compose 部署文件
└── doc/                   # 文档、SQL、Nacos 配置示例
```

## 模块说明

| 模块 | 说明 | 默认端口 |
| --- | --- | --- |
| `colacode-gateway` | API 网关，负责统一入口、路由转发、鉴权放行、限流、聚合 OpenAPI | `5000` |
| `colacode-auth` | 用户、角色、权限、登录与 RBAC 能力 | `3011` |
| `colacode-subject` | 题库、分类、标签、搜索、点赞/同步等能力 | `3010` |
| `colacode-practice` | 练习与排行榜相关能力 | `3013` |
| `colacode-interview` | 面试流程、会话管理、答题评估、AI 面试接入 | `3015` |
| `colacode-circle` | 社区、动态、消息、敏感词等能力 | `3014` |
| `colacode-oss` | 文件上传下载与对象存储适配 | `4000` |
| `colacode-wx` | 微信相关集成能力 | `3012` |
| `colacode-ai` | AI 题目生成、答案评分服务 | `3020` |
| `colacode-common` | 公共返回体、上下文、拦截器、通用常量与基础组件 | - |
| `colacode-web` | 前端项目，提供 Web 端页面 | `5173` |

## 核心架构约定

### 1. Maven 多模块后端
根工程 `pom.xml` 是聚合工程，统一管理依赖版本与模块：
- Java 17
- Spring Boot 3.4.0
- Spring Cloud 2024.0.0
- Spring Cloud Alibaba 2023.0.1.0

### 2. Nacos 作为注册中心与配置中心
项目大量服务默认通过 Nacos 完成：
- 服务注册发现
- 共享配置加载
- 服务级配置加载

常见共享配置：
- `common-datasource.yaml`
- `common-redis.yaml`
- `common-rocketmq.yaml`

参考文档：`doc/nacos-config/README.md`

### 3. 通过网关统一访问后端服务
网关模块聚合了各业务服务，典型访问前缀包括：
- `/auth/**`
- `/subject/**`
- `/practice/**`
- `/circle/**`
- `/interview/**`
- `/oss/**`
- `/wx/**`

因此本地或部署环境中，通常优先从网关入口访问接口：
- `http://localhost:5000`

### 4. 面试服务可接入独立 AI 服务
`colacode-interview` 已支持通过配置接入 `colacode-ai`：
- AI 题目生成
- AI 答案评分
- 面试流程中的 AI 语义评估

相关配置项示例：
- `INTERVIEW_AI_ENABLED`
- `INTERVIEW_AI_API_KEY`
- `INTERVIEW_AI_SERVICE_NAME`
- `INTERVIEW_AI_BASE_URL`

## 开发环境要求

建议准备如下环境：
- JDK 17
- Maven 3.9+
- Node.js 18+
- pnpm
- MySQL
- Redis
- Nacos

按需准备以下中间件：
- Elasticsearch
- RocketMQ
- MinIO

## 快速开始

### 方式一：使用 Docker Compose 启动基础环境与后端镜像

项目提供了部署文件：
- `deploy/docker-compose.yml`

可用于启动：
- gateway
- auth
- subject
- practice
- circle
- interview
- oss
- wx
- mysql
- redis
- nacos
- elasticsearch
- minio
- rocketmq

说明：当前 `deploy/docker-compose.yml` 主要覆盖后端服务与基础设施，不包含 `colacode-web` 前端，也未定义 `colacode-ai` 服务容器。

示例：

```bash
cd deploy
docker compose up -d
```

启动后可重点访问：
- 网关：`http://localhost:5000`
- Nacos：`http://localhost:8848/nacos`
- MinIO Console：`http://localhost:9001`
- Elasticsearch：`http://localhost:9200`

### 方式二：本地源码启动

### 1. 初始化数据库
SQL 文件位于：
- `doc/sql/colacode-init.sql`
- `doc/sql/20260410-add-subject-browse-count.sql`

也可以参考：
- `doc/colacode-db-optimized.sql`
- `doc/interview-phase1-schema.sql`

### 2. 准备 Nacos 配置
参考：
- `doc/nacos-config/README.md`
- `doc/nacos-config/common-datasource.yaml`
- `doc/nacos-config/common-redis.yaml`
- `doc/nacos-config/common-rocketmq.yaml`

常见环境变量：

```bash
set NACOS_SERVER_ADDR=127.0.0.1:8848
set NACOS_NAMESPACE=dev
set NACOS_GROUP=DEFAULT_GROUP
```

也兼容 Spring 标准写法：

```bash
set SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=127.0.0.1:8848
set SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=127.0.0.1:8848
```

### 3. 启动后端模块
可在根目录执行：

```bash
mvn clean install
```

按需单独启动模块，例如：

```bash
mvn -pl colacode-gateway spring-boot:run
mvn -pl colacode-auth spring-boot:run
mvn -pl colacode-subject spring-boot:run
mvn -pl colacode-practice spring-boot:run
mvn -pl colacode-interview spring-boot:run
mvn -pl colacode-circle spring-boot:run
mvn -pl colacode-oss spring-boot:run
mvn -pl colacode-wx spring-boot:run
mvn -pl colacode-ai spring-boot:run
```

建议启动顺序：
1. MySQL / Redis / Nacos
2. Elasticsearch / RocketMQ / MinIO（按需）
3. `colacode-auth`
4. 其他业务服务
5. `colacode-gateway`
6. `colacode-web`

### 4. 启动前端
前端位于 `colacode-web`，使用 pnpm：

```bash
cd colacode-web
pnpm install
pnpm dev
```

默认前端地址：
- `http://localhost:5173`

其他命令：

```bash
pnpm build
pnpm preview
```

## 接口文档

网关已聚合 OpenAPI 文档，默认可通过以下地址查看：
- `http://localhost:5000/swagger-ui.html`

当前已聚合的服务文档包括：
- auth
- subject
- practice
- circle
- interview
- oss

## 重要配置说明

### Interview AI 配置
`colacode-interview` 默认支持通过环境变量或配置中心接入 AI 服务。

示例配置语义：

```yaml
interview:
  ai:
    enabled: ${INTERVIEW_AI_ENABLED:true}
    api-key: ${INTERVIEW_AI_API_KEY:}
    service-name: ${INTERVIEW_AI_SERVICE_NAME:colacode-ai}
    base-url: ${INTERVIEW_AI_BASE_URL:}
```

### OSS 配置
`colacode-oss` 支持本地存储与 MinIO：
- 本地模式：文件落在 `./uploads/`
- MinIO 模式：通过 `storage.minio.*` 配置接入

### 微信配置
`colacode-wx` 需要提供：
- `wx.mp.app-id`
- `wx.mp.secret`
- `wx.mp.token`
- `wx.mp.aes-key`

## 常用命令

### 后端编译

```bash
mvn clean compile
```

### 后端测试

```bash
mvn test
```

### 打包

```bash
mvn clean package
```

### 指定模块测试/启动

```bash
mvn -pl colacode-interview test
mvn -pl colacode-gateway spring-boot:run
```

## 文档目录

`doc/` 下已经包含较完整的模块说明和部署说明，可作为二次开发入口：

- `doc/01-Auth认证服务.md`
- `doc/02-Circle社区服务.md`
- `doc/03-RocketMQ详细使用.md`
- `doc/04-Elasticsearch详细使用.md`
- `doc/05-Subject题库服务.md`
- `doc/06-Practice练习服务.md`
- `doc/07-Interview面试服务.md`
- `doc/08-OSS对象存储.md`
- `doc/09-Wx微信模块.md`
- `doc/10-Nacos Docker部署.md`
- `doc/11-MySQL8 Docker部署.md`
- `doc/12-SpringAI Interview联调.md`

## 开发提示

- 仓库中的部分 `application.yml` 默认地址可能是示例值或历史环境值，实际开发请优先通过环境变量或 Nacos 覆盖。
- 后端要求 **Java 17**，请确认 `mvn -version` 使用的也是 JDK 17。
- 如果不使用 Nacos，本地可按 `doc/nacos-config/README.md` 中的说明关闭相关能力并使用本地配置兜底。
- 建议通过网关联调接口，而不是直接暴露每个业务服务给前端。

## 后续可扩展方向

- 补充统一的架构图与调用链说明
- 为每个模块补充独立启动说明
- 增加本地开发 `.env` / 配置模板
- 补充 CI/CD、镜像发布与回滚文档

---

如果你是第一次接手这个项目，推荐阅读顺序：
1. `doc/design-philosophy.md`
2. `doc/nacos-config/README.md`
3. `doc/01-Auth认证服务.md` ~ `doc/12-SpringAI Interview联调.md`
4. 根目录 `pom.xml`
5. `colacode-gateway/src/main/resources/application.yml`
