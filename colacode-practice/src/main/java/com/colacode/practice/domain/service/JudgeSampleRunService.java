package com.colacode.practice.domain.service;

import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.practice.application.dto.judge.JudgeRunSampleResultDTO;
import com.colacode.practice.application.feign.SubjectFeignClient;
import com.colacode.practice.application.feign.dto.SubjectCodeCaseDTO;
import com.colacode.practice.application.feign.dto.SubjectCodeJudgeDetailDTO;
import com.colacode.practice.config.JudgeProperties;
import com.colacode.practice.infra.judge.Judge0Client;
import com.colacode.practice.infra.judge.Judge0ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class JudgeSampleRunService {

    private final SubjectFeignClient subjectFeignClient;
    private final Judge0Client judge0Client;
    private final JudgeOutputComparator judgeOutputComparator;
    private final JudgeProperties judgeProperties;

    public JudgeSampleRunService(SubjectFeignClient subjectFeignClient,
                                 Judge0Client judge0Client,
                                 JudgeOutputComparator judgeOutputComparator,
                                 JudgeProperties judgeProperties) {
        this.subjectFeignClient = subjectFeignClient;
        this.judge0Client = judge0Client;
        this.judgeOutputComparator = judgeOutputComparator;
        this.judgeProperties = judgeProperties;
    }

    public JudgeRunSampleResultDTO runSample(Long userId, Long subjectId, String language, String code) {
        Integer languageId = resolveLanguageId(language);

        Result<SubjectCodeJudgeDetailDTO> result = subjectFeignClient.queryJudgeDetail(subjectId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "获取编程题判题配置失败");
        }

        SubjectCodeJudgeDetailDTO judgeDetail = result.getData();
        List<SubjectCodeCaseDTO> allCases = judgeDetail.getTestCases();
        if (allCases == null || allCases.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "编程题测试用例为空");
        }

        // 只取样例用例
        List<SubjectCodeCaseDTO> sampleCases = allCases.stream()
                .filter(c -> c.getSampleCase() != null && c.getSampleCase() == 1)
                .toList();

        if (sampleCases.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "该题目暂无样例测试用例");
        }

        JudgeRunSampleResultDTO overallResult = new JudgeRunSampleResultDTO();
        List<JudgeRunSampleResultDTO.SampleCaseResultDTO> caseResults = new ArrayList<>();
        String overallStatus = JudgeSubmissionExecutionService.STATUS_ACCEPTED;

        for (SubjectCodeCaseDTO testCase : sampleCases) {
            log.info("执行样例运行: subjectId={}, languageId={}, caseNo={}", subjectId, languageId, testCase.getCaseNo());

            Judge0ExecutionResult judgeResult = judge0Client.execute(
                    code,
                    languageId,
                    testCase.getStdinText(),
                    judgeDetail.getCodeConfig() == null ? null : judgeDetail.getCodeConfig().getTimeLimitMs(),
                    judgeDetail.getCodeConfig() == null ? null : judgeDetail.getCodeConfig().getMemoryLimitKb());

            String caseStatus = resolveStatus(judgeResult, testCase.getExpectedStdout());

            JudgeRunSampleResultDTO.SampleCaseResultDTO caseResult = new JudgeRunSampleResultDTO.SampleCaseResultDTO();
            caseResult.setCaseNo(testCase.getCaseNo());
            caseResult.setStatus(caseStatus);
            caseResult.setStdin(testCase.getStdinText());
            caseResult.setExpectedStdout(testCase.getExpectedStdout());
            caseResult.setActualStdout(judgeResult.getStdout());
            caseResult.setStderr(judgeResult.getStderr());
            caseResult.setExecuteTimeMs(judgeResult.getExecuteTimeMs());
            caseResult.setMemoryUsedKb(judgeResult.getMemoryUsedKb());
            caseResult.setMessage(buildMessage(judgeResult, caseStatus));
            caseResults.add(caseResult);

            if (!JudgeSubmissionExecutionService.STATUS_ACCEPTED.equals(caseStatus)) {
                overallStatus = caseStatus;
            }
        }

        overallResult.setOverallStatus(overallStatus);
        overallResult.setMessage(buildOverallMessage(overallStatus, caseResults));
        overallResult.setResults(caseResults);
        return overallResult;
    }

    private String resolveStatus(Judge0ExecutionResult result, String expectedStdout) {
        Integer statusId = result.getStatusId();
        if (statusId == null) {
            return JudgeSubmissionExecutionService.STATUS_SYSTEM_ERROR;
        }
        if (statusId == 3) {
            return judgeOutputComparator.matches(result.getStdout(), expectedStdout)
                    ? JudgeSubmissionExecutionService.STATUS_ACCEPTED
                    : JudgeSubmissionExecutionService.STATUS_WRONG_ANSWER;
        }
        if (statusId == 5) {
            return JudgeSubmissionExecutionService.STATUS_TIME_LIMIT;
        }
        if (statusId == 6) {
            return JudgeSubmissionExecutionService.STATUS_COMPILE_ERROR;
        }
        if (statusId == 13 || statusId == 14) {
            return JudgeSubmissionExecutionService.STATUS_SYSTEM_ERROR;
        }
        return JudgeSubmissionExecutionService.STATUS_RUNTIME_ERROR;
    }

    private String buildMessage(Judge0ExecutionResult result, String caseStatus) {
        if (JudgeSubmissionExecutionService.STATUS_ACCEPTED.equals(caseStatus)) {
            return "通过";
        }
        if (result.getStderr() != null && !result.getStderr().isBlank()) {
            return limitText(result.getStderr());
        }
        return result.getStatusDescription();
    }

    private String buildOverallMessage(String overallStatus, List<JudgeRunSampleResultDTO.SampleCaseResultDTO> results) {
        if (JudgeSubmissionExecutionService.STATUS_ACCEPTED.equals(overallStatus)) {
            return "全部样例通过";
        }
        // 找到第一个失败的用例
        for (JudgeRunSampleResultDTO.SampleCaseResultDTO r : results) {
            if (!JudgeSubmissionExecutionService.STATUS_ACCEPTED.equals(r.getStatus())) {
                return "样例 " + r.getCaseNo() + " 未通过: " + r.getMessage();
            }
        }
        return "运行完成";
    }

    private String limitText(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private Integer resolveLanguageId(String language) {
        String normalized = normalizeLanguage(language);
        Map<String, Integer> languages = judgeProperties.getLanguages();
        Integer languageId = languages.get(normalized);
        if (languageId == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "不支持的编程语言: " + language);
        }
        return languageId;
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "语言不能为空");
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }
}
