package com.colacode.interview.domain.service;

import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.domain.strategy.impl.HybridEvaluateStrategy;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.stereotype.Service;

@Service
public class InterviewEvaluationDomainService {

    private final HybridEvaluateStrategy hybridEvaluateStrategy;

    public InterviewEvaluationDomainService(HybridEvaluateStrategy hybridEvaluateStrategy) {
        this.hybridEvaluateStrategy = hybridEvaluateStrategy;
    }

    public EvaluateResultBO evaluate(InterviewSession session, InterviewQuestionRecord questionRecord) {
        return hybridEvaluateStrategy.evaluate(session, questionRecord);
    }
}