# MySQL 8 Docker 部署文档

## 1. 目标

本文档用于在云服务器上通过 Docker 部署 `MySQL 8`，默认部署目录为：

```text
/opt/docker/mysql8
```

部署完成后，可为 `colacode` 项目提供数据库服务。

## 2. 环境约定

- 服务器系统：`CentOS 7`
- 部署用户：`root`
- Docker 部署目录：`/opt/docker/mysql8`
- 容器名：`colacode-mysql8`
- 数据库名：`colacode`
- 默认端口：`3306`

## 3. 目录规划

建议在服务器上准备以下目录：

```text
/opt/docker/mysql8/
├── docker-compose.yml
├── conf
│   └── my.cnf
├── data
├── logs
└── init
    └── init.sql
```

作用说明：

- `conf/my.cnf`：MySQL 自定义配置
- `data`：数据库数据目录
- `logs`：MySQL 日志目录
- `init/init.sql`：初始化 SQL

## 4. 创建目录

在服务器执行：

```bash
mkdir -p /opt/docker/mysql8/conf
mkdir -p /opt/docker/mysql8/data
mkdir -p /opt/docker/mysql8/logs
mkdir -p /opt/docker/mysql8/init
```

## 5. 编写 docker-compose.yml

在 `/opt/docker/mysql8/docker-compose.yml` 中写入：

```yaml
version: "3.8"

services:
  mysql8:
    image: mysql:8.0.36
    container_name: colacode-mysql8
    restart: always
    environment:
      TZ: Asia/Shanghai
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: colacode
    ports:
      - "3306:3306"
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --lower_case_table_names=1
    volumes:
      - ./data:/var/lib/mysql
      - ./logs:/var/log/mysql
      - ./conf/my.cnf:/etc/mysql/conf.d/my.cnf
      - ./init/init.sql:/docker-entrypoint-initdb.d/init.sql
```

说明：

- `mysql_native_password`：避免部分旧驱动连接 MySQL 8 出现认证问题
- `utf8mb4`：兼容更完整的字符集
- `lower_case_table_names=1`：降低 Linux 环境下表名大小写带来的问题

## 6. 编写 my.cnf

在 `/opt/docker/mysql8/conf/my.cnf` 中写入：

```cnf
[mysqld]
default-time-zone = '+08:00'
max_connections = 300
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default-storage-engine = InnoDB
sql_mode = STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION

[client]
default-character-set = utf8mb4

[mysql]
default-character-set = utf8mb4
```

## 7. 准备初始化 SQL

把项目中的初始化脚本复制到服务器：

- 推荐脚本：`doc/colacode-db-optimized.sql`
- 兼容旧版也可使用：`doc/colacode-init.sql`

最终在服务器上放到：

```text
/opt/docker/mysql8/init/init.sql
```

如果是从当前仓库上传，可以直接执行：

```bash
scp -i /root/.ssh/你的私钥 路径/to/colacode-db-optimized.sql root@服务器IP:/opt/docker/mysql8/init/init.sql
```

如果已经在服务器上，也可以手工复制。

## 8. 启动 MySQL 8

进入部署目录：

```bash
cd /opt/docker/mysql8
```

启动容器：

```bash
docker compose up -d
```

查看容器状态：

```bash
docker ps
```

查看日志：

```bash
docker logs -f colacode-mysql8
```

## 9. 验证 MySQL 是否正常

进入容器：

```bash
docker exec -it colacode-mysql8 bash
```

登录数据库：

```bash
mysql -uroot -p123456
```

查看数据库：

```sql
show databases;
```

切换到业务库：

```sql
use colacode;
show tables;
```

如果能看到项目表结构，说明初始化成功。

## 10. 防火墙与端口

如果需要远程连接 MySQL，需要确认服务器已放行 `3306` 端口。

CentOS 7 可执行：

```bash
firewall-cmd --permanent --add-port=3306/tcp
firewall-cmd --reload
```

如果云厂商有安全组，也要同步开放 `3306`。

## 11. 项目连接配置

项目里共享数据源配置位于：

- [common-datasource.yaml](/D:/project/backend/colacode/doc/nacos-config/common-datasource.yaml)

如果应用和 MySQL 部署在同一台服务器，通常可写：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/colacode?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

如果应用运行在 Docker 容器内，且 MySQL 也运行在 Docker 内，建议不要写 `127.0.0.1`，而是写容器名或同网络服务名，例如：

```yaml
spring:
  datasource:
    url: jdbc:mysql://colacode-mysql8:3306/colacode?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
```

## 12. 常见问题

### 12.1 `Public Key Retrieval is not allowed`

原因：

- JDBC 驱动和 MySQL 8 认证方式不匹配

处理：

- 已在 `docker-compose.yml` 中设置：

```text
--default-authentication-plugin=mysql_native_password
```

### 12.2 初始化 SQL 没执行

原因通常有两种：

1. `data` 目录里已经有旧数据，MySQL 初始化脚本只会在首次初始化时执行
2. `init.sql` 挂载路径不对

处理：

- 确认 `/opt/docker/mysql8/init/init.sql` 存在
- 如果允许清空重建，先删容器和数据目录再重新启动

### 12.3 中文乱码

处理：

- 确认 `my.cnf`、容器启动参数、JDBC URL 都使用 `utf8mb4`

### 12.4 表名大小写问题

Linux 默认对表名大小写敏感，已在部署参数中设置：

```text
--lower_case_table_names=1
```

这样更适合从 Windows 开发环境迁移过来的项目。

## 13. 常用运维命令

启动：

```bash
cd /opt/docker/mysql8
docker compose up -d
```

停止：

```bash
cd /opt/docker/mysql8
docker compose down
```

重启：

```bash
docker restart colacode-mysql8
```

查看日志：

```bash
docker logs -f colacode-mysql8
```

进入容器：

```bash
docker exec -it colacode-mysql8 bash
```

## 14. 推荐执行顺序

```text
1. 创建 /opt/docker/mysql8 目录结构
2. 写入 docker-compose.yml
3. 写入 conf/my.cnf
4. 放置 init/init.sql
5. 执行 docker compose up -d
6. 检查日志是否初始化成功
7. 登录 MySQL 验证库和表
8. 把数据源配置接入 Nacos
```

## 15. 参考文件

- [docker-compose.yml](/D:/project/backend/colacode/docker-compose.yml)
- [common-datasource.yaml](/D:/project/backend/colacode/doc/nacos-config/common-datasource.yaml)
- [colacode-db-optimized.sql](/D:/project/backend/colacode/doc/colacode-db-optimized.sql)
- [colacode-init.sql](/D:/project/backend/colacode/doc/colacode-init.sql)
