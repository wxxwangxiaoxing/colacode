package com.colacode.subject.application.converter;

import com.colacode.subject.application.dto.SubjectInfoDTO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectInfoDTOConverter {

    SubjectInfoDTOConverter INSTANCE = Mappers.getMapper(SubjectInfoDTOConverter.class);

    SubjectInfoDTO convertToDTO(SubjectInfoBO bo);

    SubjectInfoBO convertToBO(SubjectInfoDTO dto);

    List<SubjectInfoDTO> convertToDTOList(List<SubjectInfoBO> boList);
}
