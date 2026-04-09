package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.subject.infra.entity.SubjectLabel;
import com.colacode.subject.infra.mapper.SubjectLabelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectLabelDomainService {

    private final SubjectLabelMapper subjectLabelMapper;

    public SubjectLabelDomainService(SubjectLabelMapper subjectLabelMapper) {
        this.subjectLabelMapper = subjectLabelMapper;
    }

    public void addLabel(SubjectLabel label) {
        subjectLabelMapper.insert(label);
    }

    public void updateLabel(SubjectLabel label) {
        subjectLabelMapper.updateById(label);
    }

    public void deleteLabel(Long id) {
        subjectLabelMapper.deleteById(id);
    }

    public List<SubjectLabel> queryByCategoryId(Long categoryId) {
        LambdaQueryWrapper<SubjectLabel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectLabel::getCategoryId, categoryId);
        wrapper.orderByAsc(SubjectLabel::getSortNum);
        return subjectLabelMapper.selectList(wrapper);
    }
}
