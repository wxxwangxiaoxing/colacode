package com.colacode.subject.application.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.application.converter.SubjectInfoDTOConverter;
import com.colacode.subject.application.dto.SubjectInfoDTO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.service.SubjectDomainService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/info")
public class SubjectInfoController {

    private final SubjectDomainService subjectDomainService;

    public SubjectInfoController(SubjectDomainService subjectDomainService) {
        this.subjectDomainService = subjectDomainService;
    }

    @PostMapping("/add")
    public Result<Void> addSubject(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE.convertToBO(subjectInfoDTO);
        subjectDomainService.addSubject(subjectInfoBO);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateSubject(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE.convertToBO(subjectInfoDTO);
        subjectDomainService.updateSubject(subjectInfoBO);
        return Result.success();
    }

    @GetMapping("/query")
    public Result<SubjectInfoDTO> querySubject(@RequestParam Long id) {
        SubjectInfoBO subjectInfoBO = subjectDomainService.querySubject(id);
        return Result.success(SubjectInfoDTOConverter.INSTANCE.convertToDTO(subjectInfoBO));
    }

    @PostMapping("/delete")
    public Result<Void> deleteSubject(@RequestParam Long id) {
        subjectDomainService.deleteSubject(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<SubjectInfoDTO>> getSubjectPage(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long labelId,
            @RequestParam(required = false) Integer subjectType,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<SubjectInfoBO> page = subjectDomainService.getSubjectPage(categoryId, labelId, subjectType, pageNo, pageSize);
        List<SubjectInfoDTO> dtoList = SubjectInfoDTOConverter.INSTANCE.convertToDTOList(page.getRecords());
        return Result.success(PageUtil.toPageResult(page, dtoList));
    }

    @GetMapping("/contribute")
    public Result<List<SubjectInfoDTO>> getContributeList(
            @RequestParam(defaultValue = "5") int limit) {
        List<SubjectInfoBO> boList = subjectDomainService.getContributeList(limit);
        List<SubjectInfoDTO> dtoList = SubjectInfoDTOConverter.INSTANCE.convertToDTOList(boList);
        return Result.success(dtoList);
    }

    @PostMapping("/batchQuery")
    public Result<List<SubjectInfoDTO>> batchQuerySubjects(@RequestBody List<Long> ids) {
        List<SubjectInfoDTO> dtoList = new java.util.ArrayList<>();
        for (Long id : ids) {
            SubjectInfoBO bo = subjectDomainService.querySubject(id);
            if (bo != null) {
                dtoList.add(SubjectInfoDTOConverter.INSTANCE.convertToDTO(bo));
            }
        }
        return Result.success(dtoList);
    }
}
