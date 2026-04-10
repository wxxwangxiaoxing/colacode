# ColaCode 项目

ColaCode 是一个在线教育/学习平台，提供用户认证、题库管理、练习、面试模拟、社区交流等功能。项目采用微服务架构，使用 Spring Cloud 生态系统构建，包含多个独立的服务模块。

## 项目结构

```
├── colacode-ai/          # AI服务模块 - 智能题目生成、答案评分
├── colacode-auth/        # 认证服务模块 - 用户认证、授权、角色管理
├── colacode-circle/      # 社区服务模块 - 社区交流、敏感词过滤
├── colacode-common/      # 公共模块 - 通用工具、异常处理、通用配置
├── colacode-gateway/     # 网关服务模块 - 路由管理、认证、限流
├── colacode-interview/   # 面试服务模块 - 面试模拟、评分、历史记录
├── colacode-oss/         # OSS服务模块 - 文件上传、下载、管理
├── colacode-practice/    # 练习服务模块 - 练习管理、排行榜
├── colacode-subject/     # 题库服务模块 - 题目管理、分类、搜索
├── colacode-web/         # 前端应用模块 - 前端界面、用户交互
├── colacode-wx/          # 微信模块 - 微信消息处理、事件响应
├── deploy/               # 部署相关配置
├── doc/                  # 文档目录
├── 原型图/               # 原型图目录
├── CODE_WIKI.md          # 代码Wiki文档
├── docker-compose.yml    # Docker Compose配置文件
├── pom.xml               # Maven父项目配置
└── README.md             # 项目说明文档
```

## 核心功能

- **用户认证与授权**：用户注册、登录、角色管理、权限管理
- **题库管理**：题目管理、分类管理、标签管理、题目搜索
- **练习系统**：练习集管理、练习提交与评分、练习排行榜
- **面试模拟**：关键词分析、面试模拟、答案评分、面试历史记录
- **社区交流**：消息管理、敏感词过滤、WebSocket实时通信
- **文件管理**：文件上传、下载、管理
- **AI集成**：智能题目生成、答案评分

## 技术栈

### 后端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 后端开发语言 |
| Spring Boot | 3.4.0 | 应用框架 |
| Spring Cloud | 2024.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 微服务生态 |
| MyBatis Plus | 3.5.9 | ORM 框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.0+ | 缓存、会话管理 |
| Nacos | 2.2.0 | 服务发现、配置管理 |
| RocketMQ | 5.1.0 | 消息队列 |
| Elasticsearch | 7.14.0 | 搜索引擎 |
| MinIO | 最新版 | 对象存储 |
| Spring AI | 1.1.2 | AI 集成 |
| Sa-Token | 1.39.0 | 认证授权 |
| Sentinel | 1.8.8 | 服务限流、熔断 |

### 前端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.0+ | 前端框架 |
| Vite | 最新版 | 构建工具 |
| Pinia | 最新版 | 状态管理 |
| Vue Router | 4.0+ | 路由管理 |

## 运行方式

### 开发环境

1. **环境准备**：
   - JDK 17+
   - Maven 3.6+
   - MySQL 8.0+
   - Redis 7.0+
   - Nacos 2.2.0+
   - Elasticsearch 7.14.0+
   - RocketMQ 5.1.0+
   - MinIO 最新版

2. **启动顺序**：
   1. 启动 Nacos：`sh startup.sh -m standalone`
   2. 启动 MySQL：`systemctl start mysql`
   3. 启动 Redis：`systemctl start redis`
   4. 启动 Elasticsearch：`systemctl start elasticsearch`
   5. 启动 RocketMQ：`sh bin/mqnamesrv.sh start && sh bin/mqbroker.sh start`
   6. 启动 MinIO：`minio server /data`
   7. 启动各个微服务：`mvn spring-boot:run`

3. **配置管理**：
   - 服务配置：通过 Nacos 配置中心管理
   - 数据源配置：`common-datasource.yaml`
   - Redis 配置：`common-redis.yaml`
   - RocketMQ 配置：`common-rocketmq.yaml`

### Docker 部署

1. **环境准备**：
   - Docker
   - Docker Compose

2. **部署步骤**：
   ```bash
   # 克隆代码
   git clone <repository-url>
   cd colacode
   
   # 构建镜像并启动服务
   docker-compose up -d
   ```

3. **环境变量**：
   - `OPENAI_API_KEY`：OpenAI API 密钥
   - `OPENAI_MODEL`：OpenAI 模型名称，默认 gpt-4o-mini

4. **服务访问**：
   - 网关服务：http://localhost:5000
   - Nacos：http://localhost:8848
   - MinIO：http://localhost:9001
   - Elasticsearch：http://localhost:9200

## 模块说明

