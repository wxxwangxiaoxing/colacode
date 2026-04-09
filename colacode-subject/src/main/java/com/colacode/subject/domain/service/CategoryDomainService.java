package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.subject.domain.bo.SubjectCategoryBO;
import com.colacode.subject.domain.converter.SubjectCategoryBOConverter;
import com.colacode.subject.infra.entity.SubjectCategory;
import com.colacode.subject.infra.mapper.SubjectCategoryMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryDomainService {

    private final SubjectCategoryMapper subjectCategoryMapper;

    public CategoryDomainService(SubjectCategoryMapper subjectCategoryMapper) {
        this.subjectCategoryMapper = subjectCategoryMapper;
    }

    @CacheEvict(value = "categoryTree", allEntries = true)
    public void addCategory(SubjectCategory entity) {
        subjectCategoryMapper.insert(entity);
    }

    @CacheEvict(value = "categoryTree", allEntries = true)
    public void updateCategory(SubjectCategory entity) {
        subjectCategoryMapper.updateById(entity);
    }

    @CacheEvict(value = "categoryTree", allEntries = true)
    public void deleteCategory(Long id) {
        subjectCategoryMapper.deleteById(id);
    }

    @Cacheable(value = "categoryTree", key = "'tree'")
    public List<SubjectCategoryBO> queryCategoryTree() {
        LambdaQueryWrapper<SubjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCategory::getCategoryType, 1);
        List<SubjectCategory> rootCategories = subjectCategoryMapper.selectList(wrapper);

        return rootCategories.stream().map(root -> {
            SubjectCategoryBO rootBO = SubjectCategoryBOConverter.INSTANCE.convertToBO(root);
            List<SubjectCategoryBO> children = getChildren(root.getId());
            rootBO.setChildren(children);
            return rootBO;
        }).collect(Collectors.toList());
    }

    public List<SubjectCategoryBO> queryPrimaryCategory() {
        LambdaQueryWrapper<SubjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCategory::getParentId, 0L);
        wrapper.orderByAsc(SubjectCategory::getId);
        List<SubjectCategory> categories = subjectCategoryMapper.selectList(wrapper);
        return SubjectCategoryBOConverter.INSTANCE.convertToBOList(categories);
    }

    public List<SubjectCategoryBO> queryCategoryByPrimary(Long parentId) {
        LambdaQueryWrapper<SubjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCategory::getParentId, parentId);
        wrapper.orderByAsc(SubjectCategory::getId);
        List<SubjectCategory> categories = subjectCategoryMapper.selectList(wrapper);
        return SubjectCategoryBOConverter.INSTANCE.convertToBOList(categories);
    }

    private List<SubjectCategoryBO> getChildren(Long parentId) {
        LambdaQueryWrapper<SubjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCategory::getParentId, parentId);
        List<SubjectCategory> children = subjectCategoryMapper.selectList(wrapper);
        return SubjectCategoryBOConverter.INSTANCE.convertToBOList(children);
    }
}
