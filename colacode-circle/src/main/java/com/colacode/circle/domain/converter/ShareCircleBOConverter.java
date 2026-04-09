package com.colacode.circle.domain.converter;

import com.colacode.circle.domain.bo.ShareCircleBO;
import com.colacode.circle.infra.entity.ShareCircle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareCircleBOConverter {

    ShareCircleBOConverter INSTANCE = Mappers.getMapper(ShareCircleBOConverter.class);

    ShareCircleBO convertToBO(ShareCircle entity);

    ShareCircle convertToEntity(ShareCircleBO bo);

    List<ShareCircleBO> convertToBOList(List<ShareCircle> entityList);
}
