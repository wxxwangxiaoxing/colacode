package com.colacode.subject.application.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.application.converter.SubjectInfoDTOConverter;
import com.colacode.subject.application.dto.SubjectInfoDTO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.service.SubjectDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/info")
@Tag(name = "题目管理", description = "题目信息的增删改查")
public class SubjectInfoController {

    private final SubjectDomainService subjectDomainService;

    public SubjectInfoController(SubjectDomainService subjectDomainService) {
        this.subjectDomainService = subjectDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "新增题目", description = "添加新题目")
    public Result<Void> addSubject(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE.convertToBO(subjectInfoDTO);
        subjectDomainService.addSubject(subjectInfoBO);
        return Result.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新题目", description = "更新题目信息")
    public Result<Void> updateSubject(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE.convertToBO(subjectInfoDTO);
        subjectDomainService.updateSubject(subjectInfoBO);
        return Result.success();
    }

    @GetMapping("/query")
    @Operation(summary = "查询题目", description = "根据ID查询题目详情")
    public Result<SubjectInfoDTO> querySubject(@Parameter(description = "题目ID") @RequestParam Long id) {
        SubjectInfoBO subjectInfoBO = subjectDomainService.querySubject(id);
        return Result.success(SubjectInfoDTOConverter.INSTANCE.convertToDTO(subjectInfoBO));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除题目", description = "根据ID删除题目")
    public Result<Void> deleteSubject(@Parameter(description = "题目ID") @RequestParam Long id) {
        subjectDomainService.deleteSubject(id);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询题目", description = "分页查询题目列表")
    public Result<PageResult<SubjectInfoDTO>> getSubjectPage(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long labelId,
            @Parameter(description = "题目类型") @RequestParam(required = false) Integer subjectType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Page<SubjectInfoBO> page = subjectDomainService.getSubjectPage(categoryId, labelId, subjectType, pageNo, pageSize);
        List<SubjectInfoDTO> dtoList = SubjectInfoDTOConverter.INSTANCE.convertToDTOList(page.getRecords());
        return Result.success(PageUtil.toPageResult(page, dtoList));
    }

    @GetMapping("/contribute")
    @Operation(summary = "获取贡献榜", description = "获取贡献最多的题目列表")
    public Result<List<SubjectInfoDTO>> getContributeList(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "5") int limit) {
        List<SubjectInfoBO> boList = subjectDomainService.getContributeList(limit);
        List<SubjectInfoDTO> dtoList = SubjectInfoDTOConverter.INSTANCE.convertToDTOList(boList);
        return Result.success(dtoList);
    }

    @PostMapping("/batchQuery")
    @Operation(summary = "批量查询题目", description = "根据ID列表批量查询题目")
    public Result<List<SubjectInfoDTO>> batchQuerySubjects(@Parameter(description = "题目ID列表") @RequestBody List<Long> ids) {
        List<SubjectInfoDTO> dtoList = new java.util.ArrayList<>();
        for (Long id : ids) {
            SubjectInfoBO bo = subjectDomainService.querySubjectWithoutBrowseIncrement(id);
            if (bo != null) {
                dtoList.add(SubjectInfoDTOConverter.INSTANCE.convertToDTO(bo));
            }
        }
        return Result.success(dtoList);
    }
}
