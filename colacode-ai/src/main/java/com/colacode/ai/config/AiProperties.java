package com.colacode.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "colacode.ai")
public class AiProperties {

    private String defaultModel = "mock";

    private ModelConfig mock = defaultMockConfig();

    private ModelConfig deepseek = new ModelConfig();

    private ModelConfig openai = new ModelConfig();

    private ModelConfig chatglm = new ModelConfig();

    private ModelConfig qwen = new ModelConfig();

    private ModelConfig ollama = defaultOllamaConfig();

    @Data
    public static class ModelConfig {

        private boolean enabled;

        private String apiKey;

        private String baseUrl = "https://api.deepseek.com";

        private String model = "deepseek-chat";

        private Double temperature = 0.7;

        private Integer maxTokens = 2048;
    }

    public ModelConfig getActiveModel(String modelName) {
        if (modelName == null) {
            return mock;
        }
        return switch (modelName.toLowerCase()) {
            case "mock" -> mock;
            case "deepseek" -> deepseek;
            case "openai" -> openai;
            case "chatglm" -> chatglm;
            case "qwen" -> qwen;
            case "ollama" -> ollama;
            default -> mock;
        };
    }

    private ModelConfig defaultMockConfig() {
        ModelConfig config = new ModelConfig();
        config.setEnabled(true);
        config.setBaseUrl("local://mock");
        config.setModel("MOCK");
        return config;
    }

    private ModelConfig defaultOllamaConfig() {
        ModelConfig config = new ModelConfig();
        config.setBaseUrl("http://localhost:11434");
        config.setModel("llama3");
        return config;
    }
}
