package com.colacode.subject.application.controller;

import com.colacode.common.LoginUserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.domain.service.SubjectWrongDomainService;
import com.colacode.subject.infra.entity.SubjectWrong;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/wrong")
@Tag(name = "错题管理", description = "用户错题记录管理")
public class SubjectWrongController {

    private final SubjectWrongDomainService subjectWrongDomainService;

    public SubjectWrongController(SubjectWrongDomainService subjectWrongDomainService) {
        this.subjectWrongDomainService = subjectWrongDomainService;
    }

    @PostMapping("/record")
    @Operation(summary = "记录错题", description = "记录用户做错的题目")
    public Result<Void> recordWrong(@Parameter(description = "题目ID") @RequestParam Long subjectId, @Parameter(description = "错误答案") @RequestParam(required = false) String wrongAnswer) {
        Long userId = LoginUserContext.requireLoginUserId();
        subjectWrongDomainService.recordWrong(subjectId, userId, wrongAnswer);
        return Result.success();
    }

    @PostMapping("/remove")
    @Operation(summary = "移除错题", description = "从错题记录中移除")
    public Result<Void> removeWrong(@Parameter(description = "题目ID") @RequestParam Long subjectId) {
        Long userId = LoginUserContext.requireLoginUserId();
        subjectWrongDomainService.removeWrong(subjectId, userId);
        return Result.success();
    }

    @GetMapping("/myPage")
    @Operation(summary = "分页获取错题", description = "分页获取当前用户的错题列表")
    public Result<PageResult<SubjectWrong>> getMyWrongPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.requireLoginUserId();
        Page<SubjectWrong> page = subjectWrongDomainService.getWrongPage(userId, pageNo, pageSize);
        List<SubjectWrong> list = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, list));
    }

    @GetMapping("/myList")
    @Operation(summary = "获取错题列表", description = "获取当前用户的所有错题")
    public Result<List<SubjectWrong>> getMyWrongList() {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(subjectWrongDomainService.getWrongList(userId));
    }

    @PostMapping("/clear")
    @Operation(summary = "清空错题", description = "清空当前用户所有错题记录")
    public Result<Void> clearWrong() {
        Long userId = LoginUserContext.requireLoginUserId();
        subjectWrongDomainService.clearWrong(userId);
        return Result.success();
    }
}
