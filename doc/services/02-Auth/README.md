# ColaCode 学习笔记

## 项目概览

**ColaCode** 是一个程序员专属社区平台，采用 Spring Cloud 微服务架构，DDD 领域驱动设计。

### 技术栈

- **Java 8** + **Spring Boot 2.4.2** + **Spring Cloud 2020.0.6**
- **Nacos** - 服务注册与配置中心
- **MyBatis-Plus** - ORM 框架
- **Sa-Token** - 轻量级认证框架
- **Redis** - 缓存 + Session 存储
- **Druid** - 数据库连接池
- **MapStruct** - 对象映射
- **Lombok** - 简化代码

### 微服务架构

```
colacode-gateway    :5000   # API网关
colacode-auth       :3011   # 认证授权
colacode-subject    :3010   # 题库服务
colacode-practice   :3013   # 练习服务
colacode-interview  :3015   # AI面试
colacode-circle     :3014   # 社区圈子
colacode-oss        :4000   # 对象存储
colacode-wx         :3012   # 微信服务
colacode-common             # 公共组件
```

***

## 第一章：Auth 认证服务

### 1.1 DDD 四层架构

Auth 服务采用 DDD 分层架构，这是整个项目的样板模式：

```
colacode-auth/
├── application/          # 应用层 - 对外接口
│   ├── controller/       #   REST 控制器
│   ├── dto/              #   数据传输对象
│   └── converter/        #   DTO ↔ BO 转换器
├── domain/               # 领域层 - 核心业务逻辑
│   ├── service/          #   领域服务
│   ├── bo/               #   业务对象
│   └── converter/        #   BO ↔ Entity 转换器
├── infra/                # 基础设施层 - 数据访问
│   ├── entity/           #   数据库实体
│   └── mapper/           #   MyBatis Mapper
├── common/               # 公共组件
│   └── StpInterfaceImpl  #   Sa-Token 权限接口实现
├── config/               # 配置类
│   ├── MybatisPlusConfig #   分页插件
│   ├── MyMetaObjectHandler # 自动填充
│   └── GlobalExceptionHandler # 全局异常
└── AuthApplication.java  # 启动类
```

### 1.2 数据流转图

```
客户端请求
    ↓
[Controller] 接收 DTO
    ↓ UserDTOConverter.INSTANCE.convertToBO(dto)
[Domain Service] 处理 BO
    ↓ UserBOConverter.INSTANCE.convertToEntity(bo)
[Mapper] 操作 Entity
    ↓
数据库
```

**为什么需要三层转换？**

- **DTO** (Data Transfer Object): 网络传输对象，面向 API 消费者
- **BO** (Business Object): 业务对象，封装业务逻辑所需数据
- **Entity**: 数据库实体，与表结构一一对应

分层隔离的好处：

1. API 变更不影响数据库结构
2. 业务逻辑不依赖框架注解
3. 每层可以有自己的校验规则

### 1.3 数据库设计 - RBAC 模型

```
auth_user ──┐
            ├── auth_user_role ──┐
auth_role ──┘                    ├── auth_role_permission ── auth_permission
                                 └────────────────────────┘
```

**5 张核心表：**

| 表名                     | 说明      | 关键字段                               |
| ---------------------- | ------- | ---------------------------------- |
| auth\_user             | 用户信息    | user\_name, nick\_name, password   |
| auth\_role             | 角色      | role\_name, role\_key              |
| auth\_permission       | 权限      | name, permission\_key, type(菜单/操作) |
| auth\_user\_role       | 用户-角色关联 | user\_id, role\_id                 |
| auth\_role\_permission | 角色-权限关联 | role\_id, permission\_id           |

**逻辑删除**: 所有表都有 `is_deleted` 字段，MyBatis-Plus 自动处理

### 1.4 Entity 实体类

每个 Entity 对应一张数据库表，使用 MyBatis-Plus 注解：

```java
@TableName("auth_user")          // 表名映射
public class AuthUser {
    @TableId(type = IdType.AUTO) // 自增主键
    private Long id;
    
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充
    private Date updateTime;
    
    @TableLogic                    // 逻辑删除
    private Integer isDeleted;
}
```

**关键注解说明：**

