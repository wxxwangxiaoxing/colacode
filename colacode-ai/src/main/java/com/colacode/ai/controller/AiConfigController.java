package com.colacode.ai.controller;

import com.colacode.ai.config.AiProperties;
import com.colacode.ai.service.AiService;
import com.colacode.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI配置控制器
 * 提供AI模型配置查询和切换功能
 *
 * @author wxx
 */
@Slf4j
@RestController
@RequestMapping("/ai/config")
@Tag(name = "AI配置", description = "AI模型配置管理")
public class AiConfigController {

    /**
     * AI配置属性
     */
    private final AiProperties aiProperties;
    /**
     * AI服务
     */
    private final AiService aiService;

    /**
     * 构造函数
     *
     * @param aiProperties AI配置属性
     * @param aiService    AI服务实例
     */
    public AiConfigController(AiProperties aiProperties, AiService aiService) {
        this.aiProperties = aiProperties;
        this.aiService = aiService;
    }

    /**
     * 获取所有可用模型
     * 返回所有已配置AI模型的状态信息
     *
     * @return 各模型配置信息
     */
    @GetMapping("/models")
    @Operation(summary = "获取可用模型", description = "获取所有可用的AI模型及其配置")
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
        
        Map<String, Object> ollama = new HashMap<>();
        ollama.put("enabled", aiProperties.getOllama().isEnabled());
        ollama.put("model", aiProperties.getOllama().getModel());
        ollama.put("baseUrl", aiProperties.getOllama().getBaseUrl());
        
        result.put("deepseek", deepseek);
        result.put("openai", openai);
        result.put("chatglm", chatglm);
        result.put("qwen", qwen);
        result.put("ollama", ollama);
        result.put("currentModel", aiProperties.getDefaultModel());
        result.put("activeModelName", aiService.getModelName());
        result.put("isAvailable", aiService.isAvailable());
        
        return Result.success(result);
    }

    /**
     * 获取当前使用的模型
     *
     * @return 当前模型信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前模型", description = "获取当前使用的AI模型")
    public Result<Map<String, Object>> getCurrentModel() {
        Map<String, Object> result = new HashMap<>();
        result.put("model", aiProperties.getDefaultModel());
        result.put("modelName", aiService.getModelName());
        result.put("isAvailable", aiService.isAvailable());
        return Result.success(result);
    }

    /**
     * 切换AI模型
     * 动态切换当前使用的AI模型
     *
     * @param model 模型名称
     * @return 切换结果
     */
    @PostMapping("/switch")
    @Operation(summary = "切换AI模型", description = "切换当前使用的AI模型")
    public Result<Void> switchModel(@Parameter(description = "模型名称") @RequestParam String model) {
        if (!isValidModel(model)) {
            return Result.fail("无效的模型: " + model + ", 可选值: deepseek, openai, chatglm, qwen, ollama");
        }
        
        AiProperties.ModelConfig modelConfig = aiProperties.getActiveModel(model);
        if (!modelConfig.isEnabled()) {
            return Result.fail("模型 " + model + " 未启用，请检查配置");
        }
        
        aiProperties.setDefaultModel(model);
        log.info("切换 AI 模型到: {}", model);
        return Result.success();
    }

    /**
     * 验证模型名称是否有效
     *
     * @param model 模型名称
     * @return 是否有效
     */
    private boolean isValidModel(String model) {
        return "deepseek".equalsIgnoreCase(model) 
                || "openai".equalsIgnoreCase(model) 
                || "chatglm".equalsIgnoreCase(model)
                || "qwen".equalsIgnoreCase(model)
                || "ollama".equalsIgnoreCase(model);
    }
}