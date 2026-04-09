package com.colacode.interview.application.converter;

import com.colacode.interview.application.dto.InterviewQuestionDTO;
import com.colacode.interview.application.dto.InterviewResultDTO;
import com.colacode.interview.application.dto.KeywordDTO;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface InterviewDTOConverter {

    InterviewDTOConverter INSTANCE = Mappers.getMapper(InterviewDTOConverter.class);

    KeywordDTO toKeywordDTO(KeywordBO bo);

    List<KeywordDTO> toKeywordDTOList(List<KeywordBO> boList);

    InterviewQuestionDTO toQuestionDTO(InterviewQuestionBO bo);

    List<InterviewQuestionDTO> toQuestionDTOList(List<InterviewQuestionBO> boList);

    InterviewResultDTO toResultDTO(InterviewResultBO bo);
}
