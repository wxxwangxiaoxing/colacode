package com.colacode.subject.application.converter;

import com.colacode.subject.application.dto.SubjectInfoDTO;
import com.colacode.subject.application.dto.SubjectCodeCaseDTO;
import com.colacode.subject.application.dto.SubjectCodeConfigDTO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.bo.SubjectCodeBO;
import com.colacode.subject.domain.bo.SubjectCodeCaseBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectInfoDTOConverter {

    SubjectInfoDTOConverter INSTANCE = Mappers.getMapper(SubjectInfoDTOConverter.class);

    SubjectInfoDTO convertToDTO(SubjectInfoBO bo);

    SubjectInfoBO convertToBO(SubjectInfoDTO dto);

    SubjectCodeConfigDTO convertToDTO(SubjectCodeBO bo);

    SubjectCodeBO convertToBO(SubjectCodeConfigDTO dto);

    SubjectCodeCaseDTO convertToDTO(SubjectCodeCaseBO bo);

    SubjectCodeCaseBO convertToBO(SubjectCodeCaseDTO dto);

    List<SubjectInfoDTO> convertToDTOList(List<SubjectInfoBO> boList);
}
