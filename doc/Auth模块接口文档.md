# ColaCode Auth 模块接口文档

> 版本: 1.0.0 | 最后更新: 2026-04-08

---

## 1. 接口概述

Auth 模块是 ColaCode 平台的认证授权中心，基于 **RBAC（基于角色的访问控制）** 模型，提供用户管理、角色管理、权限管理三大核心能力。

### 主要功能

- **用户认证**：登录、登出、登录状态检测
- **用户管理**：注册、增删改查、状态变更、批量查询
- **角色管理**：角色增删改查、角色分配
- **权限管理**：权限增删改查、权限树、角色权限分配
- **安全控制**：登录频率限制、失败锁定、管理员保护

### 技术架构

| 组件 | 说明 |
|------|------|
| 认证框架 | Sa-Token (JWT) |
| 密码加密 | BCrypt |
| ORM | MyBatis-Plus |
| 服务端口 | 3011 |
| API 前缀 | `/auth` |

### 基础信息

- **Base URL**: `http://{host}:3011`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

---

## 2. 接口列表

### 2.1 用户管理 (`/auth/user`)

| # | 接口名称 | URL | 方法 | 权限 |
|---|---------|-----|------|------|
| 1 | 用户登录 | `/auth/user/login` | POST | 无 |
| 2 | 用户登出 | `/auth/user/logout` | POST | 登录用户 |
| 3 | 检查登录状态 | `/auth/user/isLogin` | GET | 无 |
| 4 | 获取当前用户信息 | `/auth/user/info` | GET | 登录用户 |
| 5 | 用户注册 | `/auth/user/register` | POST | 无 |
| 6 | 添加用户 | `/auth/user/add` | POST | 管理员 |
| 7 | 更新用户信息 | `/auth/user/update` | POST | 本人或管理员 |
| 8 | 删除用户 | `/auth/user/delete` | POST | 管理员 |
| 9 | 修改用户状态 | `/auth/user/changeStatus` | POST | 管理员 |
| 10 | 获取用户列表 | `/auth/user/list` | GET | 管理员 |
| 11 | 批量获取用户 | `/auth/user/listByIds` | POST | 管理员 |

### 2.2 角色管理 (`/auth/role`)

| # | 接口名称 | URL | 方法 | 权限 |
|---|---------|-----|------|------|
| 12 | 获取用户角色 | `/auth/role/list/{userId}` | GET | 本人或管理员 |
| 13 | 添加角色 | `/auth/role/add` | POST | 管理员 |
| 14 | 更新角色 | `/auth/role/update` | POST | 管理员 |
| 15 | 删除角色 | `/auth/role/delete` | POST | 管理员 |
| 16 | 分配角色给用户 | `/auth/role/assign` | POST | 管理员 |

### 2.3 权限管理 (`/auth/permission`)

| # | 接口名称 | URL | 方法 | 权限 |
|---|---------|-----|------|------|
| 17 | 获取权限树 | `/auth/permission/tree` | GET | 管理员 |
| 18 | 获取用户权限 | `/auth/permission/user/{userId}` | GET | 本人或管理员 |
| 19 | 添加权限 | `/auth/permission/add` | POST | 管理员 |
| 20 | 更新权限 | `/auth/permission/update` | POST | 管理员 |
| 21 | 删除权限 | `/auth/permission/delete` | POST | 管理员 |

### 2.4 角色权限管理 (`/auth/rolePermission`)

| # | 接口名称 | URL | 方法 | 权限 |
|---|---------|-----|------|------|
| 22 | 分配权限给角色 | `/auth/rolePermission/assign` | POST | 管理员 |

---

## 3. 接口详情

### 3.1 用户登录

用户通过用户名和密码进行身份认证，成功后返回 Sa-Token 令牌。

**请求**

```
POST /auth/user/login
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Content-Type | application/json | 是 | 请求体格式 |

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| userName | String | 是 | 最大32字符 | 用户名 |
| password | String | 是 | 6~64字符 | 密码 |

**请求示例**

```http
POST /auth/user/login HTTP/1.1
Content-Type: application/json

