---

# Nacos Docker 部署文档（colacode 项目）

## 1. 目标

本文档用于在 `colacode` 项目中通过 Docker 部署 Nacos，并完成配置中心初始化，使各微服务可以正常从 Nacos 读取配置。

当前项目约定：

- Nacos 地址：`127.0.0.1:8848`
- 命名空间：`dev`
- Group：`DEFAULT_GROUP`
- 配置格式：`yaml`
- 服务级配置 Data ID：`${spring.application.name}.yaml`（即项目约定，不做推广性“默认框架规则”）

---

## 2. Nacos 版本与端口说明（以 2.2.0 单机模式为例）

### 2.1 版本

项目使用 **Nacos 2.2.0**，与 1.x 版本相比存在以下行为差异：

- 客户端与服务端通信由 HTTP 改为 **gRPC**
- 新增 gRPC 端口：`9848`（客户端 gRPC 请求服务端）、`9849`（服务端 gRPC 请求服务端）
- 更适用于更大规模微服务注册/发现与配置管理

### 2.2 端口清单

| 端口 | 协议 | 说明 |
|------|------|------|
| 8848 | HTTP | Nacos 控制台 + OpenAPI |
| 9848 | gRPC | 客户端 gRPC 请求服务端（**必须开放**） |
| 9849 | gRPC | 服务端 gRPC 请求服务端（**主要用于集群节点通信**，本地单机模式可不映射） |

> ⚠️ 如果部署在云服务器或 Docker 中，必须确保 `8848`、`9848` 端口可访问（尤其是客户端/服务之间通信），否则服务连接、注册或配置读取会失败。`9849` 仅在集群部署时需要，单机 standalone 模式下可不强制暴露。

---

## 3. 前置条件

- 已安装 Docker
- 已安装 Docker Compose
- 项目根目录（示例）：`D:\project\backend\colacode`
- 确保以下端口未被占用：`8848`、`9848`（`9849` 供参考，集群部署时再决定）

---

## 4. 启动 Nacos

项目根目录的 [docker-compose.yml](./docker-compose.yml) 已包含 Nacos 服务（建议按照下方更稳健的示例编写）：

```yaml
services:
  nacos:
    image: nacos/nacos-server:v2.2.0
    container_name: colacode-nacos
    environment:
      MODE: standalone
      PREFER_HOST_MODE: hostname
      # 如需启用控制台登录鉴权（默认有差异，按实际需要配置）
      # NACOS_AUTH_ENABLE: "true"
      # JVM 调优（可选，根据内存调整）
      # JVM_XMS: 512m
      # JVM_XMX: 1024m
      # JVM_XMN: 256m
    ports:
      - "8848:8848"
      - "9848:9848"
      # - "9849:9849"  # 单机无需开放（集群部署多节点互通时使用）
    restart: unless-stopped
    volumes:
       - ./docker/nacos/logs:/home/nacos/logs
       - ./docker/nacos/data:/home/nacos/data
```

### 启动指令

只启动 Nacos：

```bash
docker compose up -d nacos
```

如果希望同时启动基础依赖（MySQL、Redis、ES、MinIO、RocketMQ）：

```bash
docker compose up -d mysql redis nacos elasticsearch minio rocketmq
```

查看容器状态（Windows：`findstr`，通用：`grep`）：

```bash
docker ps | grep nacos
```

查看启动日志：

```bash
docker logs -f colacode-nacos
```

当 Nacos 启动完成后，访问控制台：

```text
http://127.0.0.1:8848/nacos
```

控制台默认登录信息（在默认本地开发配置下；若镜像开启自定义鉴权，请以实际环境变量为准）：

- 用户名：`nacos`
- 密码：`nacos`

> ⚠️ **重要：数据是否持久化**
> 当前示例未显式挂载数据卷或配置外部数据库，因此容器删除（如执行 `docker compose rm -f nacos`）会导致 **namespace / 配置内容 / 可能的注册元数据** 丢失。若你需要持久化，可挂载容器目录（例如 `/home/nacos/logs`、`/home/nacos/data`），或更稳健地使用外部 MySQL。

---

## 5. 创建命名空间

登录 Nacos 控制台后，创建项目使用的命名空间：

- 操作路径：`命名空间 -> 新建命名空间`

