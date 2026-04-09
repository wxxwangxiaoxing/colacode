package com.colacode.interview.domain.strategy;

import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewReport;
import com.colacode.interview.infra.entity.InterviewSession;

import java.util.List;

public interface ReportGenerateStrategy {
    InterviewReport generate(InterviewSession session, List<InterviewQuestionRecord> questionRecords);
}