package com.colacode.subject.application.controller;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.subject.application.dto.SubjectLabelDTO;
import com.colacode.subject.domain.service.SubjectLabelDomainService;
import com.colacode.subject.infra.entity.SubjectLabel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/label")
@Tag(name = "题目标签管理", description = "题目标签的增删改查")
public class SubjectLabelController {

    private final SubjectLabelDomainService subjectLabelDomainService;

    public SubjectLabelController(SubjectLabelDomainService subjectLabelDomainService) {
        this.subjectLabelDomainService = subjectLabelDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "新增标签", description = "添加新标签")
    public Result<Void> addLabel(@RequestBody SubjectLabelDTO dto) {
        SubjectLabel label = new SubjectLabel();
        label.setLabelName(dto.getLabelName());
        label.setCategoryId(dto.getCategoryId());
        label.setSortNum(dto.getSortNum());
        subjectLabelDomainService.addLabel(label);
        return Result.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新标签", description = "更新标签信息")
    public Result<Void> updateLabel(@RequestBody SubjectLabelDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "标签ID不能为空");
        }
        SubjectLabel label = new SubjectLabel();
        label.setId(dto.getId());
        label.setLabelName(dto.getLabelName());
        label.setCategoryId(dto.getCategoryId());
        label.setSortNum(dto.getSortNum());
        subjectLabelDomainService.updateLabel(label);
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除标签", description = "删除标签")
    public Result<Void> deleteLabel(@RequestBody SubjectLabelDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "标签ID不能为空");
        }
        subjectLabelDomainService.deleteLabel(dto.getId());
        return Result.success();
    }

    @GetMapping("/queryByCategoryId")
    @Operation(summary = "根据分类获取标签", description = "根据分类ID获取标签列表")
    public Result<List<SubjectLabelDTO>> queryByCategoryId(@Parameter(description = "分类ID") @RequestParam Long categoryId) {
        List<SubjectLabel> labels = subjectLabelDomainService.queryByCategoryId(categoryId);
        List<SubjectLabelDTO> dtoList = labels.stream().map(label -> {
            SubjectLabelDTO dto = new SubjectLabelDTO();
            dto.setId(label.getId());
            dto.setLabelName(label.getLabelName());
            dto.setCategoryId(label.getCategoryId());
            dto.setSortNum(label.getSortNum());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(dtoList);
    }
}
