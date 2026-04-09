package com.colacode.subject.application.controller;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.subject.application.converter.SubjectCategoryDTOConverter;
import com.colacode.subject.application.dto.SubjectCategoryDTO;
import com.colacode.subject.application.dto.SubjectLabelDTO;
import com.colacode.subject.domain.bo.SubjectCategoryBO;
import com.colacode.subject.domain.service.CategoryDomainService;
import com.colacode.subject.domain.service.SubjectLabelDomainService;
import com.colacode.subject.infra.entity.SubjectCategory;
import com.colacode.subject.infra.entity.SubjectLabel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subject/category")
public class SubjectCategoryController {

    private final CategoryDomainService categoryDomainService;
    private final SubjectLabelDomainService subjectLabelDomainService;

    public SubjectCategoryController(CategoryDomainService categoryDomainService,
                                     SubjectLabelDomainService subjectLabelDomainService) {
        this.categoryDomainService = categoryDomainService;
        this.subjectLabelDomainService = subjectLabelDomainService;
    }

    @PostMapping("/add")
    public Result<Void> addCategory(@RequestBody SubjectCategoryDTO dto) {
        SubjectCategory entity = new SubjectCategory();
        entity.setCategoryName(dto.getCategoryName());
        entity.setCategoryType(dto.getCategoryType());
        entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        categoryDomainService.addCategory(entity);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateCategory(@RequestBody SubjectCategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "分类ID不能为空");
        }
        SubjectCategory entity = new SubjectCategory();
        entity.setId(dto.getId());
        entity.setCategoryName(dto.getCategoryName());
        entity.setCategoryType(dto.getCategoryType());
        entity.setParentId(dto.getParentId());
        categoryDomainService.updateCategory(entity);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteCategory(@RequestBody SubjectCategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "分类ID不能为空");
        }
        categoryDomainService.deleteCategory(dto.getId());
        return Result.success();
    }

    @GetMapping("/queryTree")
    public Result<List<SubjectCategoryDTO>> queryTree() {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryCategoryTree();
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryPrimaryCategory")
    public Result<List<SubjectCategoryDTO>> queryPrimaryCategory() {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryPrimaryCategory();
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryCategoryByPrimary")
    public Result<List<SubjectCategoryDTO>> queryCategoryByPrimary(@RequestParam Long parentId) {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryCategoryByPrimary(parentId);
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryCategoryAndLabel")
    public Result<List<SubjectCategoryDTO>> queryCategoryAndLabel() {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryCategoryTree();
        List<SubjectCategoryDTO> dtoList = categoryBOList.stream().map(bo -> {
            SubjectCategoryDTO dto = SubjectCategoryDTOConverter.INSTANCE.convertToDTO(bo);
            List<SubjectLabel> labels = subjectLabelDomainService.queryByCategoryId(bo.getId());
            List<SubjectLabelDTO> labelDTOs = labels.stream().map(label -> {
                SubjectLabelDTO labelDTO = new SubjectLabelDTO();
                labelDTO.setId(label.getId());
                labelDTO.setLabelName(label.getLabelName());
                labelDTO.setCategoryId(label.getCategoryId());
                labelDTO.setSortNum(label.getSortNum());
                return labelDTO;
            }).collect(Collectors.toList());
            dto.setLabels(labelDTOs);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }
}
