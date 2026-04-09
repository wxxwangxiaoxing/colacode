package com.colacode.interview.application.controller;

import com.colacode.common.LoginUserContext;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.interview.application.converter.InterviewDTOConverter;
import com.colacode.interview.application.dto.AnalyseReqDTO;
import com.colacode.interview.application.dto.InterviewDetailDTO;
import com.colacode.interview.application.dto.InterviewHistoryDTO;
import com.colacode.interview.application.dto.InterviewQuestionDTO;
import com.colacode.interview.application.dto.InterviewResultDTO;
import com.colacode.interview.application.dto.KeywordDTO;
import com.colacode.interview.application.dto.StartInterviewReqDTO;
import com.colacode.interview.application.dto.SubmitInterviewReqDTO;
import com.colacode.interview.application.dto.session.InterviewSessionQuestionDTO;
import com.colacode.interview.application.dto.session.InterviewSessionReportDTO;
import com.colacode.interview.application.dto.session.InterviewSessionStatusDTO;
import com.colacode.interview.application.dto.session.NextQuestionRespDTO;
import com.colacode.interview.application.dto.session.SessionActionReqDTO;
import com.colacode.interview.application.dto.session.StartInterviewSessionReqDTO;
import com.colacode.interview.application.dto.session.StartInterviewSessionRespDTO;
import com.colacode.interview.application.dto.session.SubmitAnswerReqDTO;
import com.colacode.interview.application.dto.session.SubmitAnswerRespDTO;
import com.colacode.interview.domain.bo.InterviewQuestionBO;
import com.colacode.interview.domain.bo.InterviewResultBO;
import com.colacode.interview.domain.bo.KeywordBO;
import com.colacode.interview.domain.manager.InterviewFlowManager;
import com.colacode.interview.domain.service.InterviewDomainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    private final InterviewDomainService interviewDomainService;
    private final InterviewFlowManager interviewFlowManager;

    public InterviewController(InterviewDomainService interviewDomainService,
                               InterviewFlowManager interviewFlowManager) {
        this.interviewDomainService = interviewDomainService;
        this.interviewFlowManager = interviewFlowManager;
    }

    @PostMapping("/analyse")
    public Result<List<KeywordDTO>> analyse(@RequestBody AnalyseReqDTO reqDTO) {
        List<KeywordBO> keywords = interviewDomainService.analyse(reqDTO.getEngineType(), reqDTO.getLabels());
        return Result.success(InterviewDTOConverter.INSTANCE.toKeywordDTOList(keywords));
    }

    @PostMapping("/start")
    public Result<List<InterviewQuestionDTO>> start(@RequestBody StartInterviewReqDTO reqDTO) {
        List<KeywordBO> keywords = reqDTO.getKeywords().stream().map(item -> {
            KeywordBO bo = new KeywordBO();
            bo.setKeyWord(item.getKeyWord());
            bo.setCategoryId(item.getCategoryId());
            bo.setLabelId(item.getLabelId());
            return bo;
        }).collect(java.util.stream.Collectors.toList());
        List<InterviewQuestionBO> questions = interviewDomainService.startInterview(reqDTO.getEngineType(), keywords);
        return Result.success(InterviewDTOConverter.INSTANCE.toQuestionDTOList(questions));
    }

    @PostMapping("/session/start")
    public Result<StartInterviewSessionRespDTO> startSession(@RequestBody StartInterviewSessionReqDTO reqDTO) {
        Long userId = LoginUserContext.getLoginUserIdOrDefault(reqDTO.getUserId());
        return Result.success(interviewFlowManager.startSession(userId, reqDTO));
    }

    @GetMapping("/session/detail")
    public Result<InterviewSessionStatusDTO> sessionDetail(@RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.getSessionStatus(sessionId));
    }

    @PostMapping("/session/interrupt")
    public Result<InterviewSessionStatusDTO> interrupt(@RequestBody SessionActionReqDTO reqDTO) {
        return Result.success(interviewFlowManager.interruptSession(reqDTO.getSessionId()));
    }

    @PostMapping("/session/resume")
    public Result<InterviewSessionStatusDTO> resume(@RequestBody SessionActionReqDTO reqDTO) {
        return Result.success(interviewFlowManager.resumeSession(reqDTO.getSessionId()));
    }

    @PostMapping("/answer/submit")
    public Result<SubmitAnswerRespDTO> submitAnswer(@RequestBody SubmitAnswerReqDTO reqDTO) {
        return Result.success(interviewFlowManager.submitAnswer(reqDTO));
    }

    @GetMapping("/question/result")
    public Result<InterviewSessionQuestionDTO> questionResult(@RequestParam Long recordId) {
        return Result.success(interviewFlowManager.getQuestionResult(recordId));
    }

    @GetMapping("/next-question")
    public Result<NextQuestionRespDTO> nextQuestion(@RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.nextQuestion(sessionId));
    }

    @GetMapping("/report")
    public Result<InterviewSessionReportDTO> report(@RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.getReport(sessionId));
    }

    @PostMapping("/submit")
    public Result<InterviewResultDTO> submit(@RequestBody SubmitInterviewReqDTO reqDTO) {
        Long userId = LoginUserContext.getLoginUserIdOrDefault(reqDTO.getUserId());
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }

        List<InterviewQuestionBO> questions = reqDTO.getQuestions().stream().map(q -> {
            InterviewQuestionBO bo = new InterviewQuestionBO();
            bo.setKeyWord(q.getKeyWord());
            bo.setSubjectName(q.getSubjectName());
            bo.setSubjectAnswer(q.getSubjectAnswer());
            bo.setUserAnswer(q.getUserAnswer());
            bo.setUserScore(q.getUserScore());
            return bo;
        }).collect(java.util.stream.Collectors.toList());

        InterviewResultBO result = interviewDomainService.submitAnswers(reqDTO.getEngineType(), questions);

        String keyWords = String.join("、", questions.stream().map(InterviewQuestionBO::getKeyWord).collect(java.util.stream.Collectors.toList()));
        Long interviewHistoryId = saveHistory(reqDTO, result, keyWords, userId);

        result.setAvgTips(result.getAvgTips() + " (记录ID: " + interviewHistoryId + ")");
        return Result.success(InterviewDTOConverter.INSTANCE.toResultDTO(result));
    }

    @GetMapping("/history")
    public Result<List<InterviewHistoryDTO>> getHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        userId = LoginUserContext.getLoginUserIdOrDefault(userId);
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        return Result.success(toHistoryDTOList(interviewDomainService.getHistory(userId, pageNo, pageSize)));
    }

    @GetMapping("/detail")
    public Result<List<InterviewDetailDTO>> getDetail(@RequestParam Long id) {
        return Result.success(toDetailDTOList(interviewDomainService.getDetail(id)));
    }

    private Long saveHistory(SubmitInterviewReqDTO reqDTO, InterviewResultBO result, String keyWords, Long userId) {
        return interviewDomainService.saveInterviewHistory(
                reqDTO.getInterviewUrl(),
                keyWords,
                result.getAvgScore(),
                result.getAvgTips(),
                userId,
                result.getTips() == null ? List.of() : result.getTips()
        );
    }

    private List<InterviewHistoryDTO> toHistoryDTOList(List<Map<String, Object>> historyList) {
        List<InterviewHistoryDTO> dtoList = new ArrayList<>();
        for (Map<String, Object> item : historyList) {
            InterviewHistoryDTO dto = new InterviewHistoryDTO();
            dto.setId(castLong(item.get("id")));
            dto.setAvgScore(castDouble(item.get("avgScore")));
            dto.setKeyWords((String) item.get("keyWords"));
            dto.setTip((String) item.get("tip"));
            dto.setCreatedTime(item.get("createdTime"));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private List<InterviewDetailDTO> toDetailDTOList(List<Map<String, Object>> detailList) {
        List<InterviewDetailDTO> dtoList = new ArrayList<>();
        for (Map<String, Object> item : detailList) {
            InterviewDetailDTO dto = new InterviewDetailDTO();
            dto.setScore(castDouble(item.get("score")));
            dto.setKeyWords((String) item.get("keyWords"));
            dto.setQuestion((String) item.get("question"));
            dto.setAnswer((String) item.get("answer"));
            dto.setUserAnswer((String) item.get("userAnswer"));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private Long castLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Double castDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }
}