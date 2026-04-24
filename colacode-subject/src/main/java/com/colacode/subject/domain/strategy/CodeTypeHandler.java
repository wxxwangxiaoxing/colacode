package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.service.SubjectCodeDomainService;
import org.springframework.stereotype.Component;

@Component
public class CodeTypeHandler implements SubjectTypeHandler {

    private final SubjectCodeDomainService subjectCodeDomainService;

    public CodeTypeHandler(SubjectCodeDomainService subjectCodeDomainService) {
        this.subjectCodeDomainService = subjectCodeDomainService;
    }

    @Override
    public Integer getHandlerType() {
        return SubjectCodeDomainService.SUBJECT_TYPE_CODE;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        subjectCodeDomainService.saveCodeSubject(
                subjectInfoBO.getId(),
                subjectInfoBO.getCodeConfig(),
                subjectInfoBO.getTestCases());
    }

    @Override
    public void update(SubjectInfoBO subjectInfoBO) {
        subjectCodeDomainService.saveCodeSubject(
                subjectInfoBO.getId(),
                subjectInfoBO.getCodeConfig(),
                subjectInfoBO.getTestCases());
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        return subjectCodeDomainService.queryPublicDetail(subjectId);
    }
}
