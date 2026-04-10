package com.colacode.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置属性类
 * 用于配置不同AI模型的参数
 *
 * @author wxx
 */
@Data
@Component
@ConfigurationProperties(prefix = "colacode.ai")
public class AiProperties {

    /**
     * 默认使用的AI模型名称
     */
    private String defaultModel = "deepseek";
    
    /**
     * DeepSeek模型配置
     */
    private ModelConfig deepseek = new ModelConfig();
    /**
     * OpenAI模型配置
     */
    private ModelConfig openai = new ModelConfig();
    /**
     * ChatGLM模型配置
     */
    private ModelConfig chatglm = new ModelConfig();
    /**
     * 通义千问模型配置
     */
    private ModelConfig qwen = new ModelConfig();
    /**
     * Ollama本地模型配置
     */
    private ModelConfig ollama = new ModelConfig() {{
        setBaseUrl("http://localhost:11434");
        setModel("llama3");
    }};

    /**
     * 模型配置内部类
     */
    @Data
    public static class ModelConfig {
        /**
         * 是否启用该模型
         */
        private boolean enabled = false;
        /**
         * API密钥
         */
        private String apiKey;
        /**
         * 模型服务地址
         */
        private String baseUrl = "https://api.deepseek.com";
        /**
         * 模型名称
         */
        private String model = "deepseek-chat";
        /**
         * 温度参数，控制生成随机性
         */
        private Double temperature = 0.7;
        /**
         * 最大生成token数
         */
        private Integer maxTokens = 2048;
    }

    /**
     * 获取指定名称的模型配置
     *
     * @param modelName 模型名称
     * @return 模型配置对象
     */
    public ModelConfig getActiveModel(String modelName) {
        return switch (modelName.toLowerCase()) {
            case "deepseek" -> deepseek;
            case "openai" -> openai;
            case "chatglm" -> chatglm;
            case "qwen" -> qwen;
            case "ollama" -> ollama;
            default -> deepseek;
        };
    }
}