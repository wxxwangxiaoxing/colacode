package com.colacode.interview.domain.strategy.impl;

import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.domain.strategy.AnswerEvaluateStrategy;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class HybridEvaluateStrategy implements AnswerEvaluateStrategy {

    private final RuleBasedEvaluateStrategy ruleBasedEvaluateStrategy;
    private final AiSemanticEvaluateStrategy aiSemanticEvaluateStrategy;

    public HybridEvaluateStrategy(RuleBasedEvaluateStrategy ruleBasedEvaluateStrategy,
                                  AiSemanticEvaluateStrategy aiSemanticEvaluateStrategy) {
        this.ruleBasedEvaluateStrategy = ruleBasedEvaluateStrategy;
        this.aiSemanticEvaluateStrategy = aiSemanticEvaluateStrategy;
    }

    @Override
    public EvaluateResultBO evaluate(InterviewSession session, InterviewQuestionRecord questionRecord) {
        EvaluateResultBO ruleResult = ruleBasedEvaluateStrategy.evaluate(session, questionRecord);
        EvaluateResultBO aiResult = aiSemanticEvaluateStrategy.evaluate(session, questionRecord);

        EvaluateResultBO merged = new EvaluateResultBO();
        merged.setRuleScore(ruleResult.getRuleScore());
        merged.setAiScore(aiResult.getAiScore());
        merged.setHitPoints(ruleResult.getHitPoints());
        merged.setMissPoints(ruleResult.getMissPoints());
        merged.setWrongPoints(ruleResult.getWrongPoints());
        merged.setComment(aiResult.getComment() != null ? aiResult.getComment() : ruleResult.getComment());

        BigDecimal finalScore;
        if (aiResult.getAiScore() == null) {
            finalScore = ruleResult.getRuleScore();
        } else {
            finalScore = ruleResult.getRuleScore().multiply(BigDecimal.valueOf(0.4))
                    .add(aiResult.getAiScore().multiply(BigDecimal.valueOf(0.6)))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        merged.setFinalScore(finalScore);
        return merged;
    }
}