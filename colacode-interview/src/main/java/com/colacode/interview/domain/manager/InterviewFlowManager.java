package com.colacode.interview.domain.manager;

import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.interview.application.dto.session.InterviewReportQuestionDTO;
import com.colacode.interview.application.dto.session.InterviewSessionKeywordDTO;
import com.colacode.interview.application.dto.session.InterviewSessionQuestionDTO;
import com.colacode.interview.application.dto.session.InterviewSessionReportDTO;
import com.colacode.interview.application.dto.session.InterviewSessionStatusDTO;
import com.colacode.interview.application.dto.session.NextQuestionRespDTO;
import com.colacode.interview.application.dto.session.StartInterviewSessionReqDTO;
import com.colacode.interview.application.dto.session.StartInterviewSessionRespDTO;
import com.colacode.interview.application.dto.session.SubmitAnswerReqDTO;
import com.colacode.interview.application.dto.session.SubmitAnswerRespDTO;
import com.colacode.interview.domain.bo.EvaluateResultBO;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.KeywordBO;
import com.colacode.interview.domain.enums.InterviewSessionStatusEnum;
import com.colacode.interview.domain.service.InterviewDomainService;
import com.colacode.interview.domain.service.InterviewEvaluationDomainService;
import com.colacode.interview.domain.service.InterviewReportDomainService;
import com.colacode.interview.domain.service.InterviewSessionDomainService;
import com.colacode.interview.infra.entity.InterviewQuestionRecord;
import com.colacode.interview.infra.entity.InterviewReport;
import com.colacode.interview.infra.entity.InterviewSession;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InterviewFlowManager {

    private final InterviewDomainService interviewDomainService;
    private final InterviewSessionDomainService interviewSessionDomainService;
    private final InterviewEvaluationDomainService interviewEvaluationDomainService;
    private final InterviewReportDomainService interviewReportDomainService;

    public InterviewFlowManager(InterviewDomainService interviewDomainService,
                                InterviewSessionDomainService interviewSessionDomainService,
                                InterviewEvaluationDomainService interviewEvaluationDomainService,
                                InterviewReportDomainService interviewReportDomainService) {
        this.interviewDomainService = interviewDomainService;
        this.interviewSessionDomainService = interviewSessionDomainService;
        this.interviewEvaluationDomainService = interviewEvaluationDomainService;
        this.interviewReportDomainService = interviewReportDomainService;
    }

    public StartInterviewSessionRespDTO startSession(Long userId, StartInterviewSessionReqDTO reqDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        List<KeywordBO> keywords = toKeywords(reqDTO.getKeywords());
        if (keywords.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "至少选择一个知识点后再开始面试");
        }
        String engineType = StringUtils.hasText(reqDTO.getEngineType()) ? reqDTO.getEngineType() : "DATABASE";
        List<InterviewQuestionBO> generated = interviewDomainService.startInterview(engineType, keywords);
        if (generated.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "未生成任何面试题");
        }
        int questionCount = reqDTO.getQuestionCount() == null || reqDTO.getQuestionCount() <= 0
                ? generated.size()
                : Math.min(reqDTO.getQuestionCount(), generated.size());
        List<InterviewQuestionBO> selected = generated.subList(0, questionCount);

        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setInterviewType(defaultIfBlank(reqDTO.getInterviewType(), "MOCK"));
        session.setPostType(defaultIfBlank(reqDTO.getPostType(), "GENERAL"));
        session.setDifficultyLevel(reqDTO.getDifficultyLevel() == null ? 1 : reqDTO.getDifficultyLevel());
        session.setEngineType(engineType);
        session.setSourceMode(defaultIfBlank(reqDTO.getSourceMode(), "HYBRID"));
        session.setTotalQuestionCount(selected.size());
        session = interviewSessionDomainService.createSession(session);

        List<InterviewQuestionRecord> records = new ArrayList<>();
        for (InterviewQuestionBO questionBO : selected) {
            InterviewQuestionRecord record = new InterviewQuestionRecord();
            record.setQuestionSource(engineType);
            record.setQuestionType("DEFAULT");
            record.setCategory(defaultIfBlank(questionBO.getLabelName(), questionBO.getKeyWord()));
            record.setDifficulty(session.getDifficultyLevel());
            record.setStem(questionBO.getSubjectName());
            record.setStandardAnswer(questionBO.getSubjectAnswer());
            record.setKeyWords(questionBO.getKeyWord());
            records.add(record);
        }
        interviewSessionDomainService.saveQuestionRecords(session.getId(), records);
        interviewSessionDomainService.markWaitingForFirstQuestion(session);

        List<InterviewQuestionRecord> savedRecords = interviewSessionDomainService.listQuestionRecords(session.getId());
        if (savedRecords.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "面试题保存失败");
        }

        StartInterviewSessionRespDTO respDTO = new StartInterviewSessionRespDTO();
        respDTO.setSessionId(session.getId());
        respDTO.setStatus(session.getStatus());
        respDTO.setTotalQuestionCount(selected.size());
        respDTO.setFirstQuestion(toQuestionDTO(savedRecords.get(0), 1));
        return respDTO;
    }

    public SubmitAnswerRespDTO submitAnswer(SubmitAnswerReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getAnswer())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "答案不能为空");
        }
        InterviewSession session = interviewSessionDomainService.getSession(reqDTO.getSessionId());
        InterviewQuestionRecord record = interviewSessionDomainService.getQuestionRecord(reqDTO.getRecordId());
        interviewSessionDomainService.validateCanSubmitAnswer(session, record);
        record.setUserAnswer(reqDTO.getAnswer().trim());
        record.setAnswerTime(new Date());
        if (record.getAskTime() != null) {
            record.setCostSeconds((int) ((record.getAnswerTime().getTime() - record.getAskTime().getTime()) / 1000));
        }
        interviewSessionDomainService.markEvaluating(session);
        EvaluateResultBO evaluateResult = interviewEvaluationDomainService.evaluate(session, record);
        record.setRuleScore(evaluateResult.getRuleScore());
        record.setAiScore(evaluateResult.getAiScore());
        record.setFinalScore(evaluateResult.getFinalScore());
        record.setHitKeywords(String.join("、", evaluateResult.getHitPoints()));
        record.setMissKeywords(String.join("、", evaluateResult.getMissPoints()));
        record.setWrongPoints(String.join("、", evaluateResult.getWrongPoints()));
        record.setEvaluationComment(evaluateResult.getComment());
        interviewSessionDomainService.saveEvaluatedQuestion(record);
        List<InterviewQuestionRecord> allRecords = interviewSessionDomainService.listQuestionRecords(session.getId());
        interviewSessionDomainService.refreshSessionScore(session, allRecords);

        SubmitAnswerRespDTO respDTO = new SubmitAnswerRespDTO();
        respDTO.setSessionId(session.getId());
        respDTO.setRecordId(record.getId());
        respDTO.setStatus(record.getStatus());
        respDTO.setQuestionResult(toQuestionDTO(record, session.getCurrentQuestionNo()));
        return respDTO;
    }

    public NextQuestionRespDTO nextQuestion(Long sessionId) {
        InterviewSession session = interviewSessionDomainService.getSession(sessionId);
        if (InterviewSessionStatusEnum.FINISHED.name().equals(session.getStatus())) {
            InterviewReport report = getOrCreateReport(session);
            NextQuestionRespDTO finished = new NextQuestionRespDTO();
            finished.setSessionId(session.getId());
            finished.setStatus(session.getStatus());
            finished.setFinished(true);
            finished.setReportId(report.getId());
            return finished;
        }
        InterviewQuestionRecord next = interviewSessionDomainService.activateNextQuestion(session);
        NextQuestionRespDTO respDTO = new NextQuestionRespDTO();
        respDTO.setSessionId(session.getId());
        respDTO.setStatus(session.getStatus());
        if (next == null) {
            InterviewReport report = getOrCreateReport(session);
            session.setReportId(report.getId());
            respDTO.setFinished(true);
            respDTO.setReportId(report.getId());
            return respDTO;
        }
        respDTO.setFinished(false);
        respDTO.setNextQuestion(toQuestionDTO(next, session.getCurrentQuestionNo()));
        return respDTO;
    }

    public InterviewSessionQuestionDTO getQuestionResult(Long recordId) {
        InterviewQuestionRecord record = interviewSessionDomainService.getQuestionRecord(recordId);
        InterviewSession session = interviewSessionDomainService.getSession(record.getSessionId());
        return toQuestionDTO(record, locateQuestionNo(session.getId(), recordId));
    }

    public InterviewSessionReportDTO getReport(Long sessionId) {
        InterviewSession session = interviewSessionDomainService.getSession(sessionId);
        if (!InterviewSessionStatusEnum.FINISHED.name().equals(session.getStatus())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "面试尚未结束，暂时不能查看完整报告");
        }
        List<InterviewQuestionRecord> records = interviewSessionDomainService.listQuestionRecords(sessionId);
        InterviewReport report = getOrCreateReport(session);
        return toReportDTO(session, report, records);
    }

    public InterviewSessionStatusDTO getSessionStatus(Long sessionId) {
        InterviewSession session = interviewSessionDomainService.getSession(sessionId);
        InterviewQuestionRecord current = interviewSessionDomainService.findCurrentWaitingQuestion(sessionId);
        if (current == null && InterviewSessionStatusEnum.INTERRUPTED.name().equals(session.getStatus())) {
            current = interviewSessionDomainService.findNextPendingQuestion(sessionId);
        }
        InterviewSessionStatusDTO dto = new InterviewSessionStatusDTO();
        dto.setSessionId(sessionId);
        dto.setStatus(session.getStatus());
        dto.setCurrentQuestionNo(session.getCurrentQuestionNo());
        dto.setTotalQuestionCount(session.getTotalQuestionCount());
        dto.setReportId(session.getReportId());
        dto.setTotalScore(session.getTotalScore());
        if (current != null) {
            dto.setCurrentQuestion(toQuestionDTO(current, locateQuestionNo(sessionId, current.getId())));
        }
        return dto;
    }

    public InterviewSessionStatusDTO interruptSession(Long sessionId) {
        InterviewSession session = interviewSessionDomainService.getSession(sessionId);
        interviewSessionDomainService.interruptSession(session);
        return getSessionStatus(sessionId);
    }

    public InterviewSessionStatusDTO resumeSession(Long sessionId) {
        InterviewSession session = interviewSessionDomainService.getSession(sessionId);
        InterviewQuestionRecord current = interviewSessionDomainService.resumeSession(session);
        InterviewSessionStatusDTO dto = getSessionStatus(sessionId);
        if (current != null) {
            dto.setCurrentQuestion(toQuestionDTO(current, locateQuestionNo(sessionId, current.getId())));
        }
        return dto;
    }

    private InterviewReport getOrCreateReport(InterviewSession session) {
        List<InterviewQuestionRecord> records = interviewSessionDomainService.listQuestionRecords(session.getId());
        InterviewReport report = interviewReportDomainService.generateAndSave(session, records);
        session.setReportId(report.getId());
        return report;
    }

    private List<KeywordBO> toKeywords(List<InterviewSessionKeywordDTO> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream().map(item -> {
            KeywordBO bo = new KeywordBO();
            bo.setKeyWord(item.getKeyWord());
            bo.setCategoryId(item.getCategoryId());
            bo.setLabelId(item.getLabelId());
            return bo;
        }).collect(Collectors.toList());
    }

    private InterviewSessionQuestionDTO toQuestionDTO(InterviewQuestionRecord record, Integer questionNo) {
        InterviewSessionQuestionDTO dto = new InterviewSessionQuestionDTO();
        dto.setRecordId(record.getId());
        dto.setQuestionNo(questionNo);
        dto.setQuestionSource(record.getQuestionSource());
        dto.setQuestionType(record.getQuestionType());
        dto.setKeyWord(record.getKeyWords());
        dto.setStem(record.getStem());
        dto.setStandardAnswer(record.getStandardAnswer());
        dto.setRuleScore(record.getRuleScore());
        dto.setAiScore(record.getAiScore());
        dto.setFinalScore(record.getFinalScore());
        dto.setHitPoints(parseTagList(record.getHitKeywords()));
        dto.setMissPoints(parseTagList(record.getMissKeywords()));
        dto.setWrongPoints(parseTagList(record.getWrongPoints()));
        dto.setComment(record.getEvaluationComment());
        dto.setStatus(record.getStatus());
        return dto;
    }

    private InterviewReportQuestionDTO toReportQuestionDTO(InterviewQuestionRecord record) {
        InterviewReportQuestionDTO dto = new InterviewReportQuestionDTO();
        dto.setRecordId(record.getId());
        dto.setStem(record.getStem());
        dto.setUserAnswer(record.getUserAnswer());
        dto.setFinalScore(record.getFinalScore());
        dto.setHitPoints(parseTagList(record.getHitKeywords()));
        dto.setMissPoints(parseTagList(record.getMissKeywords()));
        dto.setWrongPoints(parseTagList(record.getWrongPoints()));
        dto.setComment(record.getEvaluationComment());
        return dto;
    }

    private InterviewSessionReportDTO toReportDTO(InterviewSession session, InterviewReport report, List<InterviewQuestionRecord> records) {
        InterviewSessionReportDTO dto = new InterviewSessionReportDTO();
        dto.setReportId(report.getId());
        dto.setSessionId(session.getId());
        dto.setSessionStatus(session.getStatus());
        dto.setTotalScore(report.getTotalScore());
        dto.setBaseScore(report.getBaseScore());
        dto.setLogicScore(report.getLogicScore());
        dto.setExpressionScore(report.getExpressionScore());
        dto.setEngineeringScore(report.getEngineeringScore());
        dto.setSummary(report.getSummary());
        dto.setSuggestion(report.getSuggestion());
        dto.setWeaknessTags(parseTagList(report.getWeaknessTagsJson()));
        dto.setAdvantageTags(parseTagList(report.getAdvantageTagsJson()));
        dto.setQuestions(records.stream().map(this::toReportQuestionDTO).collect(Collectors.toList()));
        return dto;
    }

    private int locateQuestionNo(Long sessionId, Long recordId) {
        List<InterviewQuestionRecord> records = interviewSessionDomainService.listQuestionRecords(sessionId);
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId().equals(recordId)) {
                return i + 1;
            }
        }
        return 1;
    }

    private List<String> parseTagList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new ArrayList<>();
        }
        String cleaned = raw.replace("[", "").replace("]", "").replace("\"", "");
        return Arrays.stream(cleaned.split("[、,]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
