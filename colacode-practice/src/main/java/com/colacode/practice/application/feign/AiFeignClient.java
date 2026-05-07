package com.colacode.practice.application.feign;

import com.colacode.common.Result;
import com.colacode.practice.application.feign.dto.AiJudgeAnalysisReqDTO;
import com.colacode.practice.application.feign.dto.AiJudgeAnalysisRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "colacode-ai", url = "${feign.ai.url:}", path = "/ai/judge")
public interface AiFeignClient {

    @PostMapping("/analyse")
    Result<AiJudgeAnalysisRespDTO> analyse(@RequestBody AiJudgeAnalysisReqDTO reqDTO);
}
