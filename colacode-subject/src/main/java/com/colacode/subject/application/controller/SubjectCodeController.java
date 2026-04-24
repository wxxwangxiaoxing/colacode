package com.colacode.subject.application.controller;

import com.colacode.common.Result;
import com.colacode.subject.application.dto.SubjectCodeJudgeDetailDTO;
import com.colacode.subject.domain.service.SubjectCodeDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subject/code")
@Tag(name = "编程题管理", description = "编程题判题相关接口")
public class SubjectCodeController {

    private final SubjectCodeDomainService subjectCodeDomainService;

    public SubjectCodeController(SubjectCodeDomainService subjectCodeDomainService) {
        this.subjectCodeDomainService = subjectCodeDomainService;
    }

    @GetMapping("/judgeDetail")
    @Operation(summary = "查询编程题判题详情", description = "供判题服务查询完整测试用例与判题配置")
    public Result<SubjectCodeJudgeDetailDTO> queryJudgeDetail(
            @Parameter(description = "题目ID") @RequestParam("id") Long id) {
        return Result.success(subjectCodeDomainService.queryJudgeDetail(id));
    }
}
