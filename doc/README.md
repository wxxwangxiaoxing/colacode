# ColaCode 文档中心

## 目录结构

```
doc/
├── README.md                      # 文档中心首页
├── local-development-guide.md    # 本地开发指南
├── services/                    # 服务模块文档
│   ├── 01-Gateway/             # API 网关
│   ├── 02-Auth/                # 认证服务
│   ├── 03-Subject/             # 题库服务
│   ├── 04-Practice/            # 练习服务
│   ├── 05-Circle/              # 社区服务
│   ├── 06-Interview/           # 面试服务
│   ├── 07-OSS/                 # 对象存储
│   ├── 08-Wx/                 # 微信模块
│   └── 09-AI/                 # AI 服务
├── infrastructure/             # 基础设施配置
│   ├── nacos/                  # Nacos 配置示例
│   └── sql/                    # 数据库脚本
├── guides/                     # 开发指南
└── templates/                  # 模板文件
```

## 服务模块

| 序号 | 服务 | 端口 | 说明 |
|------|------|------|------|
| 01 | Gateway | 5000 | API 网关 |
| 02 | Auth | 3011 | 认证授权 |
| 03 | Subject | 3010 | 题库服务 |
| 04 | Practice | 3013 | 练习服务 |
| 05 | Circle | 3014 | 社区服务 |
| 06 | Interview | 3015 | 面试服务 |
| 07 | OSS | 4000 | 对象存储 |
| 08 | Wx | 3012 | 微信模块 |
| 09 | AI | 3020 | AI 服务 |

## 快速开始

1. [本地开发指南](./local-development-guide.md)
2. [Nacos 配置说明](./infrastructure/nacos/README.md)
3. [数据库初始化](./infrastructure/sql/)