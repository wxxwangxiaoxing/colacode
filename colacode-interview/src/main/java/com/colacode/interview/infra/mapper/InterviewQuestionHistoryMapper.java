package com.colacode.interview.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.interview.infra.entity.InterviewQuestionHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewQuestionHistoryMapper extends BaseMapper<InterviewQuestionHistory> {
}
