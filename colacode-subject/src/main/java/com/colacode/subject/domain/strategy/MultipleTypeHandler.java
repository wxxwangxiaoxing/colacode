package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.bo.SubjectMultipleBO;
import com.colacode.subject.domain.converter.SubjectMultipleBOConverter;
import com.colacode.subject.infra.entity.SubjectMultiple;
import com.colacode.subject.infra.mapper.SubjectMultipleMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultipleTypeHandler implements SubjectTypeHandler {

    private final SubjectMultipleMapper subjectMultipleMapper;

    public MultipleTypeHandler(SubjectMultipleMapper subjectMultipleMapper) {
        this.subjectMultipleMapper = subjectMultipleMapper;
    }

    @Override
    public Integer getHandlerType() {
        return 2;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        List<SubjectMultipleBO> multipleList = subjectInfoBO.getMultipleList();
        if (multipleList == null) return;
        for (SubjectMultipleBO multipleBO : multipleList) {
            SubjectMultiple entity = SubjectMultipleBOConverter.INSTANCE.convertToEntity(multipleBO);
            entity.setSubjectId(subjectInfoBO.getId());
            subjectMultipleMapper.insert(entity);
        }
    }

    @Override
    public void update(SubjectInfoBO subjectInfoBO) {
        Long subjectId = subjectInfoBO.getId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectMultiple> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectMultiple::getSubjectId, subjectId);
        subjectMultipleMapper.delete(wrapper);
        add(subjectInfoBO);
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectMultiple> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectMultiple::getSubjectId, subjectId);
        List<SubjectMultiple> multipleList = subjectMultipleMapper.selectList(wrapper);
        SubjectInfoBO bo = new SubjectInfoBO();
        bo.setMultipleList(SubjectMultipleBOConverter.INSTANCE.convertToBOList(multipleList));
        return bo;
    }
}