填写：

- 命名空间 ID：`dev`
- 命名空间名称：`开发环境`
- 描述：`colacode dev`

---

## 6. 导入共享配置

项目提供以下可供导入的共享配置文件（建议方案）：

- [doc/nacos-config/common-datasource.yaml](./doc/nacos-config/common-datasource.yaml)
- [doc/nacos-config/common-redis.yaml](./doc/nacos-config/common-redis.yaml)
- [doc/nacos-config/common-rocketmq.yaml](./doc/nacos-config/common-rocketmq.yaml)

进入 `dev` 命名空间，在 `配置管理 -> 配置列表` 中新增以下配置：

### 6.1 common-datasource.yaml

- Data ID：`common-datasource.yaml`
- Group：`DEFAULT_GROUP`
- 配置格式：`YAML`
- 内容：复制 [common-datasource.yaml](./doc/nacos-config/common-datasource.yaml)

### 6.2 common-redis.yaml

- Data ID：`common-redis.yaml`
- Group：`DEFAULT_GROUP`
- 配置格式：`YAML`
- 内容：复制 [common-redis.yaml](./doc/nacos-config/common-redis.yaml)

### 6.3 common-rocketmq.yaml

- Data ID：`common-rocketmq.yaml`
- Group：`DEFAULT_GROUP`
- 配置格式：`YAML`
- 内容：复制 [common-rocketmq.yaml](./doc/nacos-config/common-rocketmq.yaml)

---

## 7. 创建服务配置（服务级 Data ID）

除了共享配置，当前项目约定每个服务的专属配置统一按服务名读取 Data ID：

```text
${spring.application.name}.yaml
```

例如 `colacode-auth` -> `colacode-auth.yaml`、`colacode-subject` -> `colacode-subject.yaml`。

> ⚠️ 此约定是**本项目设计**，不是通用的 Nacos 默认规则（实际加载方式受 `prefix`、`file-extension`、`spring.profiles.active`、`shared-configs`、`extension-configs` 等配置影响）。因此服务启动读取配置时请以各服务 `bootstrap.yml` / 配置导入方式为准。

建议为每个服务创建对应的服务级配置，以便后续扩展与问题排查（也可以减少因配置缺失造成的告警/不确定行为）。

---

### 7.1 服务清单与 bootstrap.yml / 配置载入关系

各服务的 `bootstrap.yml` 会定义 Nacos 连接信息以及共享配置依赖。项目目前约定的依赖如下：

| 服务 | 端口 | Data ID | 共享配置依赖 |
|------|------|---------|-------------|
| colacode-gateway | 5000 | colacode-gateway.yaml | common-redis.yaml |
| colacode-auth | 3011 | colacode-auth.yaml | common-datasource.yaml, common-redis.yaml |
| colacode-subject | 3010 | colacode-subject.yaml | common-datasource.yaml, common-redis.yaml, common-rocketmq.yaml |
| colacode-practice | 3013 | colacode-practice.yaml | common-datasource.yaml, common-redis.yaml |
| colacode-circle | 3014 | colacode-circle.yaml | common-datasource.yaml, common-redis.yaml |
| colacode-interview | 3015 | colacode-interview.yaml | common-datasource.yaml, common-redis.yaml |
| colacode-oss | 4000 | colacode-oss.yaml | common-redis.yaml |
| colacode-wx | 3012 | colacode-wx.yaml | common-redis.yaml |

---

### 7.2 各服务配置模板（建议）

以下为可在 Nacos `dev` 命名空间中创建的完整配置模板，具体内容因实际环境调整（例如真实 IP、API Key、Token 等）。

#### colacode-gateway.yaml

> ⚠️ **Gateway 路由选项（两种策略）：**
> - **服务发现 + 负载均衡（推荐，和 Nacos 自动服务发现一致）**：使用 `lb://服务名`，例如 `lb://colacode-auth`
> - **本地直连（仅用于快速调试）**：使用 `http://127.0.0.1:3011`，但这不属于统一通过 Nacos 注册/服务发现的完整方案

