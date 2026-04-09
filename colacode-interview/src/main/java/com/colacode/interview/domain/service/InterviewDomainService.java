package com.colacode.interview.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;
import com.colacode.interview.domain.engine.InterviewEngine;
import com.colacode.interview.infra.entity.InterviewHistory;
import com.colacode.interview.infra.entity.InterviewQuestionHistory;
import com.colacode.interview.infra.mapper.InterviewHistoryMapper;
import com.colacode.interview.infra.mapper.InterviewQuestionHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class InterviewDomainService {

    private final List<InterviewEngine> engineList;
    private final InterviewHistoryMapper interviewHistoryMapper;
    private final InterviewQuestionHistoryMapper interviewQuestionHistoryMapper;

    private final Map<String, InterviewEngine> engineMap = new java.util.HashMap<>();

    public InterviewDomainService(List<InterviewEngine> engineList,
                                  InterviewHistoryMapper interviewHistoryMapper,
                                  InterviewQuestionHistoryMapper interviewQuestionHistoryMapper) {
        this.engineList = engineList;
        this.interviewHistoryMapper = interviewHistoryMapper;
        this.interviewQuestionHistoryMapper = interviewQuestionHistoryMapper;
        for (InterviewEngine engine : engineList) {
            engineMap.put(engine.engineType(), engine);
            log.info("注册面试引擎: {}", engine.engineType());
        }
    }

    public List<KeywordBO> analyse(String engineType, List<String> labels) {
        InterviewEngine engine = engineMap.get(engineType);
        if (engine == null) {
            throw new IllegalArgumentException("不支持的面试引擎: " + engineType);
        }
        return engine.analyse(labels);
    }

    public List<InterviewQuestionBO> startInterview(String engineType, List<KeywordBO> selectedKeywords) {
        InterviewEngine engine = engineMap.get(engineType);
        if (engine == null) {
            throw new IllegalArgumentException("不支持的面试引擎: " + engineType);
        }
        return engine.start(selectedKeywords);
    }

    public InterviewResultBO submitAnswers(String engineType, List<InterviewQuestionBO> questions) {
        InterviewEngine engine = engineMap.get(engineType);
        if (engine == null) {
            throw new IllegalArgumentException("不支持的面试引擎: " + engineType);
        }
        return engine.submit(questions);
    }

    public void saveInterviewHistory(String interviewUrl, String keyWords, Double avgScore, String tip, Long userId) {
        InterviewHistory history = new InterviewHistory();
        history.setInterviewUrl(interviewUrl);
        history.setKeyWords(keyWords);
        history.setAvgScore(avgScore);
        history.setTip(tip);
        history.setCreatedBy(String.valueOf(userId));
        interviewHistoryMapper.insert(history);
        log.info("保存面试记录成功, id: {}", history.getId());
    }

    public void saveQuestionHistory(Long interviewId, List<InterviewQuestionBO> questions, Long userId) {
        for (InterviewQuestionBO q : questions) {
            InterviewQuestionHistory history = new InterviewQuestionHistory();
            history.setInterviewId(interviewId);
            history.setKeyWords(q.getKeyWord());
            history.setQuestion(q.getSubjectName());
            history.setAnswer(q.getSubjectAnswer());
            history.setUserAnswer(q.getUserAnswer());
            history.setScore(q.getUserScore());
            history.setCreatedBy(String.valueOf(userId));
            interviewQuestionHistoryMapper.insert(history);
        }
        log.info("保存答题详情成功, interviewId: {}, count: {}", interviewId, questions.size());
    }

    public List<Map<String, Object>> getHistory(Long userId, int pageNo, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<InterviewHistory> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        LambdaQueryWrapper<InterviewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewHistory::getCreatedBy, String.valueOf(userId));
        wrapper.orderByDesc(InterviewHistory::getCreatedTime);
        interviewHistoryMapper.selectPage(page, wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (InterviewHistory h : page.getRecords()) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", h.getId());
            map.put("avgScore", h.getAvgScore());
            map.put("keyWords", h.getKeyWords());
            map.put("tip", h.getTip());
            map.put("createdTime", h.getCreatedTime());
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getDetail(Long interviewId) {
        LambdaQueryWrapper<InterviewQuestionHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestionHistory::getInterviewId, interviewId);
        wrapper.orderByAsc(InterviewQuestionHistory::getCreatedTime);
        List<InterviewQuestionHistory> questions = interviewQuestionHistoryMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (InterviewQuestionHistory q : questions) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("score", q.getScore());
            map.put("keyWords", q.getKeyWords());
            map.put("question", q.getQuestion());
            map.put("answer", q.getAnswer());
            map.put("userAnswer", q.getUserAnswer());
            result.add(map);
        }
        return result;
    }
}
