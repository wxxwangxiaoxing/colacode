package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.subject.infra.entity.SubjectFavorite;
import com.colacode.subject.infra.mapper.SubjectFavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectFavoriteDomainService {

    private final SubjectFavoriteMapper subjectFavoriteMapper;

    public SubjectFavoriteDomainService(SubjectFavoriteMapper subjectFavoriteMapper) {
        this.subjectFavoriteMapper = subjectFavoriteMapper;
    }

    public void addFavorite(Long subjectId, Long userId) {
        LambdaQueryWrapper<SubjectFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectFavorite::getSubjectId, subjectId);
        wrapper.eq(SubjectFavorite::getUserId, userId);
        SubjectFavorite exist = subjectFavoriteMapper.selectOne(wrapper);
        if (exist != null) {
            return;
        }
        SubjectFavorite favorite = new SubjectFavorite();
        favorite.setSubjectId(subjectId);
        favorite.setUserId(userId);
        subjectFavoriteMapper.insert(favorite);
    }

    public void removeFavorite(Long subjectId, Long userId) {
        LambdaQueryWrapper<SubjectFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectFavorite::getSubjectId, subjectId);
        wrapper.eq(SubjectFavorite::getUserId, userId);
        subjectFavoriteMapper.delete(wrapper);
    }

    public Page<SubjectFavorite> getFavoritePage(Long userId, int pageNo, int pageSize) {
        Page<SubjectFavorite> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SubjectFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectFavorite::getUserId, userId);
        wrapper.orderByDesc(SubjectFavorite::getCreatedTime);
        return subjectFavoriteMapper.selectPage(page, wrapper);
    }

    public boolean isFavorited(Long subjectId, Long userId) {
        LambdaQueryWrapper<SubjectFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectFavorite::getSubjectId, subjectId);
        wrapper.eq(SubjectFavorite::getUserId, userId);
        return subjectFavoriteMapper.selectCount(wrapper) > 0;
    }
}
