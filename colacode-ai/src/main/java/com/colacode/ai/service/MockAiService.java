package com.colacode.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Mock AI服务实现
 * 用于测试和开发环境，不调用真实的AI模型
 *
 * @author wxx
 */
@Slf4j
@Service
@Primary
public class MockAiService implements AiService {

    /**
     * 随机数生成器
     */
    private final Random random = new Random();

    /**
     * 生成Mock面试题
     *
     * @param keyword 关键词
     * @return 格式化的面试题
     */
    @Override
    public String generateQuestion(String keyword) {
        log.info("Mock 生成面试题, keyword: {}", keyword);
        return "【模拟面试题】请详细说明 " + keyword + " 的工作原理、优缺点以及在实际项目中的应用场景？";
    }

    /**
     * Mock评分功能
     * 根据答案长度返回模拟评分
     *
     * @param question   面试题
     * @param userAnswer 用户答案
     * @return 模拟评分（1.0-5.0）
     */
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

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    @Override
    public String getModelName() {
        return "MOCK";
    }

    /**
     * 检查服务可用性
     *
     * @return 始终返回true
     */
    @Override
    public boolean isAvailable() {
        return true;
    }
}