package com.colacode.interview.domain.engine;

import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;

import java.util.List;

public interface InterviewEngine {

    String engineType();

    List<KeywordBO> analyse(List<String> labels);

    List<InterviewQuestionBO> start(List<KeywordBO> selectedKeywords);

    InterviewResultBO submit(List<InterviewQuestionBO> questions);
}
