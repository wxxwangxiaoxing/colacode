# Nacos 配置中心使用说明

## 1. 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    Nacos Config Center                   │
│  ┌─────────────────────┐  ┌──────────────────────────┐  │
│  │ common-datasource.yaml│  │ common-redis.yaml        │  │
│  │ (数据源+MyBatis-Plus) │  │ (Redis + Sa-Token)       │  │
│  └─────────────────────┘  └──────────────────────────┘  │
│  ┌─────────────────────┐  ┌──────────────────────────┐  │
│  │ common-rocketmq.yaml │  │ {service}.yaml (服务独立) │  │
│  │ (RocketMQ)          │  │ (ES, XXL-JOB, 微信等)     │  │
│  └─────────────────────┘  └──────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 2. 配置分层

| 层级 | 文件 | 说明 |
|------|------|------|
| **共享配置** | `common-datasource.yaml` | 所有需要数据库的服务共享 |
| **共享配置** | `common-redis.yaml` | 所有需要Redis的服务共享 |
| **共享配置** | `common-rocketmq.yaml` | 所有需要MQ的服务共享 |
| **服务配置** | `bootstrap.yml` | 服务端口 + Nacos连接信息 |
| **服务配置** | `application.yml` | 服务特有配置（ES、XXL-JOB等） |

推荐把 Nacos 连接信息通过环境变量注入，而不是写死在源码里：

```bash
set NACOS_SERVER_ADDR=127.0.0.1:8848
set NACOS_NAMESPACE=dev
set NACOS_GROUP=DEFAULT_GROUP
```

也兼容 Spring 的标准环境变量写法：

```bash
set SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=127.0.0.1:8848
set SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=127.0.0.1:8848
```

## 3. Nacos 控制台操作步骤

### 3.1 登录 Nacos
访问 `http://127.0.0.1:8848/nacos`，默认账号密码 `nacos/nacos`

### 3.2 创建命名空间
- 命名空间管理 → 新建命名空间
- 命名空间ID: `dev`
- 命名名称: `开发环境`

### 3.3 创建共享配置

#### common-datasource.yaml
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/colacode?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    druid:
      initial-size: 5
      max-active: 20
      min-idle: 5
      max-wait: 60000
      test-while-idle: true
      test-on-borrow: false
      validation-query: SELECT 1
      filters: stat,wall

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### common-redis.yaml
```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:
      database: 0
      lettuce:
        pool:
          max-active: 200
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
      timeout: 2000ms

sa-token:
  token-name: satoken
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: uuid
  is-log: false
```

#### common-rocketmq.yaml
```yaml
spring:
  rocketmq:
    name-server: 127.0.0.1:9876
    producer:
      group: colacode-producer
```

### 3.4 创建服务独立配置

每个服务创建对应的配置文件，Data ID 格式：`{服务名}.yaml`

#### colacode-subject.yaml
```yaml
elasticsearch:
  host: 127.0.0.1
  port: 9200

xxl:
  job:
    admin:
      addresses: http://127.0.0.1:8080/xxl-job-admin
    accessToken: ''
    executor:
      appname: colacode-subject
      port: 9999
      logpath: ./logs/xxl-job
      logretentiondays: 30
```

#### colacode-interview.yaml
```yaml
interview:
  ai:
    enabled: false
    api-key: ""
```

## 4. 本地开发模式（不启用 Nacos）

如果本地开发不想启动 Nacos，可在 `bootstrap.yml` 中禁用：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: false
      config:
        enabled: false
```

此时服务会回退到 `application.yml` 中的本地配置。

## 5. 多环境切换

通过 `namespace` 区分环境：

| 环境 | namespace | 说明 |
|------|-----------|------|
| dev | `dev` | 开发环境 |
| test | `test` | 测试环境 |
| prod | `prod` | 生产环境 |

启动时指定：
```bash
java -jar app.jar --spring.cloud.nacos.config.namespace=prod
```

## 6. 配置热更新

Nacos 支持配置热更新，修改配置后无需重启服务：

```java
@RefreshScope
@RestController
public class SomeController {
    @Value("${some.config.key}")
    private String configValue;
}
```

## 7. 配置加载顺序

```
bootstrap.yml (Nacos连接信息)
    ↓
Nacos 共享配置 (common-datasource.yaml, common-redis.yaml)
    ↓
Nacos 服务配置 ({service}.yaml)
    ↓
application.yml (本地兜底配置)
```

后加载的配置会覆盖先加载的同名配置。