{
  "userName": "zhangsan",
  "password": "123456"
}
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否成功 |
| code | int | 业务状态码 |
| message | String | 提示信息 |
| data | String | Sa-Token 令牌值 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40006,
  "message": "用户名或密码错误",
  "data": null
}
```

**状态码说明**

| 业务码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| 20000 | 200 | 登录成功 |
| 40000 | 400 | 用户名和密码不能为空 |
| 40006 | 400 | 用户名或密码错误 |
| 40305 | 403 | 账号已被禁用 |
| 42900 | 429 | 登录过于频繁/失败次数过多被锁定 |

---

### 3.2 用户登出

退出当前登录状态，使令牌失效。

**请求**

```
POST /auth/user/logout
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 登录令牌 |

**请求示例**

```http
POST /auth/user/logout HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.3 检查登录状态

检测当前请求是否携带有效令牌。

**请求**

```
GET /auth/user/isLogin
```

**请求示例**

```http
GET /auth/user/isLogin HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| data | boolean | 是否已登录 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": true
}
```

---

### 3.4 获取当前用户信息

获取当前登录用户的详细信息。

**请求**

```
GET /auth/user/info
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 登录令牌 |

**请求示例**

```http
GET /auth/user/info HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| data.id | Long | 用户ID |
| data.userName | String | 用户名 |
| data.nickName | String | 昵称 |
| data.email | String | 邮箱 |
| data.phone | String | 手机号 |
| data.sex | Integer | 性别：0未知 1男 2女 |
| data.avatar | String | 头像URL |
| data.status | Integer | 状态：0启用 1禁用 |
| data.introduce | String | 个人介绍 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userName": "zhangsan",
    "nickName": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "sex": 1,
    "avatar": "https://oss.colacode.com/avatar/default.png",
    "status": 0,
    "introduce": "全栈开发工程师"
  }
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40100,
  "message": "未登录或登录已过期",
  "data": null
}
```

---

### 3.5 用户注册

新用户自助注册账号。

**请求**

```
POST /auth/user/register
```

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| userName | String | 是 | 最大32字符 | 用户名 |
| nickName | String | 否 | 最大32字符 | 昵称 |
| email | String | 否 | 邮箱格式，最大64字符 | 邮箱 |
| phone | String | 否 | `^1\d{10}$` | 手机号 |
| password | String | 是 | 6~64字符 | 密码 |
| sex | Integer | 否 | 0未知 1男 2女 | 性别 |
| avatar | String | 否 | 最大255字符 | 头像URL |
| status | Integer | 否 | 0启用 1禁用，默认0 | 状态 |
| introduce | String | 否 | 最大255字符 | 个人介绍 |

**请求示例**

```http
POST /auth/user/register HTTP/1.1
Content-Type: application/json

{
  "userName": "lisi",
  "nickName": "李四",
  "email": "lisi@example.com",
  "phone": "13900139000",
  "password": "abc123",
  "sex": 1,
  "introduce": "Java后端开发"
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40001,
  "message": "用户名已存在",
  "data": null
}
```

**状态码说明**

| 业务码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| 20000 | 200 | 注册成功 |
| 40000 | 400 | 参数校验失败 |
| 40001 | 400 | 用户名已存在 |

---

### 3.6 添加用户

管理员创建新用户。

**请求**

