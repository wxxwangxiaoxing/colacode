package com.colacode.practice.application.feign;

import com.colacode.common.Result;
import com.colacode.practice.application.feign.dto.SubjectCodeJudgeDetailDTO;
import com.colacode.practice.application.feign.dto.SubjectInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "colacode-subject", url = "${feign.subject.url:}")
public interface SubjectFeignClient {

    @GetMapping("/subject/info/query")
    Result<SubjectInfoDTO> querySubject(@RequestParam("id") Long id);

    @GetMapping("/subject/code/judgeDetail")
    Result<SubjectCodeJudgeDetailDTO> queryJudgeDetail(@RequestParam("id") Long id);

    @PostMapping("/subject/info/batchQuery")
    Result<List<SubjectInfoDTO>> batchQuerySubjects(@RequestBody List<Long> ids);

    @GetMapping("/auth/user/info")
    Result<Map<String, Object>> getUserInfo();
}
