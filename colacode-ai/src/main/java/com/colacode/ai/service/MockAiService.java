package com.colacode.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@Primary
public class MockAiService implements AiService {

    private final Random random = new Random();

    @Override
    public String generateQuestion(String keyword) {
        log.info("Mock 生成面试题, keyword: {}", keyword);
        return "【模拟面试题】请详细说明 " + keyword + " 的工作原理、优缺点以及在实际项目中的应用场景？";
    }

    @Override
    public double scoreAnswer(String question, String userAnswer) {
        log.info("Mock 评分, question: {}, answer: {}", question, userAnswer);
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 1.0;
        }
        int length = userAnswer.length();
        if (length < 20) {
            return 1.5;
        } else if (length < 50) {
            return 2.5;
        } else if (length < 100) {
            return 3.5;
        } else if (length < 200) {
            return 4.0;
        } else {
            return 4.5 + random.nextDouble() * 0.5;
        }
    }

    @Override
    public String getModelName() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}