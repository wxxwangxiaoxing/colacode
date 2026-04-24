package com.colacode.practice.application.feign;

import com.colacode.common.Result;
import com.colacode.practice.application.feign.dto.SubjectCodeJudgeDetailDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class CachedSubjectFeignClient {

    private final SubjectFeignClient subjectFeignClient;

    public CachedSubjectFeignClient(SubjectFeignClient subjectFeignClient) {
        this.subjectFeignClient = subjectFeignClient;
    }

    @Cacheable(value = "judgeDetail", key = "#subjectId", unless = "#result == null or !#result.isSuccess()")
    public Result<SubjectCodeJudgeDetailDTO> queryJudgeDetail(Long subjectId) {
        return subjectFeignClient.queryJudgeDetail(subjectId);
    }
}
