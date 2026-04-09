package com.colacode.circle.application.converter;

import com.colacode.circle.application.dto.SensitiveWordCreateDTO;
import com.colacode.circle.application.dto.SensitiveWordDTO;
import com.colacode.circle.application.dto.ShareCommentReplyDTO;
import com.colacode.circle.application.dto.ShareMessageDTO;
import com.colacode.circle.application.dto.ShareMomentDTO;
import com.colacode.circle.domain.bo.SensitiveWordBO;
import com.colacode.circle.domain.bo.ShareCommentReplyBO;
import com.colacode.circle.domain.bo.ShareMessageBO;
import com.colacode.circle.domain.bo.ShareMomentBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CircleDTOConverter {

    CircleDTOConverter INSTANCE = Mappers.getMapper(CircleDTOConverter.class);

    ShareMomentBO toMomentBO(ShareMomentDTO dto);

    ShareMomentDTO toMomentDTO(ShareMomentBO bo);

    List<ShareMomentDTO> toMomentDTOList(List<ShareMomentBO> boList);

    ShareCommentReplyBO toCommentBO(ShareCommentReplyDTO dto);

    ShareCommentReplyDTO toCommentDTO(ShareCommentReplyBO bo);

    List<ShareCommentReplyDTO> toCommentDTOList(List<ShareCommentReplyBO> boList);

    ShareMessageDTO toMessageDTO(ShareMessageBO bo);

    List<ShareMessageDTO> toMessageDTOList(List<ShareMessageBO> boList);

    @Mapping(target = "id", ignore = true)
    SensitiveWordBO toSensitiveWordBO(SensitiveWordCreateDTO dto);

    SensitiveWordDTO toSensitiveWordDTO(SensitiveWordBO bo);

    List<SensitiveWordDTO> toSensitiveWordDTOList(List<SensitiveWordBO> boList);
}
