package com.colacode.ai.config;

import com.colacode.ai.service.AiService;
import com.colacode.ai.service.MockAiService;
import com.colacode.ai.service.RealAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI模型配置类
 * 根据条件自动选择使用真实AI服务或Mock服务
 *
 * @author wxx
 */
@Slf4j
@Configuration
public class AiModelConfiguration {

    /**
     * Mock AI服务
     * 当没有检测到AI模型时使用
     *
     * @return MockAiService实例
     */
    @Bean
    @ConditionalOnMissingBean(AiService.class)
    public AiService mockAiService() {
        log.info("✅ 未检测到 AI 模型，使用 MockAiService");
        return new MockAiService();
    }

    /**
     * OpenAI真实AI服务
     * 当检测到OpenAiChatModel Bean时启用
     *
     * @param builder    ChatClient构建器
     * @param properties AI配置属性
     * @return RealAiService实例
     */
    @Bean
    @Primary
    @ConditionalOnBean(OpenAiChatModel.class)
    public AiService openAiService(ChatClient.Builder builder, AiProperties properties) {
        log.info("✅ 检测到 OpenAI 模型可用，当前模型: {}", properties.getDefaultModel());
        return new RealAiService(builder, properties);
    }
}