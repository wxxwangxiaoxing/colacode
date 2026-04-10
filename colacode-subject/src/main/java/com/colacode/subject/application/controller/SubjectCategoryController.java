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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subject/category")
@Tag(name = "题目分类管理", description = "题目分类的增删改查")
public class SubjectCategoryController {

    private final CategoryDomainService categoryDomainService;
    private final SubjectLabelDomainService subjectLabelDomainService;

    public SubjectCategoryController(CategoryDomainService categoryDomainService,
                                     SubjectLabelDomainService subjectLabelDomainService) {
        this.categoryDomainService = categoryDomainService;
        this.subjectLabelDomainService = subjectLabelDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "新增分类", description = "添加新分类")
    public Result<Void> addCategory(@RequestBody SubjectCategoryDTO dto) {
        SubjectCategory entity = new SubjectCategory();
        entity.setCategoryName(dto.getCategoryName());
        entity.setCategoryType(dto.getCategoryType());
        entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        categoryDomainService.addCategory(entity);
        return Result.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新分类", description = "更新分类信息")
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
    @Operation(summary = "删除分类", description = "删除分类")
    public Result<Void> deleteCategory(@RequestBody SubjectCategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "分类ID不能为空");
        }
        categoryDomainService.deleteCategory(dto.getId());
        return Result.success();
    }

    @GetMapping("/queryTree")
    @Operation(summary = "获取分类树", description = "获取所有分类的树形结构")
    public Result<List<SubjectCategoryDTO>> queryTree() {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryCategoryTree();
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryPrimaryCategory")
    @Operation(summary = "获取一级分类", description = "获取所有一级分类")
    public Result<List<SubjectCategoryDTO>> queryPrimaryCategory() {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryPrimaryCategory();
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryCategoryByPrimary")
    @Operation(summary = "获取子分类", description = "根据父分类ID获取子分类")
    public Result<List<SubjectCategoryDTO>> queryCategoryByPrimary(@Parameter(description = "父分类ID") @RequestParam Long parentId) {
        List<SubjectCategoryBO> categoryBOList = categoryDomainService.queryCategoryByPrimary(parentId);
        return Result.success(SubjectCategoryDTOConverter.INSTANCE.convertToDTOList(categoryBOList));
    }

    @GetMapping("/queryCategoryAndLabel")
    @Operation(summary = "获取分类及标签", description = "获取所有分类及其关联的标签")
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
