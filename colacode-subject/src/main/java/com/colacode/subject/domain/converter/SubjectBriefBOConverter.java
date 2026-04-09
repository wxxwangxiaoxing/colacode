package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectBriefBO;
import com.colacode.subject.infra.entity.SubjectBrief;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubjectBriefBOConverter {

    SubjectBriefBOConverter INSTANCE = Mappers.getMapper(SubjectBriefBOConverter.class);

    SubjectBriefBO convertToBO(SubjectBrief entity);

    SubjectBrief convertToEntity(SubjectBriefBO bo);
}
