package com.colacode.interview.domain.strategy;

import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;

public interface AnswerEvaluateStrategy {
    EvaluateResultBO evaluate(InterviewSession session, InterviewQuestionRecord questionRecord);
}