### 网关服务 (colacode-gateway)
- **核心功能**：请求路由与转发、用户认证与授权、接口限流、统一异常处理
- **关键类**：`GatewayApplication`、`LoginFilter`、`LoginUserHeaderFilter`、`GatewayRouteConfig`、`SentinelGatewayConfig`

### 认证服务 (colacode-auth)
- **核心功能**：用户注册、登录、注销、密码管理、角色与权限管理、用户状态管理
- **关键类**：`AuthApplication`、`UserDomainService`、`RoleDomainService`、`PermissionDomainService`、`LoginSecurityService`

### 题库服务 (colacode-subject)
- **核心功能**：题目管理（增删改查）、题目分类、标签管理、题目搜索（Elasticsearch）、题目贡献统计
- **关键类**：`SubjectApplication`、`SubjectDomainService`、`CategoryDomainService`、`SubjectTypeHandlerFactory`、`SubjectEsService`

### 练习服务 (colacode-practice)
- **核心功能**：练习管理、练习集管理、练习提交与评分、练习排行榜
- **关键类**：`PracticeApplication`、`PracticeDomainService`

### 面试服务 (colacode-interview)
- **核心功能**：面试模拟、题目分析、答案评分、面试历史记录、面试报告生成
- **关键类**：`InterviewApplication`、`InterviewDomainService`、`InterviewEngine`、`AiInterviewEngine`、`DatabaseInterviewEngine`

### 社区服务 (colacode-circle)
- **核心功能**：社区交流、敏感词过滤、WebSocket 实时通信、消息管理
- **关键类**：`CircleApplication`、`CircleDomainService`、`DFAFilter`、`ChickenSocketHandler`

### AI服务 (colacode-ai)
- **核心功能**：智能题目生成、答案评分、AI 模型管理
- **关键类**：`AiApplication`、`AiService`、`RealAiService`、`MockAiService`

### OSS服务 (colacode-oss)
- **核心功能**：文件上传、文件下载、文件管理、存储适配器管理
- **关键类**：`OssApplication`、`OssController`、`StorageAdapter`、`LocalStorageAdapter`、`MinioStorageAdapter`

### 微信模块 (colacode-wx)
- **核心功能**：微信消息处理、微信事件响应、微信消息加密与验证
- **关键类**：`WxApplication`、`WxController`、`WxMessageHandler`、`WxMessageHandlerFactory`

### 公共模块 (colacode-common)
- **核心功能**：通用工具类、统一异常处理、通用响应格式、跨服务调用配置
- **关键类**：`Result`、`BusinessException`、`CommonGlobalExceptionHandler`、`LoginUserContext`、`CommonFeignAutoConfiguration`

### 前端应用 (colacode-web)
- **核心功能**：前端界面、用户交互
- **技术栈**：Vue.js 3.0+、Vite、Pinia、Vue Router

## 开发指南

### 代码规范
- 遵循 Java 代码规范
- 使用 Lombok 简化代码
- 使用 MapStruct 处理对象转换
- 使用 Sa-Token 处理认证授权
- 使用 MyBatis Plus 处理数据库操作

### 模块开发流程
1. **创建模块**：在根目录下创建新模块，添加 pom.xml
2. **配置依赖**：在 pom.xml 中添加必要的依赖
3. **实现功能**：按照领域驱动设计的思想，实现核心功能
4. **编写测试**：为核心功能编写单元测试
5. **配置服务**：在 Nacos 中配置服务相关配置
6. **注册路由**：在网关服务中注册路由

### 数据库操作
- 使用 MyBatis Plus 进行数据库操作
- 遵循 RESTful API 设计规范
- 使用事务管理确保数据一致性
- 使用索引优化查询性能

### 微服务通信
- 使用 OpenFeign 进行服务间调用
- 使用 RocketMQ 进行异步消息处理
- 使用 Redis 进行缓存共享
- 使用 Nacos 进行服务发现和配置管理

### 异常处理
- 使用 BusinessException 处理业务异常
- 使用 CommonGlobalExceptionHandler 统一处理异常
- 使用 Result 统一响应格式

## 监控与维护

### 监控
- 使用 Spring Boot Actuator 监控服务状态
- 使用 Sentinel 监控服务流量和熔断
- 使用 ELK stack 收集和分析日志

### 日志
- 使用 SLF4J + Logback 进行日志管理
- 统一日志格式和级别
- 关键操作记录详细日志

### 故障排查
- 查看服务日志
- 检查服务状态
- 检查数据库连接
- 检查 Redis 连接
- 检查 Nacos 服务状态

## 许可证

本项目采用 MIT 许可证。详情请参阅 LICENSE 文件。

## 贡献

欢迎贡献代码、报告问题或提出建议。请通过 GitHub Issues 或 Pull Requests 参与项目。

## 联系方式

如有问题或建议，请联系项目维护者。