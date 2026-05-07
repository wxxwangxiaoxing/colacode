package com.colacode.ai.service;

import com.colacode.ai.config.AiProperties;
import com.colacode.ai.service.dto.JudgeAnalysisContext;

public class SwitchableAiService implements AiService {

    private final AiProperties aiProperties;
    private final MockAiService mockAiService;
    private final RealAiService openAiService;

    public SwitchableAiService(AiProperties aiProperties,
                               MockAiService mockAiService,
                               RealAiService openAiService) {
        this.aiProperties = aiProperties;
        this.mockAiService = mockAiService;
        this.openAiService = openAiService;
    }

    @Override
    public String generateQuestion(String keyword) {
        return currentService().generateQuestion(keyword);
    }

    @Override
    public double scoreAnswer(String question, String userAnswer) {
        return currentService().scoreAnswer(question, userAnswer);
    }

    @Override
    public String analyzeJudgeSubmission(JudgeAnalysisContext context) {
        return currentService().analyzeJudgeSubmission(context);
    }

    @Override
    public String getModelName() {
        return currentService().getModelName();
    }

    @Override
    public boolean isAvailable() {
        return currentService().isAvailable();
    }

    private AiService currentService() {
        if ("openai".equalsIgnoreCase(aiProperties.getDefaultModel())
                && openAiService != null
                && openAiService.isAvailable()) {
            return openAiService;
        }
        return mockAiService;
    }
}
