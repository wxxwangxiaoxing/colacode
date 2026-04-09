package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectMultipleBO;
import com.colacode.subject.infra.entity.SubjectMultiple;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectMultipleBOConverter {

    SubjectMultipleBOConverter INSTANCE = Mappers.getMapper(SubjectMultipleBOConverter.class);

    SubjectMultipleBO convertToBO(SubjectMultiple entity);

    SubjectMultiple convertToEntity(SubjectMultipleBO bo);

    List<SubjectMultipleBO> convertToBOList(List<SubjectMultiple> entityList);
}
