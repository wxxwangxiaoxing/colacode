package com.colacode.interview.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.interview.domain.strategy.ReportGenerateStrategy;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewReport;
import com.colacode.interview.infra.entity.InterviewSession;
import com.colacode.interview.infra.mapper.InterviewReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewReportDomainService {

    private final InterviewReportMapper interviewReportMapper;
    private final ReportGenerateStrategy reportGenerateStrategy;

    public InterviewReportDomainService(InterviewReportMapper interviewReportMapper,
                                        ReportGenerateStrategy reportGenerateStrategy) {
        this.interviewReportMapper = interviewReportMapper;
        this.reportGenerateStrategy = reportGenerateStrategy;
    }

    public InterviewReport generateAndSave(InterviewSession session, List<InterviewQuestionRecord> records) {
        InterviewReport existed = findBySessionId(session.getId());
        InterviewReport generated = reportGenerateStrategy.generate(session, records);
        if (existed == null) {
            interviewReportMapper.insert(generated);
            return generated;
        }
        generated.setId(existed.getId());
        interviewReportMapper.updateById(generated);
        return generated;
    }

    public InterviewReport findBySessionId(Long sessionId) {
        LambdaQueryWrapper<InterviewReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewReport::getSessionId, sessionId).last("limit 1");
        return interviewReportMapper.selectOne(wrapper);
    }
}