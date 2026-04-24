package com.colacode.practice.domain.service;

import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.practice.config.JudgeProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JudgeSecurityService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JudgeProperties judgeProperties;

    public JudgeSecurityService(StringRedisTemplate stringRedisTemplate, JudgeProperties judgeProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.judgeProperties = judgeProperties;
    }

    public void assertSubmissionAllowed(Long userId, Long subjectId, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "代码不能为空");
        }
        if (code.length() > judgeProperties.getMaxCodeLength()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "代码长度超出限制");
        }

        String minuteKey = "judge:submit:minute:" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(minuteKey);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(minuteKey, 60, TimeUnit.SECONDS);
        }
        if (count != null && count > judgeProperties.getMaxSubmitPerMinute()) {
            throw new BusinessException(ResultCodeEnum.TOO_MANY_REQUESTS, "提交过于频繁，请稍后再试");
        }

        String cooldownKey = "judge:submit:cooldown:" + userId + ":" + subjectId;
        Boolean existed = stringRedisTemplate.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(existed)) {
            throw new BusinessException(ResultCodeEnum.TOO_MANY_REQUESTS, "同一题目提交过于频繁，请稍后再试");
        }
        stringRedisTemplate.opsForValue().set(
                cooldownKey,
                "1",
                judgeProperties.getSubmitCooldownSeconds(),
                TimeUnit.SECONDS);
    }
}
