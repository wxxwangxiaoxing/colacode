package com.colacode.circle.application.converter;

import com.colacode.circle.application.dto.ShareCircleDTO;
import com.colacode.circle.domain.bo.ShareCircleBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareCircleDTOConverter {

    ShareCircleDTOConverter INSTANCE = Mappers.getMapper(ShareCircleDTOConverter.class);

    ShareCircleDTO convertToDTO(ShareCircleBO bo);

    ShareCircleBO convertToBO(ShareCircleDTO dto);

    List<ShareCircleDTO> convertToDTOList(List<ShareCircleBO> boList);
}
