package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.subject.infra.entity.SubjectWrong;
import com.colacode.subject.infra.mapper.SubjectWrongMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectWrongDomainService {

    private final SubjectWrongMapper subjectWrongMapper;

    public SubjectWrongDomainService(SubjectWrongMapper subjectWrongMapper) {
        this.subjectWrongMapper = subjectWrongMapper;
    }

    public void recordWrong(Long subjectId, Long userId, String wrongAnswer) {
        LambdaQueryWrapper<SubjectWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectWrong::getSubjectId, subjectId);
        wrapper.eq(SubjectWrong::getUserId, userId);
        SubjectWrong exist = subjectWrongMapper.selectOne(wrapper);
        if (exist != null) {
            exist.setWrongCount(exist.getWrongCount() + 1);
            exist.setLastWrongAnswer(wrongAnswer);
            subjectWrongMapper.updateById(exist);
        } else {
            SubjectWrong wrong = new SubjectWrong();
            wrong.setSubjectId(subjectId);
            wrong.setUserId(userId);
            wrong.setWrongCount(1);
            wrong.setLastWrongAnswer(wrongAnswer);
            subjectWrongMapper.insert(wrong);
        }
    }

    public void removeWrong(Long subjectId, Long userId) {
        LambdaQueryWrapper<SubjectWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectWrong::getSubjectId, subjectId);
        wrapper.eq(SubjectWrong::getUserId, userId);
        subjectWrongMapper.delete(wrapper);
    }

    public Page<SubjectWrong> getWrongPage(Long userId, int pageNo, int pageSize) {
        Page<SubjectWrong> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SubjectWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectWrong::getUserId, userId);
        wrapper.orderByDesc(SubjectWrong::getWrongCount);
        return subjectWrongMapper.selectPage(page, wrapper);
    }

    public List<SubjectWrong> getWrongList(Long userId) {
        LambdaQueryWrapper<SubjectWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectWrong::getUserId, userId);
        wrapper.orderByDesc(SubjectWrong::getWrongCount);
        return subjectWrongMapper.selectList(wrapper);
    }

    public void clearWrong(Long userId) {
        LambdaQueryWrapper<SubjectWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectWrong::getUserId, userId);
        subjectWrongMapper.delete(wrapper);
    }
}
