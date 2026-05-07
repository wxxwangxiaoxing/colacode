package com.colacode.ai.controller;

import com.colacode.ai.config.AiProperties;
import com.colacode.ai.service.AiService;
import com.colacode.ai.service.RealAiService;
import com.colacode.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai/config")
@Tag(name = "AI 配置", description = "AI 模型配置与运行状态")
public class AiConfigController {

    private final AiProperties aiProperties;
    private final AiService aiService;
    private final ObjectProvider<RealAiService> openAiServiceProvider;

    public AiConfigController(AiProperties aiProperties,
                              AiService aiService,
                              ObjectProvider<RealAiService> openAiServiceProvider) {
        this.aiProperties = aiProperties;
        this.aiService = aiService;
        this.openAiServiceProvider = openAiServiceProvider;
    }

    @GetMapping("/models")
    @Operation(summary = "获取可用模型", description = "返回各模型配置及当前运行状态")
    public Result<Map<String, Object>> getAvailableModels() {
        Map<String, Object> result = new HashMap<>();
        result.put("mock", toModelMap(aiProperties.getMock(), true));
        result.put("openai", toModelMap(aiProperties.getOpenai(), hasOpenAiDelegate()));
        result.put("deepseek", toModelMap(aiProperties.getDeepseek(), false));
        result.put("chatglm", toModelMap(aiProperties.getChatglm(), false));
        result.put("qwen", toModelMap(aiProperties.getQwen(), false));
        result.put("ollama", toModelMap(aiProperties.getOllama(), false));
        result.put("configuredModel", aiProperties.getDefaultModel());
        result.put("activeModelName", aiService.getModelName());
        result.put("activeModelAvailable", aiService.isAvailable());
        return Result.success(result);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前模型", description = "返回当前配置模型与实际激活模型")
    public Result<Map<String, Object>> getCurrentModel() {
        Map<String, Object> result = new HashMap<>();
        result.put("configuredModel", aiProperties.getDefaultModel());
        result.put("activeModelName", aiService.getModelName());
        result.put("activeModelAvailable", aiService.isAvailable());
        return Result.success(result);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换 AI 模型", description = "当前仅支持在 mock 与 openai 之间切换")
    public Result<Void> switchModel(@Parameter(description = "模型名称") @RequestParam String model) {
        if (!isValidModel(model)) {
            return Result.fail("无效的模型: " + model + "，当前仅支持: mock, openai");
        }
        if ("openai".equalsIgnoreCase(model)) {
            if (!aiProperties.getOpenai().isEnabled()) {
                return Result.fail("模型 openai 未启用，请先设置 COLACODE_AI_OPENAI_ENABLED=true");
            }
            if (!hasOpenAiDelegate()) {
                return Result.fail("OpenAI 运行时未就绪，请检查 API Key、Starter 配置和启动日志");
            }
        }
        aiProperties.setDefaultModel(model);
        log.info("Switched AI model config to {}", model);
        return Result.success();
    }

    private Map<String, Object> toModelMap(AiProperties.ModelConfig config, boolean runtimeSupported) {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", config.isEnabled());
        result.put("model", config.getModel());
        result.put("baseUrl", config.getBaseUrl());
        result.put("runtimeSupported", runtimeSupported);
        return result;
    }

    private boolean isValidModel(String model) {
        return "mock".equalsIgnoreCase(model)
                || "openai".equalsIgnoreCase(model);
    }

    private boolean hasOpenAiDelegate() {
        return openAiServiceProvider.getIfAvailable() != null;
    }
}
