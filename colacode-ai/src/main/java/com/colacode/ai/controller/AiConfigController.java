package com.colacode.ai.controller;

import com.colacode.ai.config.AiProperties;
import com.colacode.ai.service.AiService;
import com.colacode.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai/config")
public class AiConfigController {

    private final AiProperties aiProperties;
    private final AiService aiService;

    public AiConfigController(AiProperties aiProperties, AiService aiService) {
        this.aiProperties = aiProperties;
        this.aiService = aiService;
    }

    @GetMapping("/models")
    public Result<Map<String, Object>> getAvailableModels() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> deepseek = new HashMap<>();
        deepseek.put("enabled", aiProperties.getDeepseek().isEnabled());
        deepseek.put("model", aiProperties.getDeepseek().getModel());
        deepseek.put("baseUrl", aiProperties.getDeepseek().getBaseUrl());
        
        Map<String, Object> openai = new HashMap<>();
        openai.put("enabled", aiProperties.getOpenai().isEnabled());
        openai.put("model", aiProperties.getOpenai().getModel());
        openai.put("baseUrl", aiProperties.getOpenai().getBaseUrl());
        
        Map<String, Object> chatglm = new HashMap<>();
        chatglm.put("enabled", aiProperties.getChatglm().isEnabled());
        chatglm.put("model", aiProperties.getChatglm().getModel());
        chatglm.put("baseUrl", aiProperties.getChatglm().getBaseUrl());
        
        Map<String, Object> qwen = new HashMap<>();
        qwen.put("enabled", aiProperties.getQwen().isEnabled());
        qwen.put("model", aiProperties.getQwen().getModel());
        qwen.put("baseUrl", aiProperties.getQwen().getBaseUrl());
        
        result.put("deepseek", deepseek);
        result.put("openai", openai);
        result.put("chatglm", chatglm);
        result.put("qwen", qwen);
        result.put("currentModel", aiProperties.getDefaultModel());
        result.put("activeModelName", aiService.getModelName());
        result.put("isAvailable", aiService.isAvailable());
        
        return Result.success(result);
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentModel() {
        Map<String, Object> result = new HashMap<>();
        result.put("model", aiProperties.getDefaultModel());
        result.put("modelName", aiService.getModelName());
        result.put("isAvailable", aiService.isAvailable());
        return Result.success(result);
    }

    @PostMapping("/switch")
    public Result<Void> switchModel(@RequestParam String model) {
        if (!isValidModel(model)) {
            return Result.fail("无效的模型: " + model + ", 可选值: deepseek, openai, chatglm, qwen");
        }
        
        AiProperties.ModelConfig modelConfig = aiProperties.getActiveModel(model);
        if (!modelConfig.isEnabled()) {
            return Result.fail("模型 " + model + " 未启用，请检查配置");
        }
        
        aiProperties.setDefaultModel(model);
        log.info("切换 AI 模型到: {}", model);
        return Result.success();
    }

    private boolean isValidModel(String model) {
        return "deepseek".equalsIgnoreCase(model) 
                || "openai".equalsIgnoreCase(model) 
                || "chatglm".equalsIgnoreCase(model)
                || "qwen".equalsIgnoreCase(model);
    }
}