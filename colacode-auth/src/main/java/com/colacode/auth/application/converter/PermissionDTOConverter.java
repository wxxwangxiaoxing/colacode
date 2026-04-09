package com.colacode.auth.application.converter;

import com.colacode.auth.application.dto.PermissionDTO;
import com.colacode.auth.domain.bo.PermissionBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PermissionDTOConverter {

    PermissionDTOConverter INSTANCE = Mappers.getMapper(PermissionDTOConverter.class);

    PermissionDTO convertToDTO(PermissionBO bo);

    PermissionBO convertToBO(PermissionDTO dto);

    List<PermissionDTO> convertToDTOList(List<PermissionBO> boList);
}