```
POST /auth/user/add
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| userName | String | 否 | 最大32字符 | 用户名 |
| nickName | String | 否 | 最大32字符 | 昵称 |
| email | String | 否 | 最大64字符 | 邮箱 |
| phone | String | 否 | - | 手机号 |
| password | String | 否 | 仅写入，不返回 | 密码（明文，系统自动加密） |
| sex | Integer | 否 | - | 性别 |
| avatar | String | 否 | 最大255字符 | 头像URL |
| status | Integer | 否 | 默认0 | 状态 |
| introduce | String | 否 | 最大255字符 | 个人介绍 |

**请求示例**

```http
POST /auth/user/add HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "userName": "wangwu",
  "nickName": "王五",
  "email": "wangwu@example.com",
  "password": "123456",
  "sex": 2,
  "status": 0,
  "introduce": "前端开发工程师"
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40301,
  "message": "仅管理员可执行此操作",
  "data": null
}
```

---

### 3.7 更新用户信息

更新用户信息。不传 `id` 时默认修改当前登录用户；传 `id` 时需管理员权限或为本人。

**请求**

```
POST /auth/user/update
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 登录令牌 |

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| id | Long | 否 | 不传则修改当前用户 | 用户ID |
| userName | String | 否 | 最大32字符 | 用户名 |
| nickName | String | 否 | 最大32字符 | 昵称 |
| email | String | 否 | 邮箱格式，最大64字符 | 邮箱 |
| phone | String | 否 | `^1\d{10}$` | 手机号 |
| password | String | 否 | 6~64字符，仅写入 | 新密码 |
| sex | Integer | 否 | - | 性别 |
| avatar | String | 否 | 最大255字符 | 头像URL |
| status | Integer | 否 | - | 状态 |
| introduce | String | 否 | 最大255字符 | 个人介绍 |

**请求示例**

```http
POST /auth/user/update HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "nickName": "张三丰",
  "introduce": "资深全栈工程师"
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40302,
  "message": "无权限操作该用户",
  "data": null
}
```

---

### 3.8 删除用户

管理员逻辑删除用户。不允许删除自己，且至少保留一个管理员账号。

**请求**

```
POST /auth/user/delete
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 待删除的用户ID |

**请求示例**

```http
POST /auth/user/delete HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 5
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

| 业务码 | 说明 |
|--------|------|
| 40000 | 用户ID不能为空 |
| 40002 | 不能删除当前登录用户 |
| 40004 | 至少保留一个管理员账号 |
| 40401 | 用户不存在 |

---

### 3.9 修改用户状态

管理员启用或禁用用户。不允许禁用自己，且至少保留一个启用中的管理员。

**请求**

```
POST /auth/user/changeStatus
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |
| status | Integer | 是 | 状态：0启用 1禁用 |

**请求示例**

```http
POST /auth/user/changeStatus HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 3,
  "status": 1
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

| 业务码 | 说明 |
|--------|------|
| 40003 | 不能禁用当前登录用户 |
| 40005 | 至少保留一个启用中的管理员账号 |
| 40401 | 用户不存在 |

---

### 3.10 获取用户列表

管理员分页查询用户列表。

**请求**

```
GET /auth/user/list?pageNo=1&pageSize=10
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**查询参数**

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNo | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**请求示例**

```http
GET /auth/user/list?pageNo=1&pageSize=10 HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| data.pageNo | int | 当前页码 |
| data.pageSize | int | 每页条数 |
| data.total | long | 总记录数 |
| data.records | Array\<UserDTO\> | 用户列表 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 25,
    "records": [
      {
        "id": 1,
        "userName": "admin",
        "nickName": "管理员",
        "email": "admin@colacode.com",
        "phone": "13800000001",
        "sex": 1,
        "avatar": "https://oss.colacode.com/avatar/admin.png",
        "status": 0,
        "introduce": "系统管理员"
      },
      {
        "id": 2,
        "userName": "zhangsan",
        "nickName": "张三",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "sex": 1,
        "avatar": null,
        "status": 0,
        "introduce": "全栈开发工程师"
      }
    ]
  }
}
```

---

### 3.11 批量获取用户

管理员根据用户ID列表批量查询用户信息。

**请求**

```
POST /auth/user/listByIds
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| - | Array\<Long\> | 是 | 用户ID列表 |

**请求示例**

```http
POST /auth/user/listByIds HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

[1, 2, 3]
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userName": "admin",
      "nickName": "管理员",
      "email": "admin@colacode.com",
      "phone": "13800000001",
      "sex": 1,
      "avatar": "https://oss.colacode.com/avatar/admin.png",
      "status": 0,
      "introduce": "系统管理员"
    },
    {
      "id": 2,
      "userName": "zhangsan",
      "nickName": "张三",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "sex": 1,
      "avatar": null,
      "status": 0,
      "introduce": "全栈开发工程师"
    }
  ]
}
```

---

### 3.12 获取用户角色

查询指定用户拥有的角色列表。

**请求**

