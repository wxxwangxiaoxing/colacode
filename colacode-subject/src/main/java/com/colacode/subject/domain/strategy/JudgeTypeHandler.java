package com.colacode.subject.domain.strategy;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.bo.SubjectJudgeBO;
import com.colacode.subject.domain.converter.SubjectJudgeBOConverter;
import com.colacode.subject.infra.entity.SubjectJudge;
import com.colacode.subject.infra.mapper.SubjectJudgeMapper;
import org.springframework.stereotype.Component;

@Component
public class JudgeTypeHandler implements SubjectTypeHandler {

    private final SubjectJudgeMapper subjectJudgeMapper;

    public JudgeTypeHandler(SubjectJudgeMapper subjectJudgeMapper) {
        this.subjectJudgeMapper = subjectJudgeMapper;
    }

    @Override
    public Integer getHandlerType() {
        return 3;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        SubjectJudgeBO judgeBO = subjectInfoBO.getJudgeBO();
        if (judgeBO == null) return;
        SubjectJudge entity = SubjectJudgeBOConverter.INSTANCE.convertToEntity(judgeBO);
        entity.setSubjectId(subjectInfoBO.getId());
        subjectJudgeMapper.insert(entity);
    }

    @Override
    public void update(SubjectInfoBO subjectInfoBO) {
        Long subjectId = subjectInfoBO.getId();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectJudge> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectJudge::getSubjectId, subjectId);
        subjectJudgeMapper.delete(wrapper);
        add(subjectInfoBO);
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SubjectJudge> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SubjectJudge::getSubjectId, subjectId);
        SubjectJudge judge = subjectJudgeMapper.selectOne(wrapper);
        SubjectInfoBO bo = new SubjectInfoBO();
        if (judge != null) {
            bo.setJudgeBO(SubjectJudgeBOConverter.INSTANCE.convertToBO(judge));
        }
        return bo;
    }
}
