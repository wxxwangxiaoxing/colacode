package com.colacode.auth.domain.converter;

import com.colacode.auth.domain.bo.PermissionBO;
import com.colacode.auth.infra.entity.AuthPermission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PermissionBOConverter {

    PermissionBOConverter INSTANCE = Mappers.getMapper(PermissionBOConverter.class);

    PermissionBO convertToBO(AuthPermission entity);

    AuthPermission convertToEntity(PermissionBO bo);

    List<PermissionBO> convertToBOList(List<AuthPermission> entityList);
}
