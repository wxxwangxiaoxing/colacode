package com.colacode.interview.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.interview.domain.enums.InterviewQuestionRecordStatusEnum;
import com.colacode.interview.domain.enums.InterviewSessionActionEnum;
import com.colacode.interview.domain.enums.InterviewSessionStatusEnum;
import com.colacode.interview.domain.state.InterviewStateMachine;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewSession;
import com.colacode.interview.infra.mapper.InterviewQuestionRecordMapper;
import com.colacode.interview.infra.mapper.InterviewSessionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class InterviewSessionDomainService {

    private final InterviewSessionMapper interviewSessionMapper;
    private final InterviewQuestionRecordMapper interviewQuestionRecordMapper;
    private final InterviewStateMachine interviewStateMachine;

    public InterviewSessionDomainService(InterviewSessionMapper interviewSessionMapper,
                                         InterviewQuestionRecordMapper interviewQuestionRecordMapper,
                                         InterviewStateMachine interviewStateMachine) {
        this.interviewSessionMapper = interviewSessionMapper;
        this.interviewQuestionRecordMapper = interviewQuestionRecordMapper;
        this.interviewStateMachine = interviewStateMachine;
    }

    public InterviewSession createSession(InterviewSession session) {
        session.setStatus(InterviewSessionStatusEnum.INIT.name());
        session.setCurrentQuestionNo(0);
        session.setVersion(0);
        session.setTotalScore(BigDecimal.ZERO);
        session.setDurationSeconds(0);
        session.setStartTime(new Date());
        interviewSessionMapper.insert(session);
        interviewStateMachine.transit(session, InterviewSessionActionEnum.START);
        interviewSessionMapper.updateById(session);
        return session;
    }

    public void saveQuestionRecords(Long sessionId, List<InterviewQuestionRecord> records) {
        for (int index = 0; index < records.size(); index++) {
            InterviewQuestionRecord record = records.get(index);
            record.setSessionId(sessionId);
            record.setRoundNo(1);
            record.setIsFollowUp(0);
            record.setStatus(index == 0
                    ? InterviewQuestionRecordStatusEnum.WAITING_ANSWER.name()
                    : InterviewQuestionRecordStatusEnum.PENDING.name());
            if (index == 0) {
                record.setAskTime(new Date());
            }
            interviewQuestionRecordMapper.insert(record);
        }
    }

    public InterviewSession getSession(Long sessionId) {
        InterviewSession session = interviewSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "面试会话不存在");
        }
        return session;
    }

    public InterviewQuestionRecord getQuestionRecord(Long recordId) {
        InterviewQuestionRecord record = interviewQuestionRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "面试题记录不存在");
        }
        return record;
    }

    public List<InterviewQuestionRecord> listQuestionRecords(Long sessionId) {
        LambdaQueryWrapper<InterviewQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestionRecord::getSessionId, sessionId);
        wrapper.orderByAsc(InterviewQuestionRecord::getId);
        return interviewQuestionRecordMapper.selectList(wrapper);
    }

    public InterviewQuestionRecord findCurrentWaitingQuestion(Long sessionId) {
        LambdaQueryWrapper<InterviewQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestionRecord::getSessionId, sessionId)
                .eq(InterviewQuestionRecord::getStatus, InterviewQuestionRecordStatusEnum.WAITING_ANSWER.name())
                .orderByAsc(InterviewQuestionRecord::getId)
                .last("limit 1");
        return interviewQuestionRecordMapper.selectOne(wrapper);
    }

    public InterviewQuestionRecord findNextPendingQuestion(Long sessionId) {
        LambdaQueryWrapper<InterviewQuestionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestionRecord::getSessionId, sessionId)
                .eq(InterviewQuestionRecord::getStatus, InterviewQuestionRecordStatusEnum.PENDING.name())
                .orderByAsc(InterviewQuestionRecord::getId)
                .last("limit 1");
        return interviewQuestionRecordMapper.selectOne(wrapper);
    }

    public void markWaitingForFirstQuestion(InterviewSession session) {
        session.setCurrentQuestionNo(1);
        session.setStatus(InterviewSessionStatusEnum.WAITING_ANSWER.name());
        interviewSessionMapper.updateById(session);
    }

    public InterviewQuestionRecord activateNextQuestion(InterviewSession session) {
        InterviewQuestionRecord current = findCurrentWaitingQuestion(session.getId());
        if (current != null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "当前题目尚未作答，不能直接进入下一题");
        }
        InterviewQuestionRecord next = findNextPendingQuestion(session.getId());
        if (next == null) {
            interviewStateMachine.transit(session, InterviewSessionActionEnum.FINISH);
            session.setEndTime(new Date());
            session.setDurationSeconds(calcDurationSeconds(session));
            interviewSessionMapper.updateById(session);
            return null;
        }
        next.setStatus(InterviewQuestionRecordStatusEnum.WAITING_ANSWER.name());
        next.setAskTime(new Date());
        interviewQuestionRecordMapper.updateById(next);
        session.setCurrentQuestionNo(session.getCurrentQuestionNo() == null ? 1 : session.getCurrentQuestionNo() + 1);
        interviewStateMachine.transit(session, InterviewSessionActionEnum.NEXT_QUESTION);
        interviewSessionMapper.updateById(session);
        return next;
    }

    public void markEvaluating(InterviewSession session) {
        interviewStateMachine.transit(session, InterviewSessionActionEnum.SUBMIT_ANSWER);
        interviewSessionMapper.updateById(session);
    }

    public void interruptSession(InterviewSession session) {
        interviewStateMachine.transit(session, InterviewSessionActionEnum.INTERRUPT);
        interviewSessionMapper.updateById(session);
    }

    public InterviewQuestionRecord resumeSession(InterviewSession session) {
        interviewStateMachine.transit(session, InterviewSessionActionEnum.RESUME);
        InterviewQuestionRecord current = findCurrentWaitingQuestion(session.getId());
        if (current == null) {
            current = findNextPendingQuestion(session.getId());
            if (current != null) {
                current.setStatus(InterviewQuestionRecordStatusEnum.WAITING_ANSWER.name());
                if (current.getAskTime() == null) {
                    current.setAskTime(new Date());
                }
                interviewQuestionRecordMapper.updateById(current);
            }
        }
        interviewSessionMapper.updateById(session);
        return current;
    }

    public void saveEvaluatedQuestion(InterviewQuestionRecord questionRecord) {
        questionRecord.setStatus(InterviewQuestionRecordStatusEnum.EVALUATED.name());
        interviewQuestionRecordMapper.updateById(questionRecord);
    }

    public void refreshSessionScore(InterviewSession session, List<InterviewQuestionRecord> allRecords) {
        BigDecimal total = allRecords.stream()
                .map(InterviewQuestionRecord::getFinalScore)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = (int) allRecords.stream().filter(item -> item.getFinalScore() != null).count();
        if (count > 0) {
            session.setTotalScore(total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP));
        }
        interviewSessionMapper.updateById(session);
    }

    public void validateQuestionBelongsToSession(InterviewSession session, InterviewQuestionRecord record) {
        if (!session.getId().equals(record.getSessionId())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "题目记录与会话不匹配");
        }
    }

    public void validateCanSubmitAnswer(InterviewSession session, InterviewQuestionRecord record) {
        validateQuestionBelongsToSession(session, record);
        if (!InterviewSessionStatusEnum.WAITING_ANSWER.name().equals(session.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "当前会话状态不允许提交答案");
        }
        if (!InterviewQuestionRecordStatusEnum.WAITING_ANSWER.name().equals(record.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "当前题目不处于待作答状态");
        }
    }

    private int calcDurationSeconds(InterviewSession session) {
        if (session.getStartTime() == null || session.getEndTime() == null) {
            return 0;
        }
        return (int) ((session.getEndTime().getTime() - session.getStartTime().getTime()) / 1000);
    }
}
