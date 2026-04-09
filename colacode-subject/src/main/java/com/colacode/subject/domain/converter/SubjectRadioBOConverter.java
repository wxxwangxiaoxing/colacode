package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectRadioBO;
import com.colacode.subject.infra.entity.SubjectRadio;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectRadioBOConverter {

    SubjectRadioBOConverter INSTANCE = Mappers.getMapper(SubjectRadioBOConverter.class);

    SubjectRadioBO convertToBO(SubjectRadio entity);

    SubjectRadio convertToEntity(SubjectRadioBO bo);

    List<SubjectRadioBO> convertToBOList(List<SubjectRadio> entityList);
}