```
GET /auth/role/list/{userId}
```

**路径参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求示例**

```http
GET /auth/role/list/1 HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| data[].id | Long | 角色ID |
| data[].roleName | String | 角色名称 |
| data[].roleKey | String | 角色唯一标识 |
| data[].permissionIds | Array\<Long\> | 角色关联的权限ID列表 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "roleName": "管理员",
      "roleKey": "admin",
      "permissionIds": [1, 2, 3, 4]
    }
  ]
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40303,
  "message": "无权限查看该用户角色",
  "data": null
}
```

---

### 3.13 添加角色

创建新角色，可同时关联权限。

**请求**

```
POST /auth/role/add
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 否 | 角色名称 |
| roleKey | String | 否 | 角色唯一标识 |
| permissionIds | Array\<Long\> | 否 | 关联的权限ID列表 |

**请求示例**

```http
POST /auth/role/add HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "roleName": "普通用户",
  "roleKey": "user",
  "permissionIds": [1]
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.14 更新角色

更新角色信息，若传 `permissionIds` 则同步更新角色权限关联。

**请求**

```
POST /auth/role/update
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 角色ID |
| roleName | String | 否 | 角色名称 |
| roleKey | String | 否 | 角色唯一标识 |
| permissionIds | Array\<Long\> | 否 | 关联的权限ID列表 |

**请求示例**

```http
POST /auth/role/update HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 2,
  "roleName": "高级用户",
  "roleKey": "advanced_user",
  "permissionIds": [1, 2]
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40000,
  "message": "角色ID不能为空",
  "data": null
}
```

---

### 3.15 删除角色

逻辑删除角色，同时清除该角色的权限关联和用户关联。

**请求**

```
POST /auth/role/delete
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 角色ID |

**请求示例**

```http
POST /auth/role/delete HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 3
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.16 分配角色给用户

为指定用户分配一个角色。

**请求**

```
POST /auth/role/assign
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| roleId | Long | 否 | 角色ID |

**请求示例**

```http
POST /auth/role/assign HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "userId": 2,
  "roleId": 1
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.17 获取权限树

获取完整的权限树形结构。

**请求**

```
GET /auth/permission/tree
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求示例**

```http
GET /auth/permission/tree HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| data[].id | Long | 权限ID |
| data[].name | String | 权限名称 |
| data[].parentId | Long | 父级权限ID |
| data[].type | Integer | 权限类型：0菜单 1操作 |
| data[].menuUrl | String | 菜单路由 |
| data[].status | Integer | 状态：0启用 1禁用 |
| data[].show | Integer | 展示状态：0展示 1隐藏 |
| data[].icon | String | 图标 |
| data[].permissionKey | String | 权限唯一标识 |
| data[].children | Array\<PermissionDTO\> | 子权限列表 |

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "权限管理",
      "parentId": 0,
      "type": 0,
      "menuUrl": "/auth",
      "status": 0,
      "show": 0,
      "icon": "setting",
      "permissionKey": "auth:manage",
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "parentId": 1,
          "type": 1,
          "menuUrl": "/auth/user",
          "status": 0,
          "show": 0,
          "icon": "user",
          "permissionKey": "user:manage",
          "children": []
        },
        {
          "id": 3,
          "name": "角色管理",
          "parentId": 1,
          "type": 1,
          "menuUrl": "/auth/role",
          "status": 0,
          "show": 0,
          "icon": "team",
          "permissionKey": "role:manage",
          "children": []
        },
        {
          "id": 4,
          "name": "权限项管理",
          "parentId": 1,
          "type": 1,
          "menuUrl": "/auth/permission",
          "status": 0,
          "show": 0,
          "icon": "safety",
          "permissionKey": "permission:manage",
          "children": []
        }
      ]
    }
  ]
}
```

---

### 3.18 获取用户权限

查询指定用户拥有的权限树形结构。

**请求**

```
GET /auth/permission/user/{userId}
```

**路径参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求示例**

