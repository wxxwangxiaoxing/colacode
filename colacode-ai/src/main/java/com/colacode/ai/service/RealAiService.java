package com.colacode.ai.service;

import com.colacode.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RealAiService implements AiService {

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    public RealAiService(ChatClient.Builder builder, AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.chatClient = builder.build();
        log.info("AI 服务初始化完成，使用模型: {}", aiProperties.getDefaultModel());
    }

    @Override
    public String generateQuestion(String keyword) {
        String prompt = buildPrompt("generate", keyword);
        String content = chatClient.prompt().user(prompt).call().content();
        if (content == null || content.trim().isEmpty()) {
            return fallbackQuestion(keyword);
        }
        return content.trim();
    }

    @Override
    public double scoreAnswer(String question, String userAnswer) {
        String answer = userAnswer == null ? "" : userAnswer;
        String prompt = buildScorePrompt(question, answer);
        String content = chatClient.prompt().user(prompt).call().content();
        return parseScore(content);
    }

    @Override
    public String getModelName() {
        AiProperties.ModelConfig modelConfig = aiProperties.getActiveModel(aiProperties.getDefaultModel());
        return modelConfig.getModel();
    }

    @Override
    public boolean isAvailable() {
        return chatClient != null;
    }

    private String buildPrompt(String type, String keyword) {
        return switch (type) {
            case "generate" -> "你是资深技术面试官。基于关键词生成一道中文面试题。"
                    + "要求: 只返回题目本身，不要解释。关键词: " + keyword;
            default -> "";
        };
    }

    private String buildScorePrompt(String question, String answer) {
        return "你是技术面试评分助手。对候选人回答按 1.0 到 5.0 评分，只返回数字。"
                + "题目: " + question + " 回答: " + answer;
    }

    private String fallbackQuestion(String keyword) {
        return "请深入谈谈 " + keyword + " 的核心原理、应用场景和常见问题";
    }

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