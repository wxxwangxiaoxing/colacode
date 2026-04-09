package com.colacode.interview.domain.state;

import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.interview.domain.enums.InterviewSessionActionEnum;
import com.colacode.interview.domain.enums.InterviewSessionStatusEnum;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;

@Component
public class InterviewStateValidator {

    private static final Map<InterviewSessionActionEnum, EnumSet<InterviewSessionStatusEnum>> RULES = Map.of(
            InterviewSessionActionEnum.START, EnumSet.of(InterviewSessionStatusEnum.INIT),
            InterviewSessionActionEnum.SUBMIT_ANSWER, EnumSet.of(InterviewSessionStatusEnum.WAITING_ANSWER),
            InterviewSessionActionEnum.NEXT_QUESTION, EnumSet.of(InterviewSessionStatusEnum.EVALUATING, InterviewSessionStatusEnum.IN_PROGRESS),
            InterviewSessionActionEnum.FINISH, EnumSet.of(InterviewSessionStatusEnum.EVALUATING, InterviewSessionStatusEnum.IN_PROGRESS, InterviewSessionStatusEnum.WAITING_ANSWER),
            InterviewSessionActionEnum.INTERRUPT, EnumSet.of(InterviewSessionStatusEnum.IN_PROGRESS, InterviewSessionStatusEnum.WAITING_ANSWER),
            InterviewSessionActionEnum.RESUME, EnumSet.of(InterviewSessionStatusEnum.INTERRUPTED)
    );

    public void validate(InterviewSessionStatusEnum currentStatus, InterviewSessionActionEnum action) {
        EnumSet<InterviewSessionStatusEnum> allowed = RULES.get(action);
        if (allowed == null || !allowed.contains(currentStatus)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST,
                    "当前状态不允许执行该操作: status=" + currentStatus + ", action=" + action);
        }
    }
}