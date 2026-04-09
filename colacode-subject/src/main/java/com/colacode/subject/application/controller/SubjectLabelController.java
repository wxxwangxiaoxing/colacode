package com.colacode.subject.application.controller;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.subject.application.dto.SubjectLabelDTO;
import com.colacode.subject.domain.service.SubjectLabelDomainService;
import com.colacode.subject.infra.entity.SubjectLabel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/label")
public class SubjectLabelController {

    private final SubjectLabelDomainService subjectLabelDomainService;

    public SubjectLabelController(SubjectLabelDomainService subjectLabelDomainService) {
        this.subjectLabelDomainService = subjectLabelDomainService;
    }

    @PostMapping("/add")
    public Result<Void> addLabel(@RequestBody SubjectLabelDTO dto) {
        SubjectLabel label = new SubjectLabel();
        label.setLabelName(dto.getLabelName());
        label.setCategoryId(dto.getCategoryId());
        label.setSortNum(dto.getSortNum());
        subjectLabelDomainService.addLabel(label);
        return Result.success();
    }

    @PostMapping("/update")
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
    public Result<Void> deleteLabel(@RequestBody SubjectLabelDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "标签ID不能为空");
        }
        subjectLabelDomainService.deleteLabel(dto.getId());
        return Result.success();
    }

    @GetMapping("/queryByCategoryId")
    public Result<List<SubjectLabelDTO>> queryByCategoryId(@RequestParam Long categoryId) {
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
