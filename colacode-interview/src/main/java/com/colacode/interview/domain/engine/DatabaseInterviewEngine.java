package com.colacode.interview.domain.engine;

import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DatabaseInterviewEngine implements InterviewEngine {

    @Override
    public String engineType() {
        return "DATABASE";
    }

    @Override
    public List<KeywordBO> analyse(List<String> labels) {
        log.info("数据库引擎-简历分析, 关键词数量: {}", labels.size());
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
        log.info("数据库引擎-开始面试, 关键词: {}", selectedKeywords);
        List<InterviewQuestionBO> questions = new ArrayList<>();
        for (KeywordBO keyword : selectedKeywords) {
            InterviewQuestionBO question = new InterviewQuestionBO();
            question.setKeyWord(keyword.getKeyWord());
            question.setLabelName(keyword.getKeyWord());
            question.setSubjectName("请谈谈你对 " + keyword.getKeyWord() + " 的理解");
            question.setSubjectAnswer("参考答案: " + keyword.getKeyWord() + " 是...");
            questions.add(question);
        }
        if (questions.size() > 8) {
            Collections.shuffle(questions);
            questions = questions.subList(0, 8);
        }
        return questions;
    }

    @Override
    public InterviewResultBO submit(List<InterviewQuestionBO> questions) {
        log.info("数据库引擎-提交答案, 题目数量: {}", questions.size());
        double totalScore = 0;
        List<String> tips = new ArrayList<>();
        for (InterviewQuestionBO q : questions) {
            double score = q.getUserScore() != null ? q.getUserScore() : 0;
            totalScore += score;
            tips.add(generateTip(q, score));
        }
        double avgScore = questions.isEmpty() ? 0 : totalScore / questions.size();

        InterviewResultBO result = new InterviewResultBO();
        result.setAvgScore(Math.round(avgScore * 100.0) / 100.0);
        result.setTips(tips);
        result.setAvgTips(generateOverallTips(avgScore));
        return result;
    }

    private String generateTip(InterviewQuestionBO question, double score) {
        if (score >= 4) return question.getKeyWord() + ": 优秀";
        if (score >= 3) return question.getKeyWord() + ": 良好";
        if (score >= 2) return question.getKeyWord() + ": 一般";
        return question.getKeyWord() + ": 需要加强";
    }

    private String generateOverallTips(double avgScore) {
        if (avgScore >= 4) return "整体表现优秀，基础扎实！";
        if (avgScore >= 3) return "整体表现良好，部分知识需要巩固。";
        if (avgScore >= 2) return "基础一般，建议系统复习。";
        return "基础薄弱，建议从基础开始学习。";
    }
}
