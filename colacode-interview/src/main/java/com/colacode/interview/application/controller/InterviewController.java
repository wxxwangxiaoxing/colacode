package com.colacode.interview.application.controller;

import com.colacode.common.LoginUserContext;
import com.colacode.common.Result;
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
import com.colacode.interview.application.dto.session.InterviewSessionSummaryDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "面试管理", description = "AI模拟面试相关接口")
public class InterviewController {

    private final InterviewDomainService interviewDomainService;
    private final InterviewFlowManager interviewFlowManager;

    public InterviewController(InterviewDomainService interviewDomainService,
                               InterviewFlowManager interviewFlowManager) {
        this.interviewDomainService = interviewDomainService;
        this.interviewFlowManager = interviewFlowManager;
    }

    @PostMapping("/analyse")
    @Operation(summary = "分析关键词", description = "根据标签分析面试关键词")
    public Result<List<KeywordDTO>> analyse(@RequestBody AnalyseReqDTO reqDTO) {
        List<KeywordBO> keywords = interviewDomainService.analyse(reqDTO.getEngineType(), reqDTO.getLabels());
        return Result.success(InterviewDTOConverter.INSTANCE.toKeywordDTOList(keywords));
    }

    @PostMapping("/start")
    @Operation(summary = "开始面试", description = "根据关键词开始面试")
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
    @Operation(summary = "开始面试会话", description = "创建新的AI面试会话")
    public Result<StartInterviewSessionRespDTO> startSession(@RequestBody StartInterviewSessionReqDTO reqDTO) {
        Long userId = reqDTO.getUserId() != null ? reqDTO.getUserId() : LoginUserContext.requireLoginUserId();
        return Result.success(interviewFlowManager.startSession(userId, reqDTO));
    }

    @GetMapping("/session/list")
    @Operation(summary = "获取面试会话列表", description = "获取用户的面试会话列表")
    public Result<List<InterviewSessionSummaryDTO>> sessionList(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        userId = userId != null ? userId : LoginUserContext.requireLoginUserId();
        return Result.success(interviewFlowManager.listSessions(userId, pageNo, pageSize));
    }

    @GetMapping("/session/detail")
    @Operation(summary = "获取会话详情", description = "获取面试会话的详细状态")
    public Result<InterviewSessionStatusDTO> sessionDetail(@Parameter(description = "会话ID") @RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.getSessionStatus(sessionId));
    }

    @PostMapping("/session/interrupt")
    @Operation(summary = "中断会话", description = "中断正在进行的面试会话")
    public Result<InterviewSessionStatusDTO> interrupt(@RequestBody SessionActionReqDTO reqDTO) {
        return Result.success(interviewFlowManager.interruptSession(reqDTO.getSessionId()));
    }

    @PostMapping("/session/resume")
    @Operation(summary = "恢复会话", description = "恢复被中断的面试会话")
    public Result<InterviewSessionStatusDTO> resume(@RequestBody SessionActionReqDTO reqDTO) {
        return Result.success(interviewFlowManager.resumeSession(reqDTO.getSessionId()));
    }

    @PostMapping("/answer/submit")
    @Operation(summary = "提交答案", description = "提交用户答案给AI面试官")
    public Result<SubmitAnswerRespDTO> submitAnswer(@RequestBody SubmitAnswerReqDTO reqDTO) {
        return Result.success(interviewFlowManager.submitAnswer(reqDTO));
    }

    @GetMapping("/question/result")
    @Operation(summary = "获取答题结果", description = "获取用户答题结果")
    public Result<InterviewSessionQuestionDTO> questionResult(@Parameter(description = "记录ID") @RequestParam Long recordId) {
        return Result.success(interviewFlowManager.getQuestionResult(recordId));
    }

    @GetMapping("/next-question")
    @Operation(summary = "获取下一题", description = "获取面试的下一道题目")
    public Result<NextQuestionRespDTO> nextQuestion(@Parameter(description = "会话ID") @RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.nextQuestion(sessionId));
    }

    @GetMapping("/report")
    @Operation(summary = "获取面试报告", description = "获取面试会话的报告")
    public Result<InterviewSessionReportDTO> report(@Parameter(description = "会话ID") @RequestParam Long sessionId) {
        return Result.success(interviewFlowManager.getReport(sessionId));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交面试结果", description = "提交面试答案获取评估结果")
    public Result<InterviewResultDTO> submit(@RequestBody SubmitInterviewReqDTO reqDTO) {
        Long userId = reqDTO.getUserId() != null ? reqDTO.getUserId() : LoginUserContext.requireLoginUserId();

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
    @Operation(summary = "获取面试历史", description = "获取用户的面试历史记录")
    public Result<List<InterviewHistoryDTO>> getHistory(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        userId = userId != null ? userId : LoginUserContext.requireLoginUserId();
        return Result.success(toHistoryDTOList(interviewDomainService.getHistory(userId, pageNo, pageSize)));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取面试详情", description = "获取面试的详细评估信息")
    public Result<List<InterviewDetailDTO>> getDetail(@Parameter(description = "面试ID") @RequestParam Long id) {
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