- `@TableName`: 指定表名
- `@TableId(type = IdType.AUTO)`: 自增主键
- `@TableField(fill = ...)`: 自动填充字段
- `@TableLogic`: 逻辑删除（查询时自动加 `is_deleted = 0`）

### 1.5 Mapper 接口

继承 `BaseMapper<T>` 获得基础 CRUD，自定义方法用 `@Select` 注解：

```java
@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUser> {
    
    // 自定义联表查询
    @Select("SELECT r.role_key FROM auth_role r " +
            "INNER JOIN auth_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<String> selectRolesByUserId(@Param("userId") Long userId);
}
```

### 1.6 MapStruct 转换器

MapStruct 在编译期生成转换代码，比反射快：

```java
@Mapper  // org.mapstruct.Mapper
public interface UserBOConverter {
    // 单例模式，直接使用
    UserBOConverter INSTANCE = Mappers.getMapper(UserBOConverter.class);
    
    // 自动映射同名字段
    UserBO convertToBO(AuthUser entity);
    AuthUser convertToEntity(UserBO bo);
}
```

**两层转换器：**

1. `UserBOConverter` (domain/converter): Entity ↔ BO
2. `UserDTOConverter` (application/converter): BO ↔ DTO

### 1.7 Domain Service 领域服务

核心业务逻辑层，不依赖任何 Web 框架：

```java
@Service
public class UserDomainService {
    @Resource
    private AuthUserMapper authUserMapper;
    
    public UserBO getUserById(Long id) {
        AuthUser user = authUserMapper.selectById(id);
        return UserBOConverter.INSTANCE.convertToBO(user);
    }
    
    public void addUser(UserBO userBO) {
        AuthUser entity = UserBOConverter.INSTANCE.convertToEntity(userBO);
        authUserMapper.insert(entity);
    }
}
```

### 1.8 Controller 控制器

REST API 入口，只做参数转换和调用：

```java
@RestController
@RequestMapping("/auth/user")
public class UserController {
    @Resource
    private UserDomainService userDomainService;
    
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        // 1. 查询用户
        UserBO userBO = userDomainService.getUserByName(loginDTO.getUserName());
        // 2. 校验密码
        if (userBO == null) return Result.fail("用户不存在");
        // 3. Sa-Token 登录
        StpUtil.login(userBO.getId());
        // 4. 返回 Token
        return Result.success(StpUtil.getTokenValue());
    }
}
```

### 1.9 Sa-Token 集成

**Sa-Token 是什么？**
国产轻量级认证框架，比 Spring Security 简单很多。

**核心 API：**

```java
StpUtil.login(userId);           // 登录
StpUtil.logout();                // 登出
StpUtil.getLoginIdAsLong();      // 获取当前登录用户ID
StpUtil.getTokenValue();         // 获取当前 Token
StpUtil.checkLogin();            // 检查是否登录
```

**StpInterface - 权限认证接口：**

```java
@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回用户的权限标识列表
        return userDomainService.getPermissionsByUserId(Long.parseLong(loginId.toString()));
    }
    
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回用户的角色标识列表
        return userDomainService.getRolesByUserId(Long.parseLong(loginId.toString()));
    }
}
```

**权限校验使用：**

```java
@SaCheckPermission("subject:add")  // 需要 subject:add 权限
@PostMapping("/add")
public Result<Void> addSubject(...) { ... }

@SaCheckRole("admin_user")         // 需要 admin_user 角色
@PostMapping("/delete")
public Result<Void> deleteSubject(...) { ... }
```

