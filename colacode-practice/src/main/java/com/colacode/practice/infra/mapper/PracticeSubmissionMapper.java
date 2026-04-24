package com.colacode.practice.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.practice.infra.entity.PracticeSubmission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PracticeSubmissionMapper extends BaseMapper<PracticeSubmission> {
}
