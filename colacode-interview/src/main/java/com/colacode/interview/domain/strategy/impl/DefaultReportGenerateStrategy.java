package com.colacode.interview.domain.strategy.impl;

import com.colacode.interview.domain.strategy.ReportGenerateStrategy;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewReport;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultReportGenerateStrategy implements ReportGenerateStrategy {

    @Override
    public InterviewReport generate(InterviewSession session, List<InterviewQuestionRecord> questionRecords) {
        InterviewReport report = new InterviewReport();
        report.setSessionId(session.getId());
        report.setUserId(session.getUserId());

        BigDecimal total = questionRecords.stream()
                .map(InterviewQuestionRecord::getFinalScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal count = BigDecimal.valueOf(Math.max(1, questionRecords.size()));
        BigDecimal avg = total.divide(count, 2, RoundingMode.HALF_UP);

        List<String> weaknessTags = questionRecords.stream()
                .filter(item -> item.getFinalScore() != null && item.getFinalScore().compareTo(BigDecimal.valueOf(3)) < 0)
                .map(InterviewQuestionRecord::getKeyWords)
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .toList();
        List<String> advantageTags = questionRecords.stream()
                .filter(item -> item.getFinalScore() != null && item.getFinalScore().compareTo(BigDecimal.valueOf(4)) >= 0)
                .map(InterviewQuestionRecord::getKeyWords)
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .toList();

        report.setTotalScore(avg);
        report.setBaseScore(avg);
        report.setLogicScore(calcDimensionScore(questionRecords, 30));
        report.setExpressionScore(calcDimensionScore(questionRecords, 50));
        report.setEngineeringScore(calcDimensionScore(questionRecords, 80));
        report.setSummary(buildSummary(session, avg, weaknessTags, advantageTags));
        report.setSuggestion(buildSuggestion(weaknessTags));
        report.setWeaknessTagsJson(toJsonArray(weaknessTags));
        report.setAdvantageTagsJson(toJsonArray(advantageTags));
        report.setRecommendedPracticeJson(toJsonArray(weaknessTags.stream().limit(3).collect(Collectors.toList())));
        return report;
    }

    private BigDecimal calcDimensionScore(List<InterviewQuestionRecord> records, int baseLength) {
        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long count = records.stream().filter(record -> record.getUserAnswer() != null && record.getUserAnswer().length() >= baseLength).count();
        return BigDecimal.valueOf(count * 5.0 / records.size()).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildSummary(InterviewSession session, BigDecimal avg, List<String> weaknessTags, List<String> advantageTags) {
        return "本次 " + session.getPostType() + " 面试共完成 " + session.getTotalQuestionCount()
                + " 题，综合得分 " + avg + "。优势标签: " + String.join("、", advantageTags)
                + "；待加强标签: " + String.join("、", weaknessTags) + "。";
    }

    private String buildSuggestion(List<String> weaknessTags) {
        if (weaknessTags.isEmpty()) {
            return "整体表现稳定，建议继续通过项目题和场景题提升表达深度。";
        }
        return "建议优先复习以下知识点并安排专项练习: " + String.join("、", weaknessTags);
    }

    private String toJsonArray(List<String> items) {
        List<String> safeItems = new ArrayList<>(items);
        safeItems.sort(Comparator.naturalOrder());
        return safeItems.stream().map(item -> "\"" + item + "\"").collect(Collectors.joining(",", "[", "]"));
    }
}