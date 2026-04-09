package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.infra.entity.SubjectInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubjectInfoBOConverter {

    SubjectInfoBOConverter INSTANCE = Mappers.getMapper(SubjectInfoBOConverter.class);

    SubjectInfoBO convertToBO(SubjectInfo entity);

    SubjectInfo convertToEntity(SubjectInfoBO bo);
}
