package com.colacode.practice.application.controller;

import com.colacode.common.LoginUserContext;
import com.colacode.common.Result;
import com.colacode.practice.application.dto.judge.JudgeRunSampleDTO;
import com.colacode.practice.application.dto.judge.JudgeRunSampleResultDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmissionDetailDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmissionSummaryDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmitDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmitRespDTO;
import com.colacode.practice.domain.bo.JudgeSubmitBO;
import com.colacode.practice.domain.service.JudgeSampleRunService;
import com.colacode.practice.domain.service.JudgeSubmissionDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/practice/judge")
@Tag(name = "在线判题", description = "编程题提交与判题接口")
public class JudgeController {

    private final JudgeSubmissionDomainService judgeSubmissionDomainService;
    private final JudgeSampleRunService judgeSampleRunService;

    public JudgeController(JudgeSubmissionDomainService judgeSubmissionDomainService,
                           JudgeSampleRunService judgeSampleRunService) {
        this.judgeSubmissionDomainService = judgeSubmissionDomainService;
        this.judgeSampleRunService = judgeSampleRunService;
    }

    @PostMapping("/submit")
    @Operation(summary = "提交代码", description = "创建编程题提交并异步发起判题")
    public Result<JudgeSubmitRespDTO> submit(@Valid @RequestBody JudgeSubmitDTO submitDTO) {
        Long userId = LoginUserContext.requireLoginUserId();
        JudgeSubmitBO submitBO = new JudgeSubmitBO();
        submitBO.setSubjectId(submitDTO.getSubjectId());
        submitBO.setLanguage(submitDTO.getLanguage());
        submitBO.setCode(submitDTO.getCode());
        return Result.success(judgeSubmissionDomainService.submit(userId, submitBO));
    }

    @PostMapping("/run")
    @Operation(summary = "运行样例", description = "同步运行编程题样例测试用例，不保存提交记录")
    public Result<JudgeRunSampleResultDTO> runSample(@Valid @RequestBody JudgeRunSampleDTO runDTO) {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(judgeSampleRunService.runSample(
                userId, runDTO.getSubjectId(), runDTO.getLanguage(), runDTO.getCode()));
    }

    @GetMapping("/submission/detail")
    @Operation(summary = "查询提交详情", description = "查询单次判题提交的详细结果")
    public Result<JudgeSubmissionDetailDTO> detail(@Parameter(description = "提交ID") @RequestParam("id") Long id) {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(judgeSubmissionDomainService.getDetail(userId, id));
    }

    @GetMapping("/submission/list")
    @Operation(summary = "查询我的提交列表", description = "分页查询当前用户的编程题提交记录，支持按执行时间或内存排序")
    public Result<List<JudgeSubmissionSummaryDTO>> list(
            @Parameter(description = "题目ID") @RequestParam(value = "subjectId", required = false) Long subjectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "排序字段：executeTime/memory，默认按ID") @RequestParam(value = "sortField", required = false) String sortField,
            @Parameter(description = "排序方向：asc/desc，默认desc") @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(judgeSubmissionDomainService.list(userId, subjectId, pageNo, pageSize, sortField, sortOrder));
    }
}