### 1.10 MyBatis-Plus 自动填充

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "createdBy", String.class, getCurrentUser());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }
}
```

配合 Entity 上的 `@TableField(fill = FieldFill.INSERT)` 使用，自动填充创建时间、更新人等字段。

### 1.11 分页配置

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 1.12 API 接口清单

| 方法   | 路径                       | 说明     | 权限  |
| ---- | ------------------------ | ------ | --- |
| POST | /auth/user/login         | 用户登录   | 公开  |
| POST | /auth/user/logout        | 用户登出   | 登录  |
| GET  | /auth/user/info          | 获取用户信息 | 登录  |
| POST | /auth/user/add           | 新增用户   | 管理员 |
| POST | /auth/user/update        | 更新用户   | 登录  |
| GET  | /auth/user/list          | 用户列表   | 管理员 |
| GET  | /auth/role/list/{userId} | 获取用户角色 | 管理员 |
| POST | /auth/role/add           | 新增角色   | 管理员 |
| POST | /auth/role/assign        | 分配角色   | 管理员 |

### 1.13 关键设计模式

**模板方法模式** - MyBatis-Plus 的 `BaseMapper` 提供基础 CRUD，子类扩展自定义方法

**单例模式** - MapStruct Converter 使用 `INSTANCE` 静态常量

**依赖注入** - Spring 的 `@Resource` / `@Autowired` 实现层间解耦

### 1.14 启动与验证

**启动命令：**

```bash
mvn clean package -pl colacode-common,colacode-auth -am -DskipTests
java -jar colacode-auth/target/colacode-auth-1.0.0-SNAPSHOT.jar
```

**成功启动日志关键信息：**

```
Tomcat started on port(s): 3011 (http) with context path ''
{dataSource-1} inited
MyBatis-Plus 3.4.3.4
Sa-Token v1.37.0
```

**API 测试：**

```bash
curl -X POST http://localhost:3011/auth/user/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"test","password":"test"}'
```

***

## 第二章：Gateway 网关服务

### 2.1 为什么需要网关？

微服务架构中，每个服务都有自己的端口和 API。网关作为**统一入口**，解决以下问题：

- **统一路由**: 客户端只需知道网关地址，不需要知道每个服务的地址
- **统一鉴权**: 在网关层统一校验 Token，避免每个服务重复实现
- **统一异常处理**: 集中处理跨服务异常
- **负载均衡**: 配合 Nacos 实现服务实例的负载均衡

### 2.2 Spring Cloud Gateway 核心概念

Gateway 基于 **WebFlux**（响应式编程），与传统 Spring MVC 不同：

```
客户端请求 → Gateway (5000) → 路由匹配 → 目标服务
                                    ↓
                              Predicate (断言) → 匹配成功
                                    ↓
                              Filter (过滤器) → 前置/后置处理
                                    ↓
                              转发到目标服务
```

**三大核心组件：**

| 组件                 | 说明                            | 类比      |
| ------------------ | ----------------------------- | ------- |
| **Route (路由)**     | 网关的基本构建块，包含 ID、目标 URI、断言和过滤器  | 一条转发规则  |
| **Predicate (断言)** | 匹配 HTTP 请求的条件（路径、方法、Header 等） | if 条件判断 |
| **Filter (过滤器)**   | 在请求转发前后修改请求/响应                | 中间件     |

### 2.3 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: colacode-auth           # 路由唯一标识
          uri: http://127.0.0.1:3011  # 目标服务地址
          predicates:
            - Path=/auth/**           # 匹配路径
        - id: colacode-subject
          uri: http://127.0.0.1:3010
          predicates:
            - Path=/subject/**
```

**生产环境使用服务发现（lb://）：**

```yaml
uri: lb://colacode-auth  # lb = LoadBalancer，从 Nacos 获取实例
```

### 2.4 Sa-Token 响应式集成

Gateway 基于 WebFlux（响应式），不能使用传统的 Servlet Filter，必须使用 **SaReactorFilter**：

```java
@Bean
public SaReactorFilter getSaReactorFilter() {
    return new SaReactorFilter()
            .addInclude("/**")                    // 拦截所有请求
            .addExclude("/favicon.ico")           // 排除静态资源
            .setAuth(obj -> {
                SaRouter.match("/**")
                        .notMatch("/auth/user/login")  // 登录接口不需要鉴权
                        .notMatch("/auth/user/logout")
                        .notMatch("/wx/**")            // 微信回调不需要鉴权
                        .check(r -> StpUtil.checkLogin());
            });
}
```

**关键区别：**

- 传统 Spring MVC: `SaServletFilter` (基于 Servlet API)
- 响应式 WebFlux: `SaReactorFilter` (基于 Reactor)

### 2.5 全局异常处理

Gateway 的异常处理需要实现 `ErrorWebExceptionHandler`，返回响应式 `Mono<Void>`：

