package com.colacode.interview.domain.state;

import com.colacode.interview.domain.enums.InterviewSessionActionEnum;
import com.colacode.interview.infra.entity.InterviewSession;

public interface InterviewStateMachine {
    void transit(InterviewSession session, InterviewSessionActionEnum action);
}