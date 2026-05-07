package com.colacode.ai.service;

import com.colacode.ai.config.AiProperties;
import com.colacode.ai.service.dto.JudgeAnalysisContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;

@Slf4j
public class RealAiService implements AiService {

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    public RealAiService(ChatClient.Builder builder, AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.chatClient = builder.build();
        log.info("AI service initialized with model {}", aiProperties.getDefaultModel());
    }

    @Override
    public String generateQuestion(String keyword) {
        String prompt = buildQuestionPrompt(keyword);
        String content = chatClient.prompt().user(prompt).call().content();
        if (!StringUtils.hasText(content)) {
            return fallbackQuestion(keyword);
        }
        return content.trim();
    }

    @Override
    public double scoreAnswer(String question, String userAnswer) {
        String prompt = buildScorePrompt(question, userAnswer == null ? "" : userAnswer);
        String content = chatClient.prompt().user(prompt).call().content();
        return parseScore(content);
    }

    @Override
    public String analyzeJudgeSubmission(JudgeAnalysisContext context) {
        String prompt = buildJudgeAnalysisPrompt(context);
        String content = chatClient.prompt().user(prompt).call().content();
        if (!StringUtils.hasText(content)) {
            return fallbackJudgeAnalysis(context);
        }
        return content.trim();
    }

    @Override
    public String getModelName() {
        return aiProperties.getOpenai().getModel();
    }

    @Override
    public boolean isAvailable() {
        return chatClient != null;
    }

    private String buildQuestionPrompt(String keyword) {
        return "你是资深技术面试官。请基于关键词生成一道中文面试题，只返回题目本身，不要解释。关键词: " + keyword;
    }

    private String buildScorePrompt(String question, String answer) {
        return "你是技术面试评分助手。请对候选人的回答按 1.0 到 5.0 打分，只返回数字。题目: "
                + question + " 回答: " + answer;
    }

    private String buildJudgeAnalysisPrompt(JudgeAnalysisContext context) {
        return """
                你是在线判题系统的代码分析助手。请基于下面的提交结果，用中文给出简洁、可执行的诊断结论。
                输出要求：
                1. 先说明最可能的问题原因。
                2. 再给出 2-4 条具体修复建议。
                3. 如能判断，补充需要重点验证的边界场景。
                4. 不要输出无关寒暄，不要重复题面，不要给出完整标准答案。

                题目：%s
                语言：%s
                判题状态：%s
                判题摘要：%s
                失败用例摘要：%s
                标准输入示例：%s
                标准输出示例：%s
                stdout 摘要：%s
                stderr 摘要：%s
                用户代码：
                %s
                """.formatted(
                defaultText(context.getSubjectName(), "未知题目"),
                defaultText(context.getLanguage(), "未知语言"),
                defaultText(context.getStatus(), "UNKNOWN"),
                defaultText(context.getJudgeMessage(), "无"),
                defaultText(context.getFailedCaseSummary(), "无"),
                defaultText(context.getInputExample(), "无"),
                defaultText(context.getOutputExample(), "无"),
                defaultText(context.getStdoutPreview(), "无"),
                defaultText(context.getStderrPreview(), "无"),
                defaultText(context.getCode(), ""));
    }

    private String fallbackQuestion(String keyword) {
        return "请深入谈谈 " + keyword + " 的核心原理、应用场景和常见问题。";
    }

    private String fallbackJudgeAnalysis(JudgeAnalysisContext context) {
        return "判题状态为 %s。建议先结合失败用例、错误输出和边界输入复现问题，再逐项排查。"
                .formatted(defaultText(context.getStatus(), "UNKNOWN"));
    }

    private double parseScore(String raw) {
        if (!StringUtils.hasText(raw)) {
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
        } catch (NumberFormatException ignored) {
            return 3.0;
        }
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
