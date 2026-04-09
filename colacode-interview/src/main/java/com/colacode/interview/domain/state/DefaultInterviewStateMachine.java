package com.colacode.interview.domain.state;

import com.colacode.interview.domain.enums.InterviewSessionActionEnum;
import com.colacode.interview.domain.enums.InterviewSessionStatusEnum;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.stereotype.Component;

@Component
public class DefaultInterviewStateMachine implements InterviewStateMachine {

    private final InterviewStateValidator validator;

    public DefaultInterviewStateMachine(InterviewStateValidator validator) {
        this.validator = validator;
    }

    @Override
    public void transit(InterviewSession session, InterviewSessionActionEnum action) {
        InterviewSessionStatusEnum current = InterviewSessionStatusEnum.valueOf(session.getStatus());
        validator.validate(current, action);
        switch (action) {
            case START -> session.setStatus(InterviewSessionStatusEnum.IN_PROGRESS.name());
            case SUBMIT_ANSWER -> session.setStatus(InterviewSessionStatusEnum.EVALUATING.name());
            case NEXT_QUESTION -> session.setStatus(InterviewSessionStatusEnum.WAITING_ANSWER.name());
            case FINISH -> session.setStatus(InterviewSessionStatusEnum.FINISHED.name());
            case INTERRUPT -> session.setStatus(InterviewSessionStatusEnum.INTERRUPTED.name());
            case RESUME -> session.setStatus(InterviewSessionStatusEnum.WAITING_ANSWER.name());
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }
}