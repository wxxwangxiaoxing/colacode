# AI 服务

## 概述

`colacode-ai` 是独立的 Java 17 / Spring Boot 3 服务，负责承接与大模型相关的能力，避免把 AI 依赖直接塞进旧链路里。

当前已落地三类能力：

- 面试题生成
- 面试答案评分
- 编程题判题结果分析

## 基本信息

- 默认端口：`3020`
- 技术栈：Spring AI
- 相关调用方：`colacode-interview`、`colacode-practice`

## 接口

### 面试能力

- `POST /ai/interview/question`
- `POST /ai/interview/score`

### 判题分析

- `POST /ai/judge/analyse`

请求体核心字段：

- `subjectName`
- `language`
- `code`
- `status`
- `judgeMessage`
- `failedCaseSummary`
- `stdoutPreview`
- `stderrPreview`
- `inputExample`
- `outputExample`

返回值：

- `feedback`：AI 生成的排查建议与修复方向

### 运行配置查询

- `GET /ai/config/models`
- `GET /ai/config/current`
- `POST /ai/config/switch?model=mock|openai`

## 模型策略

当前运行时真正接通的模型只有：

- `mock`
- `openai`

其余 `deepseek / chatglm / qwen / ollama` 配置项目前仅保留结构，占位给后续扩展，不参与运行时切换。

默认启动模式：

- `COLACODE_AI_DEFAULT_MODEL=mock`
- `COLACODE_AI_OPENAI_ENABLED=false`

启用真实 OpenAI 模式至少需要：

```bash
set COLACODE_AI_DEFAULT_MODEL=openai
set COLACODE_AI_OPENAI_ENABLED=true
set OPENAI_API_KEY=your_key
set OPENAI_MODEL=gpt-5.4
```

可选配置：

```bash
set OPENAI_BASE_URL=https://api.openai.com
```

## 本地联调

### 启动 AI 服务

```bash
mvn -pl colacode-ai spring-boot:run -Dspring-boot.run.profiles=local
```

默认 `local` 模式下，即使没有 API Key，也可以用 `mock` 正常启动。

### Interview 接入

`colacode-interview` 本地配置默认读取：

- `BOOT3_AI_ENABLED`
- `BOOT3_AI_BASE_URL`
- `BOOT3_AI_URL`

### Practice 接入

`colacode-practice` 已支持判题完成后异步调用 AI 服务回填 `ai_status` 和 `ai_feedback`，启用方式：

```bash
set BOOT3_AI_URL=http://127.0.0.1:3020
set BOOT3_JUDGE_AI_ENABLED=true
```

如需对通过提交也输出分析：

```bash
set BOOT3_JUDGE_AI_INCLUDE_ACCEPTED=true
```
