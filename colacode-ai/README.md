# colacode-ai

`colacode-ai` is a standalone Spring AI service for short-term integration with the existing Java 8 microservices.

## Purpose

- Keep current `colacode` services on Java 8 / Spring Boot 2.4.2.
- Provide AI capabilities from an isolated Java 17 / Spring Boot 3.4 service.
- Expose stable HTTP APIs for interview question generation and scoring.

## APIs

- `POST /ai/interview/question`
- `POST /ai/interview/score`

Request/response contract is aligned with `colacode-interview` Feign DTOs.

## Run

```bash
cd colacode-ai
mvn spring-boot:run
```

Or set environment variables first:

```bash
set OPENAI_API_KEY=your_key
set OPENAI_MODEL=gpt-4o-mini
```

## Interview service wiring

In `colacode-interview` set one of the following:

- `interview.ai.base-url=http://127.0.0.1:3020` (direct URL)
- or `interview.ai.service-name=colacode-ai` with service discovery

And enable:

- `interview.ai.enabled=true`
