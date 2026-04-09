package com.colacode.practice.application.converter;

import com.colacode.practice.application.dto.PracticeDetailDTO;
import com.colacode.practice.application.dto.PracticeInfoDTO;
import com.colacode.practice.application.dto.PracticeSetDTO;
import com.colacode.practice.application.dto.PracticeSubmitDTO;
import com.colacode.practice.domain.bo.PracticeDetailBO;
import com.colacode.practice.domain.bo.PracticeInfoBO;
import com.colacode.practice.domain.bo.PracticeSetBO;
import com.colacode.practice.domain.bo.PracticeSubmitBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PracticeDTOConverter {

    PracticeDTOConverter INSTANCE = Mappers.getMapper(PracticeDTOConverter.class);

    PracticeSetBO toSetBO(PracticeSetDTO dto);

    PracticeSetDTO toSetDTO(PracticeSetBO bo);

    List<PracticeSetDTO> toSetDTOList(List<PracticeSetBO> boList);

    PracticeSubmitBO toSubmitBO(PracticeSubmitDTO dto);

    PracticeInfoDTO toInfoDTO(PracticeInfoBO bo);

    List<PracticeInfoDTO> toInfoDTOList(List<PracticeInfoBO> boList);

    PracticeDetailDTO toDetailDTO(PracticeDetailBO bo);

    List<PracticeDetailDTO> toDetailDTOList(List<PracticeDetailBO> boList);
}