```yaml
# Gateway 路由（推荐使用 lb:// 服务名）
spring:
  cloud:
    gateway:
      routes:
        - id: colacode-auth
          uri: lb://colacode-auth
          predicates:
            - Path=/auth/**
        - id: colacode-subject
          uri: lb://colacode-subject
          predicates:
            - Path=/subject/**
        - id: colacode-practice
          uri: lb://colacode-practice
          predicates:
            - Path=/practice/**
        - id: colacode-circle
          uri: lb://colacode-circle
          predicates:
            - Path=/circle/**
        - id: colacode-interview
          uri: lb://colacode-interview
          predicates:
            - Path=/interview/**
        - id: colacode-oss
          uri: lb://colacode-oss
          predicates:
            - Path=/oss/**
        - id: colacode-wx
          uri: lb://colacode-wx
          predicates:
            - Path=/wx/**
```

#### colacode-auth.yaml

```yaml
# colacode-auth 服务专属配置
# 当前无额外业务配置，可留空（不强制在 Nacos 中写入业务无关内容）
spring:
  profiles:
    active: dev
```

#### colacode-subject.yaml

```yaml
elasticsearch:
  host: 127.0.0.1
  port: 9200

xxl:
  job:
    admin:
      addresses: http://127.0.0.1:8080/xxl-job-admin
    accessToken: ''  # 如启用鉴权，填写 token
    executor:
      appname: colacode-subject
      port: 9999
      logpath: ./logs/xxl-job
      logretentiondays: 30
```

#### colacode-practice.yaml

```yaml
# Practice 服务特有配置（建议统一使用服务发现；本示例提供 Feign 配置参考）
feign:
  # 如果 Practice 需要直连 Subject（临时策略），可保留以下配置（仅调试用）
  subject:
    url: http://127.0.0.1:3010
  # 更推荐使用服务发现（对应 @FeignClient(name = "colacode-subject")），并确保服务已注册到 Nacos
```

#### colacode-circle.yaml

```yaml
# colacode-circle 服务专属配置
# 当前无额外业务配置，可留空
spring:
  profiles:
    active: dev
```

#### colacode-interview.yaml

```yaml
interview:
  ai:
    enabled: false  # 未接入 AI 能力时可先保留默认空值
    api-key: ""
```

#### colacode-oss.yaml

```yaml
# OSS 服务特有配置（本地存储占位）
storage:
  local:
    path: ./uploads/
    access-url: http://localhost:4000/oss/download/
```

#### colacode-wx.yaml

```yaml
# 微信服务特有配置（未使用可先留空）
wx:
  appId: ""
  appSecret: ""
  token: ""
  aesKey: ""
```

---

## 8. 配置加载关系（项目实测或项目设计，非绝对全局默认）

当前项目的配置“加载链路”大致如下（根据你项目的 `bootstrap.yml` + Spring Cloud Alibaba 实现）：

```text
启动参数 / 本地基础配置（优先）
  -> Nacos shared-configs（共享配置列表，按配置顺序，可存在覆盖）
  -> Nacos 服务级配置：${spring.application.name}.yaml（项目约定）
  -> 本地 application.yml / application-*.yml 补充或兜底
```

共享配置列表（`shared-configs`）依赖关系如下（请与每个服务的 `bootstrap.yml` 中配置一致）：

- `colacode-gateway`：`common-redis.yaml`
- `colacode-auth`：`common-datasource.yaml`、`common-redis.yaml`
- `colacode-subject`：`common-datasource.yaml`、`common-redis.yaml`、`common-rocketmq.yaml`
- `colacode-practice`：`common-datasource.yaml`、`common-redis.yaml`
- `colacode-circle`：`common-datasource.yaml`、`common-redis.yaml`
- `colacode-interview`：`common-datasource.yaml`、`common-redis.yaml`
- `colacode-oss`：`common-redis.yaml`
- `colacode-wx`：`common-redis.yaml`

> ⚠️ 配置优先级、覆盖顺序和 profile 激活（`spring.profiles.active`）会影响最终生效内容，请以你服务实际启动逻辑为准；此外 `bootstrap.yml` 机制在新版本 Spring Cloud / Spring Boot 社区可能有迁移方案（如 `spring.config.import`），本文叙述为当前项目的实现方式。

---

## 9. 配置热更新（可动态刷新 vs 需重启）

Nacos 支持配置的动态“推送/刷新”能力，部分简单业务配置可以“热更新”，但基础设施配置（如数据源、路由、连接池等）通常不会在运行时完全自动重建。