```java
@Order(-1)  // 优先级最高
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("code", response.getStatusCode().value());
        result.put("message", ex.getMessage());
        
        return response.writeWith(Mono.fromSupplier(() ->
                response.bufferFactory().wrap(JSON.toJSONBytes(result))
        ));
    }
}
```

### 2.6 Gateway 项目结构

```
colacode-gateway/
├── filter/
│   └── LoginFilter.java              # Sa-Token 全局鉴权过滤器
├── exception/
│   └── GatewayExceptionHandler.java  # 全局异常处理
├── config/
│   └── SaTokenConfig.java            # Sa-Token 配置
└── GatewayApplication.java           # 启动类
```

### 2.7 请求链路图

```
浏览器
  ↓ POST /auth/user/login
Gateway (:5000)
  ↓ SaReactorFilter 放行（notMatch /auth/user/login）
  ↓ 路由匹配 Path=/auth/** → uri=http://127.0.0.1:3011
Auth Service (:3011)
  ↓ UserController.login()
  ↓ StpUtil.login(userId) → Redis 写入 Session
  ↓ 返回 Token
Gateway
  ↓ 响应返回浏览器
浏览器获得 Token
  ↓ 后续请求携带 satoken
Gateway
  ↓ SaReactorFilter 校验 Token
  ↓ 校验通过 → 转发到目标服务
  ↓ 校验失败 → 返回 401
```

***

## 踩坑记录

### 1. MySQL 字符集

```
ERROR 1273 (HY000): Unknown collation: 'utf8mb4'
```

**解决**: `utf8mb4` 是字符集，需要配合排序规则：`utf8mb4_general_ci`

### 2. PowerShell 大括号展开

PowerShell 不支持 bash 的 `{a,b,c}` 路径展开语法，需要用 `New-Item` 逐个创建。

### 3. SQL 文件编码

从原项目复制 SQL 时，如果编码处理不当，中文注释会变成乱码。使用 `Get-Content -Encoding UTF8` 读取。

### 4. Spring Boot Maven Plugin 版本冲突

```
class file version 61.0, this version only recognizes up to 52.0
```

Maven 自动下载了 Spring Boot 4.1.0-M4 插件（需要 Java 17+），而我们用的是 Java 8。
**解决**: 在父 pom.xml 的 `<pluginManagement>` 中锁定版本为 `${spring-boot.version}` (2.4.2)

### 5. Spring Boot 可执行 JAR 缺少 Manifest

```
xxx.jar中没有主清单属性
```

**解决**: 在子模块 pom.xml 中添加 `spring-boot-maven-plugin` 的 `repackage` goal

### 6. MySQL 密码错误

```
Access denied for user 'root'@'localhost' (using password: YES)
```

检查 application.yml 中的 `spring.datasource.password` 是否与本地 MySQL 一致。

### 7. Nacos 注册超时导致启动慢

Nacos 远程服务器连接超时（30秒），本地开发可临时禁用：

```yaml
spring.cloud.nacos.discovery.enabled: false
spring.cloud.nacos.config.enabled: false
```

### 8. Gateway 响应式编程

Gateway 基于 WebFlux，不能使用传统的 `HttpServletRequest`，需要使用 `ServerHttpRequest`。Sa-Token 也必须使用 `sa-token-reactor-spring-boot-starter` 而非普通版本。

***

## 学习总结

### Auth 服务学到的核心知识点

1. ✅ DDD 四层架构的实际应用
2. ✅ DTO → BO → Entity 三层数据转换
3. ✅ MyBatis-Plus 基础使用（CRUD、分页、逻辑删除、自动填充）
4. ✅ MapStruct 对象映射
5. ✅ Sa-Token 认证授权集成
6. ✅ RBAC 权限模型实现
7. ✅ 全局异常处理
8. ✅ RESTful API 设计
9. ✅ Maven 多模块项目构建与打包

### Gateway 服务学到的核心知识点

1. ✅ Spring Cloud Gateway 路由配置
2. ✅ Predicate 断言匹配
3. ✅ SaReactorFilter 响应式鉴权
4. ✅ WebFlux 响应式编程基础
5. ✅ 全局异常处理（ErrorWebExceptionHandler）
6. ✅ 网关请求链路理解
7. ✅ 传统 Servlet vs 响应式 WebFlux 的区别

