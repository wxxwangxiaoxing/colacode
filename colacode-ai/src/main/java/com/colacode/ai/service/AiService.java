package com.colacode.ai.service;

/**
 * AI服务接口
 * 定义AI能力的基本方法
 *
 * @author wxx
 */
public interface AiService {

    /**
     * 根据关键词生成面试题
     *
     * @param keyword 关键词
     * @return 生成的面试题
     */
    String generateQuestion(String keyword);

    /**
     * 对用户答案进行评分
     *
     * @param question   面试题
     * @param userAnswer 用户答案
     * @return 评分结果
     */
    double scoreAnswer(String question, String userAnswer);

    /**
     * 获取当前使用的模型名称
     *
     * @return 模型名称
     */
    String getModelName();

    /**
     * 检查AI服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}