### 9.1 已开启热更新的共享配置（建议）

当前项目共享配置列表一般配置了 `refresh: true`（以你的 `bootstrap.yml` + Spring Cloud Alibaba 版本为准）：

```yaml
shared-configs:
  - data-id: common-datasource.yaml
    group: DEFAULT_GROUP
    refresh: true
  - data-id: common-redis.yaml
    group: DEFAULT_GROUP
    refresh: true
  - data-id: common-rocketmq.yaml
    group: DEFAULT_GROUP
    refresh: true
```

### 9.2 使用 @RefreshScope（热更新能力的使用方法）

仅当配置通过 `@Value` 或 `@ConfigurationProperties` 注入，并且目标 Bean 配置了 `@RefreshScope`（或通过事件/手动刷新）时，修改 Nacos 配置后才能在运行中热刷新：

```java
@RefreshScope
@RestController
public class TestController {
    @Value("${interview.ai.enabled:false}")
    private boolean aiEnabled;

    @GetMapping("/config/test")
    public boolean isAiEnabled() {
        return aiEnabled;
    }
}
```

### 9.3 热更新的生效边界（实用建议）

- **可热更新（通常）**：业务开关、阈值、文案、功能增强参数等简单配置（需 `@RefreshScope`）
- **不建议 / 通常需重启**：数据库连接配置（连接池初始化）、消息队列连接配置、线程池配置、路由配置等基础设施配置
- **Gateway 路由热更新**：能力取决于项目的实现方式（配置属性加载、是否订阅 `RefreshRoutesEvent` 等）。如果你仅通过静态配置加载路由，修改后不一定自动生效；很多场景下**需要手动刷新或重启 Gateway**，实际验证。

---

## 10. 服务注册验证

### 10.1 查看服务列表

启动所有微服务后，在 Nacos 控制台查看服务注册情况：

```text
服务管理 -> 服务列表 -> 选择 dev 命名空间期看到服务（实例数量以你实际启动数量为准）：

| 服务名 | 实例数 | �|
| colacode-gateway | 1 | 1 |
| colacode-auth | 1 | 1 | 1 |
| colacode-practice | 1 | 1 |
| colacode-circle | 1 | 1 |
| colacode-interview | 1 | 1 |
| colacode-oss | 1 | 1 |
| colacode-wx | 1 | 1 |

### 10.2 服务详情

点击服务名可以查看实例详情，包括：

- IP 地址
- 端口号
- 元数据（metadata）
- 健康状态
- 权重

### 10.3 服务间调用验证（Nacos 服务发现）

在支持服务发现的项目中，服务名访问目标服务，而不是写死 IP/端服务名调用，Nacos 自动发现
public interface SubjectFeignClient {
    @GetMapping("/subject/info/query")
    Result<SubjectInfoDTO> querySubject(@RequestParam("id") Long id);
}
```

如果进行 Gateway 集成，请将 Gateway 路由 `uri` 改为 `lb://服务名`（参见 7.2 Gateway 配置模板），而不要写死 `127.0.0.1:XXXX`，以便后续扩缩容/服务注册正常生效。

---

## 11. 启动验证

### 11.1 验证 Nacos 容器

```bash
docker ps
```

确认 `colacode-nacos` 正常运行，并且端口 `8848`、`9848` 已映射（`9849` 供集群使用，本地可选）。

### 11.2 验证 Nacos 控制台

浏览器访问：

```text
1:8848/nacos
```

确认：

- 可以正常登录（默认 `nacos / nacos`）
- `dev` 命名空间已创建
- 共享配置（common-datasource.yaml / common-redis.yaml / common-rocketmq.yaml）与各服务 Data ID 已存在

### 11.3 验证服务读取配置（实测日志）

启动任一微服务，例如：

```bash
mvn -pl colacode-auth -am spring-boot:run
```

重点检查启动日志：成功连接 Nacosdev / DEFAULT_GROUP`、成功加载以下配置（具体数据以你启动服务的 `bootstrap.yml` 依赖为准）：

- `common-datasource.yaml`
- `common-redis.yaml`
- 服务级 `colacode-auth.yaml`

如果日志中出现找不到 Data ID 或命名空间错误，请按 12. 常见问题 部分排查。

### 11.4 接口验证（更加落地）

如果 Gateway 已启动（默认 5000 端口），可以做一次简单 HTTP 校验：

```bash
# 示例：登录/具体接口路径以项目为准）
curl http '{"username":"test","password":"test"}'

