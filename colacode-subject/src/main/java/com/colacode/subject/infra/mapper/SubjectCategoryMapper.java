package com.colacode.subject.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.subject.infra.entity.SubjectCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubjectCategoryMapper extends BaseMapper<SubjectCategory> {
}
