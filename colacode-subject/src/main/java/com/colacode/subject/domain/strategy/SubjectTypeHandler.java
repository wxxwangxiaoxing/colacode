package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectInfoBO;

public interface SubjectTypeHandler {

    Integer getHandlerType();

    void add(SubjectInfoBO subjectInfoBO);

    void update(SubjectInfoBO subjectInfoBO);

    SubjectInfoBO query(Long subjectId);
}
