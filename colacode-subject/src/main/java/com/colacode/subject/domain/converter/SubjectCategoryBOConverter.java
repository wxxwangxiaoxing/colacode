package com.colacode.subject.domain.converter;

import com.colacode.subject.domain.bo.SubjectCategoryBO;
import com.colacode.subject.infra.entity.SubjectCategory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectCategoryBOConverter {

    SubjectCategoryBOConverter INSTANCE = Mappers.getMapper(SubjectCategoryBOConverter.class);

    SubjectCategoryBO convertToBO(SubjectCategory entity);

    SubjectCategory convertToEntity(SubjectCategoryBO bo);

    List<SubjectCategoryBO> convertToBOList(List<SubjectCategory> entityList);
}
