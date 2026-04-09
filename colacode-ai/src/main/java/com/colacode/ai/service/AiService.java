package com.colacode.ai.service;

public interface AiService {

    String generateQuestion(String keyword);

    double scoreAnswer(String question, String userAnswer);

    String getModelName();

    boolean isAvailable();
}