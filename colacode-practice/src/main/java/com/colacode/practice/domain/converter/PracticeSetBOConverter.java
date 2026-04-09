package com.colacode.practice.domain.converter;

import com.colacode.practice.domain.bo.PracticeSetBO;
import com.colacode.practice.infra.entity.PracticeSet;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PracticeSetBOConverter {

    PracticeSetBOConverter INSTANCE = Mappers.getMapper(PracticeSetBOConverter.class);

    PracticeSetBO convertToBO(PracticeSet entity);

    PracticeSet convertToEntity(PracticeSetBO bo);

    List<PracticeSetBO> convertToBOList(List<PracticeSet> entityList);
}
