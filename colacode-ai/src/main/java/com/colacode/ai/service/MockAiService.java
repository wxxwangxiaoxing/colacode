package com.colacode.ai.service;

import com.colacode.ai.service.dto.JudgeAnalysisContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Random;

/**
 * Mock AI服务实现
 * 用于测试和开发环境，不调用真实的AI模型
 *
 * @author wxx
 */
@Slf4j
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
    public String analyzeJudgeSubmission(JudgeAnalysisContext context) {
        log.info("Mock 分析判题结果, subject: {}, status: {}",
                context.getSubjectName(), context.getStatus());
        String status = context.getStatus() == null ? "UNKNOWN" : context.getStatus().trim().toUpperCase(Locale.ROOT);
        return switch (status) {
            case "AC", "ACCEPTED" ->
                    "本次提交已通过全部测试。可继续检查代码可读性、边界条件说明和复杂度优化。";
            case "WA", "WRONG_ANSWER" ->
                    "结果与预期输出不一致。优先核对输入解析、输出格式以及边界值处理。";
            case "CE", "COMPILE_ERROR" ->
                    "代码未通过编译。先根据编译错误定位语法、类型或缺失导入问题。";
            case "RE", "RUNTIME_ERROR" ->
                    "程序运行时异常。重点检查空指针、数组越界、除零和未处理输入。";
            case "TLE", "TIME_LIMIT", "TIME_LIMIT_EXCEEDED" ->
                    "程序超时。需要检查算法复杂度、循环边界和重复计算。";
            case "PENDING", "RUNNING" ->
                    "提交仍在判题中，请稍后刷新结果。";
            case "SYSTEM_ERROR", "SE" ->
                    "判题过程中出现系统错误。先确认代码和输入正常，再检查判题服务状态。";
            default -> "请结合判题状态、失败用例和错误输出继续排查，优先复现失败场景。";
        };
    }

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