```http
GET /auth/permission/user/1 HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**成功响应**

响应结构与 [3.17 获取权限树](#317-获取权限树) 一致，但仅包含该用户通过角色关联获得的权限。

**错误响应**

```json
{
  "success": false,
  "code": 40304,
  "message": "无权限查看该用户权限",
  "data": null
}
```

---

### 3.19 添加权限

创建新的权限项。

**请求**

```
POST /auth/permission/add
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 权限名称 |
| parentId | Long | 否 | 父级权限ID，不传默认0（顶级） |
| type | Integer | 否 | 权限类型：0菜单 1操作 |
| menuUrl | String | 否 | 菜单路由 |
| status | Integer | 否 | 状态：0启用 1禁用 |
| show | Integer | 否 | 展示状态：0展示 1隐藏 |
| icon | String | 否 | 图标 |
| permissionKey | String | 否 | 权限唯一标识 |

**请求示例**

```http
POST /auth/permission/add HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "name": "题库管理",
  "parentId": 0,
  "type": 0,
  "menuUrl": "/subject",
  "status": 0,
  "show": 0,
  "icon": "book",
  "permissionKey": "subject:manage"
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.20 更新权限

更新权限项信息。

**请求**

```
POST /auth/permission/update
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 权限ID |
| name | String | 否 | 权限名称 |
| parentId | Long | 否 | 父级权限ID |
| type | Integer | 否 | 权限类型 |
| menuUrl | String | 否 | 菜单路由 |
| status | Integer | 否 | 状态 |
| show | Integer | 否 | 展示状态 |
| icon | String | 否 | 图标 |
| permissionKey | String | 否 | 权限唯一标识 |

**请求示例**

```http
POST /auth/permission/update HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 5,
  "name": "题库管理（升级版）",
  "icon": "read"
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40000,
  "message": "权限ID不能为空",
  "data": null
}
```

---

### 3.21 删除权限

逻辑删除权限项。

**请求**

```
POST /auth/permission/delete
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 权限ID |

**请求示例**

```http
POST /auth/permission/delete HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "id": 5
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

---

### 3.22 分配权限给角色

为指定角色重新分配权限列表（全量覆盖）。

**请求**

```
POST /auth/rolePermission/assign
```

**请求头**

| 名称 | 值 | 必填 | 说明 |
|------|-----|------|------|
| Authorization | Bearer {token} | 是 | 管理员令牌 |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | Long | 是 | 角色ID |
| permissionIds | Array\<Long\> | 否 | 权限ID列表（全量覆盖） |

**请求示例**

```http
POST /auth/rolePermission/assign HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "roleId": 1,
  "permissionIds": [1, 2, 3, 4]
}
```

