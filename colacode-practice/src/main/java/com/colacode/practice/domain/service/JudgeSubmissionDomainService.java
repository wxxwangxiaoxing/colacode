package com.colacode.practice.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.practice.application.dto.judge.JudgeSubmissionCaseDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmissionDetailDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmissionSummaryDTO;
import com.colacode.practice.application.dto.judge.JudgeSubmitRespDTO;
import com.colacode.practice.config.JudgeProperties;
import com.colacode.practice.domain.bo.JudgeSubmitBO;
import com.colacode.practice.infra.entity.PracticeSubmission;
import com.colacode.practice.infra.entity.PracticeSubmissionCase;
import com.colacode.practice.infra.mapper.PracticeSubmissionCaseMapper;
import com.colacode.practice.infra.mapper.PracticeSubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JudgeSubmissionDomainService {

    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final PracticeSubmissionCaseMapper practiceSubmissionCaseMapper;
    private final JudgeSubmissionExecutionService judgeSubmissionExecutionService;
    private final JudgeProperties judgeProperties;
    private final JudgeSecurityService judgeSecurityService;

    public JudgeSubmissionDomainService(PracticeSubmissionMapper practiceSubmissionMapper,
                                        PracticeSubmissionCaseMapper practiceSubmissionCaseMapper,
                                        JudgeSubmissionExecutionService judgeSubmissionExecutionService,
                                        JudgeProperties judgeProperties,
                                        JudgeSecurityService judgeSecurityService) {
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.practiceSubmissionCaseMapper = practiceSubmissionCaseMapper;
        this.judgeSubmissionExecutionService = judgeSubmissionExecutionService;
        this.judgeProperties = judgeProperties;
        this.judgeSecurityService = judgeSecurityService;
    }

    public JudgeSubmitRespDTO submit(Long userId, JudgeSubmitBO submitBO) {
        judgeSecurityService.assertSubmissionAllowed(userId, submitBO.getSubjectId(), submitBO.getCode());
        Integer languageId = resolveLanguageId(submitBO.getLanguage());

        PracticeSubmission submission = new PracticeSubmission();
        submission.setUserId(userId);
        submission.setSubjectId(submitBO.getSubjectId());
        submission.setLanguage(normalizeLanguage(submitBO.getLanguage()));
        submission.setLanguageId(languageId);
        submission.setCode(submitBO.getCode() != null ? submitBO.getCode().getBytes() : null);
        submission.setStatus(JudgeSubmissionExecutionService.STATUS_PENDING);
        submission.setPassCaseCount(0);
        submission.setTotalCaseCount(0);
        if (judgeProperties.getAi().isEnabled()) {
            submission.setAiStatus(JudgeAiAnalysisService.AI_STATUS_PENDING);
        }
        practiceSubmissionMapper.insert(submission);

        judgeSubmissionExecutionService.processSubmissionAsync(submission.getId());

        JudgeSubmitRespDTO respDTO = new JudgeSubmitRespDTO();
        respDTO.setSubmissionId(submission.getId());
        respDTO.setStatus(submission.getStatus());
        return respDTO;
    }

    public JudgeSubmissionDetailDTO getDetail(Long userId, Long submissionId) {
        PracticeSubmission submission = requireOwnedSubmission(userId, submissionId);

        JudgeSubmissionDetailDTO dto = new JudgeSubmissionDetailDTO();
        dto.setId(submission.getId());
        dto.setSubjectId(submission.getSubjectId());
        dto.setLanguage(submission.getLanguage());
        dto.setStatus(submission.getStatus());
        dto.setPassCaseCount(submission.getPassCaseCount());
        dto.setTotalCaseCount(submission.getTotalCaseCount());
        dto.setExecuteTimeMs(submission.getExecuteTimeMs());
        dto.setMemoryUsedKb(submission.getMemoryUsedKb());
        dto.setJudgeMessage(submission.getJudgeMessage());
        dto.setStdoutPreview(submission.getStdoutPreview());
        dto.setStderrPreview(submission.getStderrPreview());
        dto.setAiStatus(submission.getAiStatus());
        dto.setAiFeedback(submission.getAiFeedback());
        dto.setCreatedTime(submission.getCreatedTime());
        List<JudgeSubmissionCaseDTO> cases = listSubmissionCases(submissionId);
        dto.setCases(cases);
        dto.setFailedCaseSummary(buildFailedCaseSummary(cases));
        return dto;
    }

    public List<JudgeSubmissionSummaryDTO> list(Long userId, Long subjectId, int pageNo, int pageSize, String sortField, String sortOrder) {
        Page<PracticeSubmission> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PracticeSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSubmission::getUserId, userId);
        if (subjectId != null) {
            wrapper.eq(PracticeSubmission::getSubjectId, subjectId);
        }
        // 排序处理
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        if ("executeTime".equalsIgnoreCase(sortField)) {
            wrapper.orderBy(true, isAsc, PracticeSubmission::getExecuteTimeMs);
        } else if ("memory".equalsIgnoreCase(sortField)) {
            wrapper.orderBy(true, isAsc, PracticeSubmission::getMemoryUsedKb);
        } else {
            // 默认按 ID 降序
            wrapper.orderByDesc(PracticeSubmission::getId);
        }
        practiceSubmissionMapper.selectPage(page, wrapper);
        return page.getRecords().stream().map(this::toSummaryDTO).toList();
    }

    private PracticeSubmission requireOwnedSubmission(Long userId, Long submissionId) {
        PracticeSubmission submission = practiceSubmissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "提交记录不存在");
        }
        if (!submission.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "无权查看该提交记录");
        }
        return submission;
    }

    private List<JudgeSubmissionCaseDTO> listSubmissionCases(Long submissionId) {
        LambdaQueryWrapper<PracticeSubmissionCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSubmissionCase::getSubmissionId, submissionId);
        wrapper.orderByAsc(PracticeSubmissionCase::getCaseNo);
        return practiceSubmissionCaseMapper.selectList(wrapper).stream().map(caseEntity -> {
            JudgeSubmissionCaseDTO dto = new JudgeSubmissionCaseDTO();
            dto.setCaseNo(caseEntity.getCaseNo());
            dto.setSampleCase(caseEntity.getSampleCase());
            dto.setStatus(caseEntity.getStatus());
            dto.setStdin(caseEntity.getStdinText());
            dto.setExpectedStdout(caseEntity.getExpectedStdout());
            dto.setActualStdout(caseEntity.getActualStdout());
            dto.setStderr(caseEntity.getStderrText());
            dto.setExecuteTimeMs(caseEntity.getExecuteTimeMs());
            dto.setMemoryUsedKb(caseEntity.getMemoryUsedKb());
            dto.setJudgeMessage(caseEntity.getJudgeMessage());
            return dto;
        }).toList();
    }

    private JudgeSubmissionSummaryDTO toSummaryDTO(PracticeSubmission submission) {
        JudgeSubmissionSummaryDTO dto = new JudgeSubmissionSummaryDTO();
        dto.setId(submission.getId());
        dto.setSubjectId(submission.getSubjectId());
        dto.setLanguage(submission.getLanguage());
        dto.setStatus(submission.getStatus());
        dto.setPassCaseCount(submission.getPassCaseCount());
        dto.setTotalCaseCount(submission.getTotalCaseCount());
        dto.setExecuteTimeMs(submission.getExecuteTimeMs());
        dto.setMemoryUsedKb(submission.getMemoryUsedKb());
        dto.setCreatedTime(submission.getCreatedTime());
        return dto;
    }

    private String buildFailedCaseSummary(List<JudgeSubmissionCaseDTO> cases) {
        if (cases == null || cases.isEmpty()) {
            return null;
        }
        for (JudgeSubmissionCaseDTO c : cases) {
            if (!JudgeSubmissionExecutionService.STATUS_ACCEPTED.equals(c.getStatus())) {
                StringBuilder sb = new StringBuilder();
                sb.append("测试点 ").append(c.getCaseNo());
                if (c.getSampleCase() != null && c.getSampleCase() == 1) {
                    sb.append(" (样例)");
                }
                sb.append(" 未通过 [").append(c.getStatus()).append("]");
                if (c.getStdin() != null && !c.getStdin().isBlank()) {
                    sb.append("\n输入: ").append(limitText(c.getStdin(), 200));
                }
                if (c.getExpectedStdout() != null && !c.getExpectedStdout().isBlank()) {
                    sb.append("\n预期输出: ").append(limitText(c.getExpectedStdout(), 200));
                }
                if (c.getActualStdout() != null && !c.getActualStdout().isBlank()) {
                    sb.append("\n实际输出: ").append(limitText(c.getActualStdout(), 200));
                }
                if (c.getStderr() != null && !c.getStderr().isBlank()) {
                    sb.append("\n错误信息: ").append(limitText(c.getStderr(), 300));
                }
                return sb.toString();
            }
        }
        return null;
    }

    private String limitText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
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
