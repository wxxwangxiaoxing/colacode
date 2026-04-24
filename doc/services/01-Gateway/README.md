# Gateway 网关服务

## 概述

API 网关服务，统一入口，负责路由转发、鉴权放行、限流、OpenAPI 聚合。

## 基本信息

- **默认端口**: 5000
- **技术栈**: Spring Cloud Gateway (WebFlux)
- **相关服务**: 所有业务服务

## 核心功能

- 统一路由
- 统一鉴权 (Sa-Token)
- 全局异常处理
- OpenAPI 文档聚合

## API 路由

| 前缀 | 目标服务 |
|------|----------|
| `/auth/**` | colacode-auth |
| `/subject/**` | colacode-subject |
| `/practice/**` | colacode-practice |
| `/circle/**` | colacode-circle |
| `/interview/**` | colacode-interview |
| `/oss/**` | colacode-oss |
| `/wx/**` | colacode-wx |

## 待补充

- [ ] 详细架构说明
- [ ] 过滤器实现细节
- [ ] 限流配置说明