**成功响应**

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": null
}
```

**错误响应**

```json
{
  "success": false,
  "code": 40000,
  "message": "角色ID不能为空",
  "data": null
}
```

---

## 4. 权限说明

### 4.1 权限等级

| 等级 | 说明 | 判定条件 |
|------|------|---------|
| **无** | 无需任何认证 | 公开接口 |
| **登录用户** | 需要有效的 Sa-Token 令牌 | `StpUtil.isLogin() == true` |
| **本人或管理员** | 资源所有者或管理员 | `canAccessUser(userId)` |
| **管理员** | 仅管理员角色 | `hasAdminAccess()` |

### 4.2 管理员判定规则

用户满足以下任一条件即为管理员：

| 条件 | 说明 |
|------|------|
| 角色标识包含 `admin` | 拥有管理员角色 |
| 权限标识包含 `auth:manage` | 拥有权限管理权限 |
| 权限标识包含 `user:manage` | 拥有用户管理权限 |
| 权限标识包含 `role:manage` | 拥有角色管理权限 |
| 权限标识包含 `permission:manage` | 拥有权限项管理权限 |

### 4.3 接口权限矩阵

| 接口 | 无认证 | 登录用户 | 本人 | 管理员 |
|------|--------|---------|------|--------|
| 登录 | ✅ | - | - | - |
| 登出 | - | ✅ | - | - |
| 检查登录状态 | ✅ | - | - | - |
| 获取当前用户信息 | - | ✅ | - | - |
| 注册 | ✅ | - | - | - |
| 添加用户 | - | - | - | ✅ |
| 更新用户信息 | - | - | ✅ | ✅ |
| 删除用户 | - | - | - | ✅ |
| 修改用户状态 | - | - | - | ✅ |
| 获取用户列表 | - | - | - | ✅ |
| 批量获取用户 | - | - | - | ✅ |
| 获取用户角色 | - | - | ✅ | ✅ |
| 添加角色 | - | - | - | ✅ |
| 更新角色 | - | - | - | ✅ |
| 删除角色 | - | - | - | ✅ |
| 分配角色给用户 | - | - | - | ✅ |
| 获取权限树 | - | - | - | ✅ |
| 获取用户权限 | - | - | ✅ | ✅ |
| 添加权限 | - | - | - | ✅ |
| 更新权限 | - | - | - | ✅ |
| 删除权限 | - | - | - | ✅ |
| 分配权限给角色 | - | - | - | ✅ |

### 4.4 登录安全策略

| 策略 | 值 | 说明 |
|------|-----|------|
| 每分钟最大尝试次数 | 10 | 超过限制返回频率限制提示 |
| 最大失败次数 | 5 | 超过限制锁定账号 |
| 尝试窗口期 | 60秒 | 频率限制的统计窗口 |
| 失败窗口期 | 900秒（15分钟） | 失败次数统计窗口 |
| 锁定时长 | 900秒（15分钟） | 账号锁定持续时间 |

---

## 5. 数据模型

### 5.1 通用响应结构 Result\<T\>

```json
{
  "success": true,
  "code": 20000,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 操作是否成功 |
| code | int | 业务状态码 |
| message | String | 提示信息 |
| data | T | 响应数据 |

### 5.2 分页响应结构 PageResult\<T\>

```json
{
  "pageNo": 1,
  "pageSize": 10,
  "total": 25,
  "records": []
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| pageNo | int | 当前页码 |
| pageSize | int | 每页条数 |
| total | long | 总记录数 |
| records | Array\<T\> | 数据列表 |

### 5.3 业务状态码枚举

| 枚举值 | HTTP状态码 | 业务码 | 说明 |
|--------|-----------|--------|------|
| SUCCESS | 200 | 20000 | 操作成功 |
| BAD_REQUEST | 400 | 40000 | 请求参数错误 |
| UNAUTHORIZED | 401 | 40100 | 未登录或登录已过期 |
| FORBIDDEN | 403 | 40300 | 无权限访问 |
| NOT_FOUND | 404 | 40400 | 资源不存在 |
| TOO_MANY_REQUESTS | 429 | 42900 | 请求过于频繁 |
| SYSTEM_ERROR | 500 | 50000 | 系统异常 |
| ADMIN_REQUIRED | 403 | 40301 | 仅管理员可执行此操作 |
| USER_ACCESS_FORBIDDEN | 403 | 40302 | 无权限操作该用户 |
| USER_ROLE_VIEW_FORBIDDEN | 403 | 40303 | 无权限查看该用户角色 |
| USER_PERMISSION_VIEW_FORBIDDEN | 403 | 40304 | 无权限查看该用户权限 |
| USER_NOT_FOUND | 404 | 40401 | 用户不存在 |
| USER_DISABLED | 403 | 40305 | 账号已被禁用 |
| USERNAME_EXISTS | 400 | 40001 | 用户名已存在 |
| CURRENT_USER_DELETE_FORBIDDEN | 400 | 40002 | 不能删除当前登录用户 |
| CURRENT_USER_DISABLE_FORBIDDEN | 400 | 40003 | 不能禁用当前登录用户 |
| LAST_ADMIN_DELETE_FORBIDDEN | 400 | 40004 | 至少保留一个管理员账号 |
| LAST_ADMIN_DISABLE_FORBIDDEN | 400 | 40005 | 至少保留一个启用中的管理员账号 |
| LOGIN_FAILED | 400 | 40006 | 用户名或密码错误 |

### 5.4 数据库表结构

#### auth_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK AUTO_INCREMENT | 用户ID |
| user_name | varchar(32) UNIQUE | 用户名/账号 |
| nick_name | varchar(32) | 昵称 |
| email | varchar(64) UNIQUE | 邮箱 |
| phone | varchar(32) UNIQUE | 手机号 |
| password | varchar(128) | 密码（BCrypt加密） |
| sex | tinyint(2) | 性别：0未知 1男 2女 |
| avatar | varchar(255) | 头像URL |
| status | tinyint(2) | 状态：0启用 1禁用 |
| introduce | varchar(255) | 个人介绍 |
| ext_json | text | 扩展信息(JSON) |
| created_by | varchar(32) | 操作人标识 |
| created_time | datetime | 创建时间 |
| update_by | varchar(32) | 更新人标识 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint(1) | 是否删除：0未删除 1已删除 |

#### auth_role（角色表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK AUTO_INCREMENT | 角色ID |
| role_name | varchar(32) | 角色名称 |
| role_key | varchar(64) UNIQUE | 角色唯一标识 |
| created_by | varchar(32) | 操作人标识 |
| created_time | datetime | 创建时间 |
| update_by | varchar(32) | 更新人标识 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint(1) | 是否删除：0未删除 1已删除 |

#### auth_permission（权限表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK AUTO_INCREMENT | 权限ID |
| name | varchar(64) | 权限名称 |
| parent_id | bigint(20) | 父级权限ID |
| type | tinyint(4) | 权限类型：0菜单 1操作 |
| menu_url | varchar(255) | 菜单路由 |
| status | tinyint(2) | 状态：0启用 1禁用 |
| is_show | tinyint(2) | 展示状态：0展示 1隐藏 |
| icon | varchar(128) | 图标 |
| permission_key | varchar(64) UNIQUE | 权限唯一标识 |
| created_by | varchar(32) | 操作人标识 |
| created_time | datetime | 创建时间 |
| update_by | varchar(32) | 更新人标识 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint(1) | 是否删除：0未删除 1已删除 |

#### auth_user_role（用户角色关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK AUTO_INCREMENT | 关联ID |
| user_id | bigint(20) | 用户ID |
| role_id | bigint(20) | 角色ID |
| created_by | varchar(32) | 操作人标识 |
| created_time | datetime | 创建时间 |
| update_by | varchar(32) | 更新人标识 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint(1) | 是否删除：0未删除 1已删除 |

> **唯一约束**: `(active_user_id, active_role_id)` — 同一用户不能重复分配同一角色

#### auth_role_permission（角色权限关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) PK AUTO_INCREMENT | 关联ID |
| role_id | bigint(20) | 角色ID |
| permission_id | bigint(20) | 权限ID |
| created_by | varchar(32) | 操作人标识 |
| created_time | datetime | 创建时间 |
| update_by | varchar(32) | 更新人标识 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint(1) | 是否删除：0未删除 1已删除 |

> **唯一约束**: `(active_role_id, active_permission_id)` — 同一角色不能重复关联同一权限

### 5.5 RBAC 关系模型

```
┌──────────┐     ┌───────────────┐     ┌──────────┐
│ auth_user │────<│ auth_user_role │>────│ auth_role │
└──────────┘     └───────────────┘     └──────────┘
                                              │
                                    ┌─────────────────────┐
                                    │ auth_role_permission │
                                    └─────────────────────┘
                                              │
                                     ┌─────────────────┐
                                     │ auth_permission  │
                                     └─────────────────┘
                                       │ (parentId)
                                       └──> 自引用树形结构
```

### 5.6 初始化数据

系统预置以下数据：

**权限**

| ID | 名称 | 类型 | 路由 | 标识 |
|----|------|------|------|------|
| 1 | 权限管理 | 菜单(0) | /auth | auth:manage |
| 2 | 用户管理 | 操作(1) | /auth/user | user:manage |
| 3 | 角色管理 | 操作(1) | /auth/role | role:manage |
| 4 | 权限项管理 | 操作(1) | /auth/permission | permission:manage |

**角色**

| ID | 名称 | 标识 |
|----|------|------|
| 1 | 管理员 | admin |

**角色权限**

| 角色ID | 权限ID |
|--------|--------|
| 1 | 1 |
| 1 | 2 |
| 1 | 3 |
| 1 | 4 |
