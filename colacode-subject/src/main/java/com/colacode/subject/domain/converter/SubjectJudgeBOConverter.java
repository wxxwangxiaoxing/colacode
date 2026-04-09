package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectJudgeBO;
import com.colacode.subject.infra.entity.SubjectJudge;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubjectJudgeBOConverter {

    SubjectJudgeBOConverter INSTANCE = Mappers.getMapper(SubjectJudgeBOConverter.class);

    SubjectJudgeBO convertToBO(SubjectJudge entity);

    SubjectJudge convertToEntity(SubjectJudgeBO bo);
}
