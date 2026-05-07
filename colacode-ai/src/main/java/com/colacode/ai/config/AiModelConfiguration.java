package com.colacode.ai.config;

import com.colacode.ai.service.AiService;
import com.colacode.ai.service.MockAiService;
import com.colacode.ai.service.RealAiService;
import com.colacode.ai.service.SwitchableAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiModelConfiguration {

    @Bean
    public MockAiService mockAiDelegate() {
        log.info("Mock AI service is ready");
        return new MockAiService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "colacode.ai.openai", name = "enabled", havingValue = "true")
    @ConditionalOnBean(OpenAiChatModel.class)
    public RealAiService openAiDelegate(ChatClient.Builder builder, AiProperties properties) {
        log.info("OpenAI delegate is ready");
        return new RealAiService(builder, properties);
    }

    @Bean
    public AiService aiService(AiProperties properties,
                               MockAiService mockAiDelegate,
                               ObjectProvider<RealAiService> openAiDelegateProvider) {
        return new SwitchableAiService(properties, mockAiDelegate, openAiDelegateProvider.getIfAvailable());
    }
}
