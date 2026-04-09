package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.bo.SubjectRadioBO;
import com.colacode.subject.domain.converter.SubjectRadioBOConverter;
import com.colacode.subject.infra.entity.SubjectRadio;
import com.colacode.subject.infra.mapper.SubjectRadioMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RadioTypeHandler implements SubjectTypeHandler {

    private final SubjectRadioMapper subjectRadioMapper;

    public RadioTypeHandler(SubjectRadioMapper subjectRadioMapper) {
        this.subjectRadioMapper = subjectRadioMapper;
    }

    @Override
    public Integer getHandlerType() {
        return 1;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        List<SubjectRadioBO> radioList = subjectInfoBO.getRadioList();
        if (radioList == null) return;
        for (SubjectRadioBO radioBO : radioList) {
            SubjectRadio entity = SubjectRadioBOConverter.INSTANCE.convertToEntity(radioBO);
            entity.setSubjectId(subjectInfoBO.getId());
            subjectRadioMapper.insert(entity);
        }
    }

    @Override
    public void update(SubjectInfoBO subjectInfoBO) {
        Long subjectId = subjectInfoBO.getId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectRadio> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectRadio::getSubjectId, subjectId);
        subjectRadioMapper.delete(wrapper);
        add(subjectInfoBO);
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectRadio> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectRadio::getSubjectId, subjectId);
        List<SubjectRadio> radioList = subjectRadioMapper.selectList(wrapper);
        SubjectInfoBO bo = new SubjectInfoBO();
        bo.setRadioList(SubjectRadioBOConverter.INSTANCE.convertToBOList(radioList));
        return bo;
    }
}
