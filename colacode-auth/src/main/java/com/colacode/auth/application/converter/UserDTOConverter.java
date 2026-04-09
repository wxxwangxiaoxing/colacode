package com.colacode.auth.application.converter;

import com.colacode.auth.application.dto.RegisterUserDTO;
import com.colacode.auth.application.dto.UpdateUserDTO;
import com.colacode.auth.application.dto.UserDTO;
import com.colacode.auth.domain.bo.UserBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserDTOConverter {

    UserDTOConverter INSTANCE = Mappers.getMapper(UserDTOConverter.class);

    UserDTO convertToDTO(UserBO bo);

    UserBO convertToBO(UserDTO dto);

    @Mapping(target = "extJson", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserBO convertToBO(RegisterUserDTO dto);

    @Mapping(target = "extJson", ignore = true)
    UserBO convertToBO(UpdateUserDTO dto);

    List<UserDTO> convertToDTOList(List<UserBO> boList);
}
