package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectBriefBO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.converter.SubjectBriefBOConverter;
import com.colacode.subject.infra.entity.SubjectBrief;
import com.colacode.subject.infra.mapper.SubjectBriefMapper;
import org.springframework.stereotype.Component;

@Component
public class BriefTypeHandler implements SubjectTypeHandler {

    private final SubjectBriefMapper subjectBriefMapper;

    public BriefTypeHandler(SubjectBriefMapper subjectBriefMapper) {
        this.subjectBriefMapper = subjectBriefMapper;
    }

    @Override
    public Integer getHandlerType() {
        return 4;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        SubjectBriefBO briefBO = subjectInfoBO.getBriefBO();
        if (briefBO == null) return;
        SubjectBrief entity = SubjectBriefBOConverter.INSTANCE.convertToEntity(briefBO);
        entity.setSubjectId(subjectInfoBO.getId());
        subjectBriefMapper.insert(entity);
    }

    @Override
    public void update(SubjectInfoBO subjectInfoBO) {
        Long subjectId = subjectInfoBO.getId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectBrief> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectBrief::getSubjectId, subjectId);
        subjectBriefMapper.delete(wrapper);
        add(subjectInfoBO);
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectBrief> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectBrief::getSubjectId, subjectId);
        SubjectBrief brief = subjectBriefMapper.selectOne(wrapper);
        SubjectInfoBO bo = new SubjectInfoBO();
        if (brief != null) {
            bo.setBriefBO(SubjectBriefBOConverter.INSTANCE.convertToBO(brief));
        }
        return bo;
    }
}
