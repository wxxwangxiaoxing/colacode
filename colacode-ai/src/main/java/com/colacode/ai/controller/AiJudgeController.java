package com.colacode.ai.controller;

import com.colacode.ai.controller.dto.AiJudgeAnalysisReqDTO;
import com.colacode.ai.controller.dto.AiJudgeAnalysisRespDTO;
import com.colacode.ai.service.AiService;
import com.colacode.ai.service.dto.JudgeAnalysisContext;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/judge")
@Tag(name = "AI 判题分析", description = "编程题判题结果的 AI 解释与修复建议")
public class AiJudgeController {

    private final AiService aiService;

    public AiJudgeController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyse")
    @Operation(summary = "分析判题结果", description = "根据提交代码、判题状态和失败摘要生成修复建议")
    public Result<AiJudgeAnalysisRespDTO> analyse(@Valid @RequestBody AiJudgeAnalysisReqDTO reqDTO) {
        try {
            JudgeAnalysisContext context = new JudgeAnalysisContext();
            context.setSubjectName(reqDTO.getSubjectName());
            context.setLanguage(reqDTO.getLanguage());
            context.setCode(reqDTO.getCode());
            context.setStatus(reqDTO.getStatus());
            context.setJudgeMessage(reqDTO.getJudgeMessage());
            context.setFailedCaseSummary(reqDTO.getFailedCaseSummary());
            context.setStdoutPreview(reqDTO.getStdoutPreview());
            context.setStderrPreview(reqDTO.getStderrPreview());
            context.setInputExample(reqDTO.getInputExample());
            context.setOutputExample(reqDTO.getOutputExample());

            AiJudgeAnalysisRespDTO respDTO = new AiJudgeAnalysisRespDTO();
            respDTO.setFeedback(aiService.analyzeJudgeSubmission(context));
            return Result.success(respDTO);
        } catch (Exception e) {
            log.error("AI judge analysis failed, status={}", reqDTO.getStatus(), e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "判题结果 AI 分析失败");
        }
    }
}
