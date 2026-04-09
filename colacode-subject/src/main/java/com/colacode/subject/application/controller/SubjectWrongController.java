package com.colacode.subject.application.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.domain.service.SubjectWrongDomainService;
import com.colacode.subject.infra.entity.SubjectWrong;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/wrong")
public class SubjectWrongController {

    private final SubjectWrongDomainService subjectWrongDomainService;

    public SubjectWrongController(SubjectWrongDomainService subjectWrongDomainService) {
        this.subjectWrongDomainService = subjectWrongDomainService;
    }

    @PostMapping("/record")
    public Result<Void> recordWrong(@RequestParam Long subjectId, @RequestParam(required = false) String wrongAnswer) {
        Long userId = StpUtil.getLoginIdAsLong();
        subjectWrongDomainService.recordWrong(subjectId, userId, wrongAnswer);
        return Result.success();
    }

    @PostMapping("/remove")
    public Result<Void> removeWrong(@RequestParam Long subjectId) {
        Long userId = StpUtil.getLoginIdAsLong();
        subjectWrongDomainService.removeWrong(subjectId, userId);
        return Result.success();
    }

    @GetMapping("/myPage")
    public Result<PageResult<SubjectWrong>> getMyWrongPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<SubjectWrong> page = subjectWrongDomainService.getWrongPage(userId, pageNo, pageSize);
        List<SubjectWrong> list = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, list));
    }

    @GetMapping("/myList")
    public Result<List<SubjectWrong>> getMyWrongList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(subjectWrongDomainService.getWrongList(userId));
    }

    @PostMapping("/clear")
    public Result<Void> clearWrong() {
        Long userId = StpUtil.getLoginIdAsLong();
        subjectWrongDomainService.clearWrong(userId);
        return Result.success();
    }
}
