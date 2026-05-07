# colacode-ai

`colacode-ai` 是独立 AI 服务模块，使用 Java 17 / Spring Boot 3.4 构建，当前为 ColaCode 提供面试题生成、答案评分和编程题判题分析能力。

## 已提供接口

- `POST /ai/interview/question`
- `POST /ai/interview/score`
- `POST /ai/judge/analyse`
- `GET /ai/config/models`
- `GET /ai/config/current`
- `POST /ai/config/switch?model=mock|openai`

## 运行模式

当前运行时只接通两种模型：

- `mock`
- `openai`

默认配置为 `mock`，因此本地可以在没有 API Key 的情况下直接启动。

如果需要启用真实 OpenAI：

```bash
set COLACODE_AI_DEFAULT_MODEL=openai
set COLACODE_AI_OPENAI_ENABLED=true
set OPENAI_API_KEY=your_key
set OPENAI_MODEL=gpt-5.4
```

## 启动

```bash
cd colacode-ai
mvn spring-boot:run
```

或本地 profile：

```bash
mvn -pl colacode-ai spring-boot:run -Dspring-boot.run.profiles=local
```

## 接入方

### Interview

`colacode-interview` 可通过以下配置接入：

- `interview.ai.base-url=http://127.0.0.1:3020`
- 或 `interview.ai.service-name=colacode-ai`

本地 `application-local.yml` 默认读取：

- `BOOT3_AI_ENABLED`
- `BOOT3_AI_BASE_URL`
- `BOOT3_AI_URL`

### Practice

`colacode-practice` 会在 Judge0 判题完成后可选触发 AI 分析，写回提交表中的：

- `ai_status`
- `ai_feedback`

启用方式：

```bash
set BOOT3_AI_URL=http://127.0.0.1:3020
set BOOT3_JUDGE_AI_ENABLED=true
```
