# Spring AI Interview 联调说明

## 1. 目标

在不升级主项目（Java 8 + Spring Boot 2.4.2）的前提下，打通：

- `colacode-interview` (Java 8) -> Feign
- 外部 AI 服务示例（可使用独立的 `colacode-ai`，但它不属于当前主干项目）

## 2. 当前接口约定

`colacode-interview` 调用外部 AI 服务：

- `POST /ai/interview/question`
- `POST /ai/interview/score`

外部 AI 服务需要实现以上接口。`colacode-ai` 只是一个独立示例实现。

## 3. 启动外部 AI 服务

如果你使用独立的 `colacode-ai` 示例服务，可在 `D:\project\backend\colacode\colacode-ai` 目录启动：

```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.18
set PATH=%JAVA_HOME%\bin;%PATH%
set OPENAI_API_KEY=你的Key
set OPENAI_MODEL=gpt-4o-mini
mvn spring-boot:run
```

默认端口 `3020`。

## 4. 启动 colacode-interview（AI 模式）

`colacode-interview` 已新增配置文件：

- [application-ai.yml](/D:/project/backend/colacode/colacode-interview/src/main/resources/application-ai.yml)

默认已配置：

- `interview.ai.enabled=true`
- `interview.ai.base-url=http://your-ai-host:3020`

启动命令：

```bash
mvn -pl colacode-interview -am spring-boot:run -Dspring-boot.run.profiles=ai
```

## 5. 联调验证

## 5.1 先验证 interview AI 分析接口

```bash
curl -X POST "http://127.0.0.1:3015/interview/analyse" ^
  -H "Content-Type: application/json" ^
  -d "{\"engineType\":\"AI\",\"labels\":[\"Java\",\"Spring\",\"MySQL\"]}"
```

期望：

- 返回 `success=true`
- `data` 为关键词列表

## 5.2 验证 AI 生成面试题

```bash
curl -X POST "http://127.0.0.1:3015/interview/start" ^
  -H "Content-Type: application/json" ^
  -d "{\"engineType\":\"AI\",\"keywords\":[{\"keyWord\":\"Java\",\"categoryId\":1,\"labelId\":1},{\"keyWord\":\"Spring\",\"categoryId\":1,\"labelId\":2}]}"
```

期望：

- 返回 `success=true`
- `data[].subjectName` 为 AI 生成题目（或兜底题目）

## 5.3 验证 AI 评分

```bash
curl -X POST "http://127.0.0.1:3015/interview/submit" ^
  -H "Content-Type: application/json" ^
  -d "{\"engineType\":\"AI\",\"interviewUrl\":\"http://demo/interview/1\",\"userId\":1001,\"questions\":[{\"keyWord\":\"Java\",\"subjectName\":\"请解释JVM内存结构\",\"subjectAnswer\":\"\",\"userAnswer\":\"堆、栈、方法区...\"}]}"
```

期望：

- 返回 `success=true`
- `data.avgScore` 有值
- `data.tips` 有评分建议

## 6. 失败降级行为

如果外部 AI 服务不可用或超时：

- 生成题目会回退到模板题目
- 评分会回退到默认分值 `3.0`

这保证了 `colacode-interview` 不会因为 AI 网关异常而不可用。

## 7. 常见问题

### 7.1 `connection refused`

排查：

1. 外部 AI 服务是否已启动
2. `application-ai.yml` 的 `base-url` 是否可达
3. 云服务器安全组是否放行 `3020`

### 7.2 `401` 或模型调用失败

排查：

1. `OPENAI_API_KEY` 是否正确
2. `OPENAI_MODEL` 是否可用
3. 服务端网络是否可访问模型供应商

### 7.3 interview 未走 AI

排查：

1. 启动时是否加了 `-Dspring-boot.run.profiles=ai`
2. 请求体 `engineType` 是否为 `AI`
