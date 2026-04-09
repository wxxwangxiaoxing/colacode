package com.colacode.ai.controller;

import com.colacode.ai.controller.dto.AiGenerateQuestionReqDTO;
import com.colacode.ai.controller.dto.AiGenerateQuestionRespDTO;
import com.colacode.ai.controller.dto.AiScoreAnswerReqDTO;
import com.colacode.ai.controller.dto.AiScoreAnswerRespDTO;
import com.colacode.ai.service.AiService;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/interview")
public class AiInterviewController {

    private final AiService aiService;

    public AiInterviewController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/question")
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

    @PostMapping("/score")
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
