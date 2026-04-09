package com.colacode.ai.config;

import com.colacode.ai.service.AiService;
import com.colacode.ai.service.RealAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiModelConfiguration {

    @Bean
    @ConditionalOnBean(OpenAiChatModel.class)
    public AiService realAiService(ChatClient.Builder builder, AiProperties properties) {
        log.info("✅ 检测到 AI 模型可用，初始化 RealAiService，当前模型: {}", properties.getDefaultModel());
        return new RealAiService(builder, properties);
    }
}