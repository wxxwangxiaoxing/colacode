# Gateway 网关服务

## 概述

`colacode-gateway` 是项目统一入口，负责路由转发、登录态透传、鉴权放行、 Sentinel 限流和 OpenAPI 聚合。

## 基本信息

- 默认端口：`5000`
- 技术栈：Spring Cloud Gateway + WebFlux + Sa-Token + Sentinel
- 入口地址：`http://localhost:5000`
- Swagger 聚合地址：`http://localhost:5000/swagger-ui.html`

## 核心职责

- 统一暴露业务服务入口，避免前端直连各微服务
- 对登录态进行校验，并把用户标识透传给下游服务
- 聚合各服务的 OpenAPI 文档
- 对路由和关键接口施加限流规则

## 关键组件

- `GatewayRouteConfig`：根据 `colacode.gateway.routes.definitions` 动态注册路由
- `LoginFilter`：基于 `exclude-paths` 判断哪些路径可匿名访问
- `LoginUserHeaderFilter`：将当前登录用户信息写入请求头，供下游服务使用
- `SentinelGatewayConfig`：注册路由级和 API 级限流规则

## 路由前缀

| 前缀 | 目标服务 |
| --- | --- |
| `/auth/**` | `colacode-auth` |
| `/subject/**` | `colacode-subject` |
| `/practice/**` | `colacode-practice` |
| `/circle/**` | `colacode-circle` |
| `/interview/**` | `colacode-interview` |
| `/oss/**` | `colacode-oss` |
| `/wx/**` | `colacode-wx` |

OpenAPI 也通过网关聚合，例如：

- `/auth/v3/api-docs`
- `/practice/v3/api-docs`
- `/interview/v3/api-docs`

## 鉴权放行

匿名放行路径由 `colacode.gateway.auth.exclude-paths` 配置，当前主要包括：

- 登录、注册、登录态检查
- Swagger / OpenAPI 相关路径
- 部分题库公开查询接口
- OSS 部分开放接口
- 微信与健康检查接口

精确列表请以 `colacode-gateway/src/main/resources/application.yml` 和 `application-local.yml` 为准。

## 限流

限流由 `SentinelGatewayConfig` 读取 `colacode.gateway.rate-limit` 配置注册：

- 路由级限流：按服务维度限制整体流量
- API 级限流：对登录、注册、上传等高风险接口单独收敛

默认已配置：

- `colacode-auth`
- `colacode-subject`
- `colacode-practice`
- `colacode-circle`
- `colacode-interview`
- `colacode-oss`
- `colacode-wx`

## Local Profile

`local` 模式不依赖 Nacos，而是直接把路由指向固定端口：

- `BOOT3_AUTH_ROUTE`
- `BOOT3_SUBJECT_ROUTE`
- `BOOT3_PRACTICE_ROUTE`
- `BOOT3_CIRCLE_ROUTE`
- `BOOT3_INTERVIEW_ROUTE`
- `BOOT3_OSS_ROUTE`
- `BOOT3_WX_ROUTE`

示例：

```bash
mvn -pl colacode-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## 联调建议

- 前端和外部调用统一走网关，不要绕过网关直连业务服务
- 本地联调时先确保被代理的服务已经启动
- 如果 Swagger 页面空白，先检查对应下游服务的 `/v3/api-docs` 是否可访问
