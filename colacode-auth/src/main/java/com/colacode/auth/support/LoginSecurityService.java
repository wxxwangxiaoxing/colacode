package com.colacode.auth.support;

import com.colacode.auth.config.LoginSecurityProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginSecurityService {

    private static final String LOGIN_ATTEMPT_KEY = "auth:login:attempt:";
    private static final String LOGIN_FAILURE_KEY = "auth:login:failure:";
    private static final String LOGIN_LOCK_KEY = "auth:login:lock:";

    private final StringRedisTemplate stringRedisTemplate;
    private final LoginSecurityProperties properties;

    public LoginSecurityService(StringRedisTemplate stringRedisTemplate,
                                LoginSecurityProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
    }

    public String checkLoginRisk(String userName) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey(userName)))) {
            return "登录失败次数过多，请稍后再试";
        }

        Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey(userName));
        if (attempts != null && attempts == 1L) {
            stringRedisTemplate.expire(attemptKey(userName), properties.getAttemptWindowSeconds(), TimeUnit.SECONDS);
        }
        if (attempts != null && attempts > properties.getMaxAttemptsPerMinute()) {
            return "登录过于频繁，请稍后再试";
        }
        return null;
    }

    public void recordLoginFailure(String userName) {
        Long failures = stringRedisTemplate.opsForValue().increment(failureKey(userName));
        if (failures != null && failures == 1L) {
            stringRedisTemplate.expire(failureKey(userName), properties.getFailureWindowSeconds(), TimeUnit.SECONDS);
        }
        if (failures != null && failures >= properties.getMaxFailures()) {
            stringRedisTemplate.opsForValue().set(lockKey(userName), "1", properties.getLockSeconds(), TimeUnit.SECONDS);
        }
    }

    public void recordLoginSuccess(String userName) {
        stringRedisTemplate.delete(failureKey(userName));
        stringRedisTemplate.delete(lockKey(userName));
    }

    private String attemptKey(String userName) {
        return LOGIN_ATTEMPT_KEY + userName;
    }

    private String failureKey(String userName) {
        return LOGIN_FAILURE_KEY + userName;
    }

    private String lockKey(String userName) {
        return LOGIN_LOCK_KEY + userName;
    }
}
