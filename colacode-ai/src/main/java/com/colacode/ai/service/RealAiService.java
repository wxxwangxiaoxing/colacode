package com.colacode.ai.service;

import com.colacode.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * 真实AI服务实现
 * 调用实际的AI模型进行面试题生成和评分
 *
 * @author wxx
 */
@Slf4j
@Service
public class RealAiService implements AiService {

    /**
     * ChatClient实例
     */
    private final ChatClient chatClient;
    /**
     * AI配置属性
     */
    private final AiProperties aiProperties;

    /**
     * 构造函数
     *
     * @param builder     ChatClient构建器
     * @param aiProperties AI配置属性
     */
    public RealAiService(ChatClient.Builder builder, AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.chatClient = builder.build();
        log.info("AI 服务初始化完成，使用模型: {}", aiProperties.getDefaultModel());
    }

    /**
     * 生成面试题
     *
     * @param keyword 关键词
     * @return AI生成的面试题
     */
    @Override
    public String generateQuestion(String keyword) {
        String prompt = buildPrompt("generate", keyword);
        String content = chatClient.prompt().user(prompt).call().content();
        if (content == null || content.trim().isEmpty()) {
            return fallbackQuestion(keyword);
        }
        return content.trim();
    }

    /**
     * 对用户答案进行评分
     *
     * @param question   面试题
     * @param userAnswer 用户答案
     * @return 评分结果（1.0-5.0）
     */
    @Override
    public double scoreAnswer(String question, String userAnswer) {
        String answer = userAnswer == null ? "" : userAnswer;
        String prompt = buildScorePrompt(question, answer);
        String content = chatClient.prompt().user(prompt).call().content();
        return parseScore(content);
    }

    /**
     * 获取当前使用的模型名称
     *
     * @return 模型名称
     */
    @Override
    public String getModelName() {
        AiProperties.ModelConfig modelConfig = aiProperties.getActiveModel(aiProperties.getDefaultModel());
        return modelConfig.getModel();
    }

    /**
     * 检查AI服务是否可用
     *
     * @return 是否可用
     */
    @Override
    public boolean isAvailable() {
        return chatClient != null;
    }

    /**
     * 构建生成面试题的Prompt
     *
     * @param type    Prompt类型
     * @param keyword 关键词
     * @return 格式化后的Prompt
     */
    private String buildPrompt(String type, String keyword) {
        return switch (type) {
            case "generate" -> "你是资深技术面试官。基于关键词生成一道中文面试题。"
                    + "要求: 只返回题目本身，不要解释。关键词: " + keyword;
            default -> "";
        };
    }

    /**
     * 构建评分Prompt
     *
     * @param question 面试题
     * @param answer   用户答案
     * @return 格式化后的Prompt
     */
    private String buildScorePrompt(String question, String answer) {
        return "你是技术面试评分助手。对候选人回答按 1.0 到 5.0 评分，只返回数字。"
                + "题目: " + question + " 回答: " + answer;
    }

    /**
     * 生成备用面试题
     *
     * @param keyword 关键词
     * @return 备用面试题
     */
    private String fallbackQuestion(String keyword) {
        return "请深入谈谈 " + keyword + " 的核心原理、应用场景和常见问题";
    }

    /**
     * 解析评分结果
     *
     * @param raw 原始评分字符串
     * @return 标准化评分（1.0-5.0）
     */
    private double parseScore(String raw) {
        if (raw == null) {
            return 3.0;
        }
        String cleaned = raw.trim().replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) {
            return 3.0;
        }
        try {
            double value = Double.parseDouble(cleaned);
            if (value < 1.0) {
                return 1.0;
            }
            if (value > 5.0) {
                return 5.0;
            }
            return value;
        } catch (NumberFormatException e) {
            return 3.0;
        }
    }
}