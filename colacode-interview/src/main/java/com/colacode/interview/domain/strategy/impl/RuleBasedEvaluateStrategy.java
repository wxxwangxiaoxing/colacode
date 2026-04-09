package com.colacode.interview.domain.strategy.impl;

import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.domain.strategy.AnswerEvaluateStrategy;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Order(10)
public class RuleBasedEvaluateStrategy implements AnswerEvaluateStrategy {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[，。；：、,.!?\\s()（）/\\-]+");

    @Override
    public EvaluateResultBO evaluate(InterviewSession session, InterviewQuestionRecord questionRecord) {
        EvaluateResultBO result = new EvaluateResultBO();
        String answer = safeLower(questionRecord.getUserAnswer());
        if (!StringUtils.hasText(answer)) {
            result.getWrongPoints().add("未提供有效答案");
            result.setComment("答案为空，建议先覆盖核心概念和关键步骤。");
            result.setRuleScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            result.setFinalScore(result.getRuleScore());
            return result;
        }

        List<String> expectedTerms = extractExpectedTerms(questionRecord);
        List<String> hit = new ArrayList<>();
        List<String> miss = new ArrayList<>();
        for (String term : expectedTerms) {
            if (answer.contains(term.toLowerCase(Locale.ROOT))) {
                hit.add(term);
            } else {
                miss.add(term);
            }
        }

        if (answer.length() < 20) {
            result.getWrongPoints().add("答案过短，缺少展开");
        }
        if (hit.isEmpty()) {
            result.getWrongPoints().add("未命中核心要点");
        }

        BigDecimal score = BigDecimal.valueOf(1.0D + hit.size() * 0.8D - result.getWrongPoints().size() * 0.3D);
        score = score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(5));
        score = score.setScale(2, RoundingMode.HALF_UP);

        result.setRuleScore(score);
        result.setFinalScore(score);
        result.setHitPoints(hit);
        result.setMissPoints(miss);
        result.setComment(buildComment(hit, miss, result.getWrongPoints()));
        return result;
    }

    private List<String> extractExpectedTerms(InterviewQuestionRecord questionRecord) {
        Set<String> terms = new LinkedHashSet<>();
        addTerms(terms, questionRecord.getKeyWords());
        addTerms(terms, questionRecord.getStandardAnswer());
        addTerms(terms, questionRecord.getStem());
        return terms.stream().limit(6).toList();
    }

    private void addTerms(Set<String> terms, String text) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        for (String item : SPLIT_PATTERN.split(text)) {
            String term = item.trim();
            if (term.length() >= 2) {
                terms.add(term);
            }
        }
    }

    private String buildComment(List<String> hit, List<String> miss, List<String> wrong) {
        if (!wrong.isEmpty()) {
            return "已命中 " + hit.size() + " 个关键点，仍需补足: " + String.join("、", miss);
        }
        if (!miss.isEmpty()) {
            return "回答基础较完整，但可继续补充: " + String.join("、", miss);
        }
        return "关键点覆盖较完整，表达较稳定。";
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}