package com.colacode.interview.application.feign;

import com.colacode.common.Result;
import com.colacode.interview.application.feign.dto.AiGenerateQuestionReqDTO;
import com.colacode.interview.application.feign.dto.AiGenerateQuestionRespDTO;
import com.colacode.interview.application.feign.dto.AiScoreAnswerReqDTO;
import com.colacode.interview.application.feign.dto.AiScoreAnswerRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "${interview.ai.service-name:colacode-ai}",
        url = "${interview.ai.base-url:}",
        path = "/ai/interview"
)
public interface AiGatewayFeignClient {

    @PostMapping("/question")
    Result<AiGenerateQuestionRespDTO> generateQuestion(@RequestBody AiGenerateQuestionReqDTO reqDTO);

    @PostMapping("/score")
    Result<AiScoreAnswerRespDTO> scoreAnswer(@RequestBody AiScoreAnswerReqDTO reqDTO);
}
