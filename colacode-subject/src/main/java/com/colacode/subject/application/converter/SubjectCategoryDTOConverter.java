package com.colacode.subject.application.converter;

import com.colacode.subject.application.dto.SubjectCategoryDTO;
import com.colacode.subject.domain.bo.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectCategoryDTOConverter {

    SubjectCategoryDTOConverter INSTANCE = Mappers.getMapper(SubjectCategoryDTOConverter.class);

    SubjectCategoryDTO convertToDTO(SubjectCategoryBO bo);

    SubjectCategoryBO convertToBO(SubjectCategoryDTO dto);

    List<SubjectCategoryDTO> convertToDTOList(List<SubjectCategoryBO> boList);
}
