package com.colacode.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "colacode.ai")
public class AiProperties {

    private String defaultModel = "deepseek";
    
    private ModelConfig deepseek = new ModelConfig();
    private ModelConfig openai = new ModelConfig();
    private ModelConfig chatglm = new ModelConfig();
    private ModelConfig qwen = new ModelConfig();

    @Data
    public static class ModelConfig {
        private boolean enabled = false;
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private Double temperature = 0.7;
        private Integer maxTokens = 2048;
    }

    public ModelConfig getActiveModel(String modelName) {
        return switch (modelName.toLowerCase()) {
            case "deepseek" -> deepseek;
            case "openai" -> openai;
            case "chatglm" -> chatglm;
            case "qwen" -> qwen;
            default -> deepseek;
        };
    }
}