package com.colacode.auth.domain.converter;

import com.colacode.auth.domain.bo.UserBO;
import com.colacode.auth.infra.entity.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserBOConverter {

    UserBOConverter INSTANCE = Mappers.getMapper(UserBOConverter.class);

    UserBO convertToBO(AuthUser entity);

    AuthUser convertToEntity(UserBO bo);

    List<UserBO> convertToBOList(List<AuthUser> entityList);
}
