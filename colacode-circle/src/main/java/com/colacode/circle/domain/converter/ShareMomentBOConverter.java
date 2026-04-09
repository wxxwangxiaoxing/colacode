package com.colacode.circle.domain.converter;

import com.colacode.circle.domain.bo.ShareMomentBO;
import com.colacode.circle.infra.entity.ShareMoment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareMomentBOConverter {

    ShareMomentBOConverter INSTANCE = Mappers.getMapper(ShareMomentBOConverter.class);

    ShareMomentBO convertToBO(ShareMoment entity);

    ShareMoment convertToEntity(ShareMomentBO bo);

    List<ShareMomentBO> convertToBOList(List<ShareMoment> entityList);
}
