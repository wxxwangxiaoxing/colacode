package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.subject.infra.entity.SubjectLiked;
import com.colacode.subject.infra.mapper.SubjectLikedMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectLikedDomainService {

    private final SubjectLikedMapper subjectLikedMapper;

    public SubjectLikedDomainService(SubjectLikedMapper subjectLikedMapper) {
        this.subjectLikedMapper = subjectLikedMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(SubjectLiked liked) {
        LambdaQueryWrapper<SubjectLiked> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectLiked::getSubjectId, liked.getSubjectId());
        wrapper.eq(SubjectLiked::getLikedUserId, liked.getLikedUserId());
        SubjectLiked exist = subjectLikedMapper.selectOne(wrapper);
        if (exist != null) {
            exist.setLikedStatus(liked.getLikedStatus());
            subjectLikedMapper.updateById(exist);
            return;
        }

        try {
            subjectLikedMapper.insert(liked);
        } catch (DuplicateKeyException e) {
            SubjectLiked retryExist = subjectLikedMapper.selectOne(wrapper);
            if (retryExist == null) {
                throw e;
            }
            retryExist.setLikedStatus(liked.getLikedStatus());
            subjectLikedMapper.updateById(retryExist);
        }
    }

    public Page<SubjectLiked> getLikedPage(Long userId, int pageNo, int pageSize) {
        Page<SubjectLiked> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SubjectLiked> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectLiked::getLikedUserId, userId);
        wrapper.eq(SubjectLiked::getLikedStatus, 1);
        wrapper.orderByDesc(SubjectLiked::getCreatedTime);
        return subjectLikedMapper.selectPage(page, wrapper);
    }

    public void delete(Long id) {
        subjectLikedMapper.deleteById(id);
    }
}
