package com.colacode.interview.domain.engine;

import com.colacode.common.Result;
import com.colacode.interview.application.feign.AiGatewayFeignClient;
import com.colacode.interview.application.feign.dto.AiGenerateQuestionReqDTO;
import com.colacode.interview.application.feign.dto.AiGenerateQuestionRespDTO;
import com.colacode.interview.application.feign.dto.AiScoreAnswerReqDTO;
import com.colacode.interview.application.feign.dto.AiScoreAnswerRespDTO;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class AiInterviewEngine implements InterviewEngine {

    private static final double DEFAULT_AI_SCORE = 3.0D;

    private final AiGatewayFeignClient aiGatewayFeignClient;

    public AiInterviewEngine(AiGatewayFeignClient aiGatewayFeignClient) {
        this.aiGatewayFeignClient = aiGatewayFeignClient;
    }

    @Value("${interview.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${interview.ai.api-key:}")
    private String apiKey;

    @Override
    public String engineType() {
        return "AI";
    }

    @Override
    public List<KeywordBO> analyse(List<String> labels) {
        log.info("AI引擎-简历分析, 关键词数量: {}", labels.size());
        List<KeywordBO> result = new ArrayList<>();
        for (String label : labels) {
            KeywordBO bo = new KeywordBO();
            bo.setKeyWord(label);
            result.add(bo);
        }
        return result;
    }

    @Override
    public List<InterviewQuestionBO> start(List<KeywordBO> selectedKeywords) {
        log.info("AI引擎-开始面试, 关键词: {}", selectedKeywords);
        if (!aiEnabled) {
            log.warn("AI引擎未启用，当前将使用本地兜底题目文本");
        }
        List<InterviewQuestionBO> questions = new ArrayList<>();

        List<KeywordBO> shuffled = new ArrayList<>(selectedKeywords);
        Collections.shuffle(shuffled);
        int count = Math.min(shuffled.size(), 8);

        for (int i = 0; i < count; i++) {
            KeywordBO keyword = shuffled.get(i);
            InterviewQuestionBO question = new InterviewQuestionBO();
            question.setKeyWord(keyword.getKeyWord());
            question.setLabelName(keyword.getKeyWord());

            if (aiEnabled) {
                String generatedQuestion = callAiGenerateQuestion(keyword.getKeyWord());
                question.setSubjectName(generatedQuestion);
            } else {
                question.setSubjectName(buildFallbackQuestion(keyword.getKeyWord()));
            }
            question.setSubjectAnswer("AI 参考答案...");
            questions.add(question);
        }
        return questions;
    }

    @Override
    public InterviewResultBO submit(List<InterviewQuestionBO> questions) {
        log.info("AI引擎-提交答案, 题目数量: {}", questions.size());
        if (!aiEnabled) {
            log.warn("AI引擎未启用，当前将使用默认分数兜底");
        }
        double totalScore = 0;
        boolean usedFallbackScore = false;
        List<String> tips = new ArrayList<>();

        for (InterviewQuestionBO q : questions) {
            double score;
            if (aiEnabled) {
                AiScoreOutcome scoreOutcome = callAiScoreAnswer(q.getSubjectName(), q.getUserAnswer());
                score = scoreOutcome.score();
                usedFallbackScore = usedFallbackScore || scoreOutcome.fallback();
            } else {
                score = q.getUserScore() != null ? q.getUserScore() : DEFAULT_AI_SCORE;
                usedFallbackScore = true;
            }
            q.setUserScore(score);
            totalScore += score;
            tips.add(generateTip(q, score));
        }

        double avgScore = questions.isEmpty() ? 0 : totalScore / questions.size();

        InterviewResultBO result = new InterviewResultBO();
        result.setAvgScore(Math.round(avgScore * 100.0) / 100.0);
        result.setTips(tips);
        result.setAvgTips(generateOverallTips(avgScore, usedFallbackScore));
        return result;
    }

    private String callAiGenerateQuestion(String keyword) {
        try {
            AiGenerateQuestionReqDTO reqDTO = new AiGenerateQuestionReqDTO();
            reqDTO.setKeyword(keyword);
            reqDTO.setApiKey(apiKey);
            Result<AiGenerateQuestionRespDTO> result = aiGatewayFeignClient.generateQuestion(reqDTO);
            if (result != null && result.isSuccess() && result.getData() != null
                    && result.getData().getQuestion() != null && !result.getData().getQuestion().trim().isEmpty()) {
                return result.getData().getQuestion();
            }
            log.warn("AI 生成题目返回为空，使用兜底题目, keyword: {}, result: {}", keyword, result);
        } catch (Exception e) {
            log.warn("调用 AI 生成题目失败，使用兜底题目, keyword: {}", keyword, e);
        }
        return buildFallbackQuestion(keyword);
    }

    private AiScoreOutcome callAiScoreAnswer(String question, String userAnswer) {
        try {
            AiScoreAnswerReqDTO reqDTO = new AiScoreAnswerReqDTO();
            reqDTO.setQuestion(question);
            reqDTO.setUserAnswer(userAnswer);
            reqDTO.setApiKey(apiKey);
            Result<AiScoreAnswerRespDTO> result = aiGatewayFeignClient.scoreAnswer(reqDTO);
            if (result != null && result.isSuccess() && result.getData() != null && result.getData().getScore() != null) {
                return new AiScoreOutcome(result.getData().getScore(), false);
            }
            log.warn("AI 评分返回为空，使用默认分数兜底, result: {}", result);
        } catch (Exception e) {
            log.warn("调用 AI 评分失败，使用默认分数兜底", e);
        }
        return new AiScoreOutcome(DEFAULT_AI_SCORE, true);
    }

    private String generateTip(InterviewQuestionBO question, double score) {
        if (score >= 4) return question.getKeyWord() + ": 优秀";
        if (score >= 3) return question.getKeyWord() + ": 良好";
        if (score >= 2) return question.getKeyWord() + ": 一般";
        return question.getKeyWord() + ": 需要加强";
    }

    private String generateOverallTips(double avgScore, boolean usedFallbackScore) {
        String baseTips;
        if (avgScore >= 4) {
            baseTips = "整体表现优秀，基础扎实！";
        } else if (avgScore >= 3) {
            baseTips = "整体表现良好，部分知识需要巩固。";
        } else if (avgScore >= 2) {
            baseTips = "基础一般，建议系统复习。";
        } else {
            baseTips = "基础薄弱，建议从基础开始学习。";
        }
        return usedFallbackScore ? baseTips + "（AI评分回退，当前结果包含默认分数）" : baseTips;
    }

    private String buildFallbackQuestion(String keyword) {
        return "请深入谈谈 " + keyword + " 的核心原理、应用场景和常见问题";
    }

    private record AiScoreOutcome(double score, boolean fallback) {
    }
}
