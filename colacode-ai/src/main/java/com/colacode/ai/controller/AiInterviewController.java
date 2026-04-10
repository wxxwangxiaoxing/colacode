package com.colacode.ai.controller;

import com.colacode.ai.controller.dto.AiGenerateQuestionReqDTO;
import com.colacode.ai.controller.dto.AiGenerateQuestionRespDTO;
import com.colacode.ai.controller.dto.AiScoreAnswerReqDTO;
import com.colacode.ai.controller.dto.AiScoreAnswerRespDTO;
import com.colacode.ai.service.AiService;
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

/**
 * AI面试控制器
 * 提供面试题生成和答案评分接口
 *
 * @author wxx
 */
@Slf4j
@RestController
@RequestMapping("/ai/interview")
@Tag(name = "AI面试", description = "AI面试问题生成与评分")
public class AiInterviewController {

    /**
     * AI服务
     */
    private final AiService aiService;

    /**
     * 构造函数
     *
     * @param aiService AI服务实例
     */
    public AiInterviewController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 生成面试题
     * 根据关键词调用AI服务生成面试题
     *
     * @param reqDTO 请求参数，包含关键词
     * @return 生成的面试题
     */
    @PostMapping("/question")
    @Operation(summary = "生成面试题", description = "根据关键词生成AI面试题")
    public Result<AiGenerateQuestionRespDTO> generateQuestion(@Valid @RequestBody AiGenerateQuestionReqDTO reqDTO) {
        try {
            String question = aiService.generateQuestion(reqDTO.getKeyword());
            AiGenerateQuestionRespDTO respDTO = new AiGenerateQuestionRespDTO();
            respDTO.setQuestion(question);
            return Result.success(respDTO);
        } catch (Exception e) {
            log.error("生成面试题失败, keyword: {}", reqDTO.getKeyword(), e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "生成面试题失败");
        }
    }

    /**
     * 评分答案
     * 对用户提交的面试答案进行AI评分
     *
     * @param reqDTO 请求参数，包含面试题和用户答案
     * @return 评分结果
     */
    @PostMapping("/score")
    @Operation(summary = "评分答案", description = "对用户面试答案进行评分")
    public Result<AiScoreAnswerRespDTO> scoreAnswer(@Valid @RequestBody AiScoreAnswerReqDTO reqDTO) {
        try {
            double score = aiService.scoreAnswer(reqDTO.getQuestion(), reqDTO.getUserAnswer());
            AiScoreAnswerRespDTO respDTO = new AiScoreAnswerRespDTO();
            respDTO.setScore(score);
            return Result.success(respDTO);
        } catch (Exception e) {
            log.error("面试答案评分失败", e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "面试答案评分失败");
        }
    }
}