# 示例：请求 subject.0.1:5000/subject/info/query?id=1"
```

如果直接访问服务（例如 `col 已启动于 3010）：

```bash
curl "http://127.0.0.1:3010/subject/info/query?id=1"
```

### 11.5 常用排查命令

查看容器日志：

```bash
docker logs -f colacode-nacos
```

重启 Nacos：

```bash
docker restart colacode-nacos
```

停止 Nacos：

```bash
docker compose stop nacos
```

删除 Nacos 容器并重新创建（⚠️ **会删除未持久化的数据**，需重新导入配置）：

```bash
docker compose rm -f nacos
docker compose up -d nacos
```

---

## 12. 常见问题

### 12.1 服务启动提示找不到 namespace `dev`

原因：

- Nacos 中还没有创建 `dev` 命名空间
- 创建了命名空间，但 Namespace ID 不是 `dev`

解决方案：

- 删除错误命名空间
- 重新按 **Namespace ID = `dev`** 创建

### 12.2 服务启动提示找不到 Data ID

原因：

- 未创建 `common-datasource.yaml`、`common-redis.yaml` 等共享配置
- 服务级配置文件名（Data ID）与 `spring.application.name` 值不一致
- Group 不是 `DEFAULT命名空间不是 `dev`
- 服务的 `bootstrap.yml` shared-configs / extension-configs 配置了 或排序无效，导致实际未加载

解决方案：

- �`）
- 核对 Group = `DEFAULT_GROUP`，命名（参见 7.1）
- 如果你确实允许配置 建议为每个服务创建对应的服务级配置，便成本。

### 12.3 浏览器打不开 Nacos 控制台（8848848` 端口未被占用（`netstat -8`）
2. 执行 `docker ps` 检查端口）
3. 执行 `docker logs -f colacode-nacos` 检查容存不足、端口冲突失败）

### 12.4 服务在�器内，`server-addr` 不应写 `127.0:
cloud:
nacos:
discovery:
server-addr      config:
server-addr: nacos:8848
```

于同一个 Docker Compose 网络**，并且服务名能通过 Docker失败（IP/端口不对）

原因可能是：

- Nacos 中服务实例元数据异常（如由错误的 `PREFER_HOST使用了容器内 `127.0.0.1`（需要 `http://127.0.0.1:XXXX` 而非 `lb://服务名`

解决方案PREFER_HOST_MODE=hostname`（或根据部署环境配置一致的服务注册 IP）
- 改用 `lb://服务名` 的 Gateway 路由 + 服务发现模式
- 检查 Nacos 控制台“服务列表”中实例的 IP 是否可被其他服务访问

### 12.6 热更新生效了但有些配置修改后无效（包括 Gateway 路由）

原因：

- 修改的配置并未纳入 `@RefreshScope` / 刷新事件（业务配置）
- 配置是“基础设施配置”（数据源、连接池、路由定义等），多数实现不支持运行时完全重建配置
- Gateway 路由热更新能力取决于项目的加载方式和监听机制（部分场景需要刷新或重启）

解决方案：

-，并确保对应 Bean 使用 `@RefreshScope`
- 基础设施实需要 Gateway 路由实时化，请验证 `RefreshRoutesEvent`场景进行完整测试

### 12.7 容器删除后 数据持久化（无卷/没有外部 MySQL）
- 执 等命令

解决方案：

- 重建后 Nacos 持久化（挂载卷或接入外部 MySQL## 13. 推荐执行顺序

```text
1.acos 控制台（http://127.0.0.1. 导入 common-datasource.yaml（dev / DEFAULT_GROUP）
5. 导入 common. 导入 common-rocketmq.yaml（dev / DEFAULT_GROUP）
7. 创建各服务的 {service}.yaml 服务配置（dev / DEFAULT_GROUP）
8. 启动 mysql、redis、rocketmq、es 等必要依赖（或 docker compose up -d）
9. 启动各微服务并验证读取配置（日志 + Nacos 服务列表 + 接口调用）
```

