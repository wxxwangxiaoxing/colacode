package com.colacode.practice.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.practice.application.feign.CachedSubjectFeignClient;
import com.colacode.practice.application.feign.dto.SubjectCodeCaseDTO;
import com.colacode.practice.application.feign.dto.SubjectCodeJudgeDetailDTO;
import com.colacode.practice.infra.entity.PracticeSubmission;
import com.colacode.practice.infra.entity.PracticeSubmissionCase;
import com.colacode.practice.infra.judge.Judge0Client;
import com.colacode.practice.infra.judge.Judge0ExecutionResult;
import com.colacode.practice.infra.mapper.PracticeSubmissionCaseMapper;
import com.colacode.practice.infra.mapper.PracticeSubmissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JudgeSubmissionExecutionService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_ACCEPTED = "AC";
    public static final String STATUS_WRONG_ANSWER = "WA";
    public static final String STATUS_TIME_LIMIT = "TLE";
    public static final String STATUS_RUNTIME_ERROR = "RE";
    public static final String STATUS_COMPILE_ERROR = "CE";
    public static final String STATUS_SYSTEM_ERROR = "SYSTEM_ERROR";

    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final PracticeSubmissionCaseMapper practiceSubmissionCaseMapper;
    private final CachedSubjectFeignClient cachedSubjectFeignClient;
    private final Judge0Client judge0Client;
    private final JudgeOutputComparator judgeOutputComparator;
    private final Executor judgeCaseExecutor;

    public JudgeSubmissionExecutionService(PracticeSubmissionMapper practiceSubmissionMapper,
                                           PracticeSubmissionCaseMapper practiceSubmissionCaseMapper,
                                           CachedSubjectFeignClient cachedSubjectFeignClient,
                                           Judge0Client judge0Client,
                                           JudgeOutputComparator judgeOutputComparator,
                                           Executor judgeCaseExecutor) {
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.practiceSubmissionCaseMapper = practiceSubmissionCaseMapper;
        this.cachedSubjectFeignClient = cachedSubjectFeignClient;
        this.judge0Client = judge0Client;
        this.judgeOutputComparator = judgeOutputComparator;
        this.judgeCaseExecutor = judgeCaseExecutor;
    }

    @Async("judgeTaskExecutor")
    public void processSubmissionAsync(Long submissionId) {
        long startTime = System.currentTimeMillis();
        PracticeSubmission submission = practiceSubmissionMapper.selectById(submissionId);
        if (submission == null) {
            return;
        }
        try {
            submission.setStatus(STATUS_RUNNING);
            practiceSubmissionMapper.updateById(submission);

            Result<SubjectCodeJudgeDetailDTO> result = cachedSubjectFeignClient.queryJudgeDetail(submission.getSubjectId());
            if (result == null || !result.isSuccess() || result.getData() == null) {
                throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "获取编程题判题配置失败");
            }

            SubjectCodeJudgeDetailDTO judgeDetail = result.getData();
            List<SubjectCodeCaseDTO> testCases = judgeDetail.getTestCases();
            if (testCases == null || testCases.isEmpty()) {
                throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "编程题测试用例为空");
            }

            clearExistingCases(submissionId);

            String code = submission.getCode() != null ? new String(submission.getCode()) : "";
            Integer languageId = submission.getLanguageId();
            Integer timeLimitMs = judgeDetail.getCodeConfig() == null ? null : judgeDetail.getCodeConfig().getTimeLimitMs();
            Integer memoryLimitKb = judgeDetail.getCodeConfig() == null ? null : judgeDetail.getCodeConfig().getMemoryLimitKb();

            // 并行执行所有测试用例
            List<CompletableFuture<CaseResult>> futures = new ArrayList<>();
            for (SubjectCodeCaseDTO testCase : testCases) {
                CompletableFuture<CaseResult> future = CompletableFuture.supplyAsync(() -> {
                    log.info("执行判题: submissionId={}, languageId={}, codeLength={}, testCase={}",
                            submissionId, languageId, code.length(), testCase.getCaseNo());
                    Judge0ExecutionResult judgeResult = judge0Client.execute(
                            code,
                            languageId,
                            testCase.getStdinText(),
                            timeLimitMs,
                            memoryLimitKb);
                    String caseStatus = resolveStatus(judgeResult, testCase.getExpectedStdout());
                    return new CaseResult(testCase, judgeResult, caseStatus);
                }, judgeCaseExecutor);
                futures.add(future);
            }

            // 等待所有用例执行完成（设置超时，防止无限等待）
            CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allDone.get(30, TimeUnit.SECONDS);

            // 收集结果
            List<CaseResult> results = new ArrayList<>();
            for (CompletableFuture<CaseResult> future : futures) {
                results.add(future.get());
            }

            // 按 caseNo 排序，确保顺序正确
            results.sort((a, b) -> Integer.compare(a.testCase.getCaseNo(), b.testCase.getCaseNo()));

            // 批量保存测试用例结果
            List<PracticeSubmissionCase> caseEntities = new ArrayList<>();
            int passCount = 0;
            int maxExecuteTime = 0;
            int maxMemory = 0;
            String finalStatus = STATUS_ACCEPTED;
            String judgeMessage = "全部测试用例通过";
            String stdoutPreview = null;
            String stderrPreview = null;

            for (CaseResult caseResult : results) {
                SubjectCodeCaseDTO testCase = caseResult.testCase;
                Judge0ExecutionResult judgeResult = caseResult.judgeResult;
                String caseStatus = caseResult.caseStatus;

                PracticeSubmissionCase caseEntity = new PracticeSubmissionCase();
                caseEntity.setSubmissionId(submissionId);
                caseEntity.setCaseNo(testCase.getCaseNo());
                caseEntity.setSampleCase(testCase.getSampleCase());
                caseEntity.setStatus(caseStatus);
                caseEntity.setStdinText(testCase.getStdinText());
                caseEntity.setExpectedStdout(testCase.getExpectedStdout());
                caseEntity.setActualStdout(judgeResult.getStdout());
                caseEntity.setStderrText(judgeResult.getStderr());
                caseEntity.setExecuteTimeMs(judgeResult.getExecuteTimeMs());
                caseEntity.setMemoryUsedKb(judgeResult.getMemoryUsedKb());
                caseEntity.setJudgeToken(judgeResult.getToken());
                caseEntity.setJudgeMessage(buildJudgeMessage(judgeResult, caseStatus));
                caseEntities.add(caseEntity);

                if (judgeResult.getExecuteTimeMs() != null) {
                    maxExecuteTime = Math.max(maxExecuteTime, judgeResult.getExecuteTimeMs());
                }
                if (judgeResult.getMemoryUsedKb() != null) {
                    maxMemory = Math.max(maxMemory, judgeResult.getMemoryUsedKb());
                }

                if (STATUS_ACCEPTED.equals(caseStatus)) {
                    passCount++;
                } else if (STATUS_ACCEPTED.equals(finalStatus)) {
                    // 第一个失败的用例决定最终状态
                    finalStatus = caseStatus;
                    judgeMessage = caseEntity.getJudgeMessage();
                    stdoutPreview = limitText(judgeResult.getStdout());
                    stderrPreview = limitText(judgeResult.getStderr());
                }
            }

            // 批量插入
            if (!caseEntities.isEmpty()) {
                for (PracticeSubmissionCase caseEntity : caseEntities) {
                    practiceSubmissionCaseMapper.insert(caseEntity);
                }
            }

            submission.setStatus(finalStatus);
            submission.setPassCaseCount(passCount);
            submission.setTotalCaseCount(testCases.size());
            submission.setExecuteTimeMs(maxExecuteTime == 0 ? null : maxExecuteTime);
            submission.setMemoryUsedKb(maxMemory == 0 ? null : maxMemory);
            submission.setJudgeMessage(judgeMessage);
            submission.setStdoutPreview(stdoutPreview);
            submission.setStderrPreview(stderrPreview);
            practiceSubmissionMapper.updateById(submission);

            long duration = System.currentTimeMillis() - startTime;
            log.info("判题完成: submissionId={}, status={}, duration={}ms, cases={}",
                    submissionId, finalStatus, duration, testCases.size());

        } catch (Exception e) {
            log.error("异步判题失败, submissionId: {}", submissionId, e);
            PracticeSubmission failed = new PracticeSubmission();
            failed.setId(submissionId);
            failed.setStatus(STATUS_SYSTEM_ERROR);
            failed.setJudgeMessage(e.getMessage());
            practiceSubmissionMapper.updateById(failed);
        }
    }

    private void clearExistingCases(Long submissionId) {
        LambdaQueryWrapper<PracticeSubmissionCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSubmissionCase::getSubmissionId, submissionId);
        practiceSubmissionCaseMapper.delete(wrapper);
    }

    private String resolveStatus(Judge0ExecutionResult result, String expectedStdout) {
        Integer statusId = result.getStatusId();
        if (statusId == null) {
            return STATUS_SYSTEM_ERROR;
        }
        if (statusId == 3) {
            return judgeOutputComparator.matches(result.getStdout(), expectedStdout)
                    ? STATUS_ACCEPTED
                    : STATUS_WRONG_ANSWER;
        }
        if (statusId == 5) {
            return STATUS_TIME_LIMIT;
        }
        if (statusId == 6) {
            return STATUS_COMPILE_ERROR;
        }
        if (statusId == 13 || statusId == 14) {
            return STATUS_SYSTEM_ERROR;
        }
        return STATUS_RUNTIME_ERROR;
    }

    private String buildJudgeMessage(Judge0ExecutionResult result, String caseStatus) {
        if (STATUS_ACCEPTED.equals(caseStatus)) {
            return "通过";
        }
        if (result.getStderr() != null && !result.getStderr().isBlank()) {
            return limitText(result.getStderr());
        }
        return result.getStatusDescription();
    }

    private String limitText(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private record CaseResult(SubjectCodeCaseDTO testCase, Judge0ExecutionResult judgeResult, String caseStatus) {
    }
}
