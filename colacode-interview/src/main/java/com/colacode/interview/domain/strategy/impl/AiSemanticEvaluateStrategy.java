package com.colacode.interview.domain.strategy.impl;

import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.strategy.AnswerEvaluateStrategy;
import com.colacode.interview.domain.service.InterviewDomainService;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;

@Component
@Order(20)
public class AiSemanticEvaluateStrategy implements AnswerEvaluateStrategy {

    private final InterviewDomainService interviewDomainService;

    public AiSemanticEvaluateStrategy(InterviewDomainService interviewDomainService) {
        this.interviewDomainService = interviewDomainService;
    }

    @Override
    public EvaluateResultBO evaluate(InterviewSession session, InterviewQuestionRecord questionRecord) {
        EvaluateResultBO result = new EvaluateResultBO();
        if (!"AI".equalsIgnoreCase(session.getEngineType())) {
            return result;
        }

        InterviewQuestionBO questionBO = new InterviewQuestionBO();
        questionBO.setKeyWord(questionRecord.getKeyWords());
        questionBO.setLabelName(questionRecord.getCategory());
        questionBO.setSubjectName(questionRecord.getStem());
        questionBO.setSubjectAnswer(questionRecord.getStandardAnswer());
        questionBO.setUserAnswer(questionRecord.getUserAnswer());

        InterviewResultBO interviewResult = interviewDomainService.submitAnswers(session.getEngineType(), Collections.singletonList(questionBO));
        Double aiScore = questionBO.getUserScore() != null ? questionBO.getUserScore() : interviewResult.getAvgScore();
        if (aiScore != null) {
            result.setAiScore(BigDecimal.valueOf(aiScore).setScale(2, RoundingMode.HALF_UP));
            result.setComment(interviewResult.getAvgTips());
        }
        return result;
    }
}