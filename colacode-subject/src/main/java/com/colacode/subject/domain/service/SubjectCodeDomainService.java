package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.subject.application.converter.SubjectInfoDTOConverter;
import com.colacode.subject.application.dto.SubjectCodeJudgeDetailDTO;
import com.colacode.subject.domain.bo.SubjectCodeBO;
import com.colacode.subject.domain.bo.SubjectCodeCaseBO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.infra.entity.SubjectCode;
import com.colacode.subject.infra.entity.SubjectCodeCase;
import com.colacode.subject.infra.entity.SubjectInfo;
import com.colacode.subject.infra.mapper.SubjectCodeCaseMapper;
import com.colacode.subject.infra.mapper.SubjectCodeMapper;
import com.colacode.subject.infra.mapper.SubjectInfoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubjectCodeDomainService {

    public static final int SUBJECT_TYPE_CODE = 5;

    private final SubjectCodeMapper subjectCodeMapper;
    private final SubjectCodeCaseMapper subjectCodeCaseMapper;
    private final SubjectInfoMapper subjectInfoMapper;
    private final ObjectMapper objectMapper;

    public SubjectCodeDomainService(SubjectCodeMapper subjectCodeMapper,
                                    SubjectCodeCaseMapper subjectCodeCaseMapper,
                                    SubjectInfoMapper subjectInfoMapper,
                                    ObjectMapper objectMapper) {
        this.subjectCodeMapper = subjectCodeMapper;
        this.subjectCodeCaseMapper = subjectCodeCaseMapper;
        this.subjectInfoMapper = subjectInfoMapper;
        this.objectMapper = objectMapper;
    }

    public void saveCodeSubject(Long subjectId, SubjectCodeBO codeConfig, List<SubjectCodeCaseBO> testCases) {
        if (subjectId == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "题目ID不能为空");
        }
        if (codeConfig == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "编程题配置不能为空");
        }
        if (!StringUtils.hasText(codeConfig.getJudgeMode())) {
            codeConfig.setJudgeMode("STANDARD_IO");
        }
        if (codeConfig.getSupportedLanguages() == null || codeConfig.getSupportedLanguages().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "至少配置一种支持的语言");
        }
        if (testCases == null || testCases.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "至少配置一个测试用例");
        }
        boolean hasSampleCase = testCases.stream()
                .anyMatch(testCase -> Integer.valueOf(1).equals(testCase.getSampleCase()));
        if (!hasSampleCase) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "至少配置一个样例测试用例");
        }

        SubjectCode entity = findBySubjectId(subjectId);
        if (entity == null) {
            entity = new SubjectCode();
            entity.setSubjectId(subjectId);
        }
        entity.setJudgeMode(codeConfig.getJudgeMode());
        entity.setTimeLimitMs(defaultIfNull(codeConfig.getTimeLimitMs(), 1000));
        entity.setMemoryLimitKb(defaultIfNull(codeConfig.getMemoryLimitKb(), 131072));
        entity.setSupportedLanguagesJson(writeJson(codeConfig.getSupportedLanguages()));
        entity.setTemplateCodeJson(writeJson(codeConfig.getTemplateCode() == null ? Map.of() : codeConfig.getTemplateCode()));
        entity.setInputExample(codeConfig.getInputExample());
        entity.setOutputExample(codeConfig.getOutputExample());
        if (entity.getId() == null) {
            subjectCodeMapper.insert(entity);
        } else {
            subjectCodeMapper.updateById(entity);
        }

        LambdaQueryWrapper<SubjectCodeCase> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SubjectCodeCase::getSubjectId, subjectId);
        subjectCodeCaseMapper.delete(deleteWrapper);

        for (int i = 0; i < testCases.size(); i++) {
            SubjectCodeCaseBO testCaseBO = testCases.get(i);
            SubjectCodeCase testCase = new SubjectCodeCase();
            testCase.setSubjectId(subjectId);
            testCase.setCaseNo(testCaseBO.getCaseNo() != null ? testCaseBO.getCaseNo() : i + 1);
            testCase.setStdinText(testCaseBO.getStdinText());
            testCase.setExpectedStdout(testCaseBO.getExpectedStdout());
            testCase.setSampleCase(defaultIfNull(testCaseBO.getSampleCase(), 0));
            testCase.setScore(defaultIfNull(testCaseBO.getScore(), 1));
            subjectCodeCaseMapper.insert(testCase);
        }
    }

    public SubjectInfoBO queryPublicDetail(Long subjectId) {
        SubjectCode subjectCode = findBySubjectId(subjectId);
        if (subjectCode == null) {
            return new SubjectInfoBO();
        }
        SubjectInfoBO detail = new SubjectInfoBO();
        detail.setCodeConfig(toBO(subjectCode));
        detail.setTestCases(listCases(subjectId, true));
        return detail;
    }

    public SubjectCodeJudgeDetailDTO queryJudgeDetail(Long subjectId) {
        SubjectInfo subjectInfo = subjectInfoMapper.selectById(subjectId);
        if (subjectInfo == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "编程题不存在");
        }
        if (!Integer.valueOf(SUBJECT_TYPE_CODE).equals(subjectInfo.getSubjectType())) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "当前题目不是编程题");
        }

        SubjectCode subjectCode = findBySubjectId(subjectId);
        if (subjectCode == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "编程题判题配置不存在");
        }

        SubjectCodeJudgeDetailDTO dto = new SubjectCodeJudgeDetailDTO();
        dto.setSubjectId(subjectInfo.getId());
        dto.setSubjectName(subjectInfo.getSubjectName());
        dto.setSubjectType(subjectInfo.getSubjectType());
        dto.setCodeConfig(SubjectInfoDTOConverter.INSTANCE.convertToDTO(toBO(subjectCode)));
        dto.setTestCases(listCases(subjectId, false).stream()
                .map(SubjectInfoDTOConverter.INSTANCE::convertToDTO)
                .toList());
        return dto;
    }

    private SubjectCode findBySubjectId(Long subjectId) {
        LambdaQueryWrapper<SubjectCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCode::getSubjectId, subjectId);
        return subjectCodeMapper.selectOne(wrapper);
    }

    private List<SubjectCodeCaseBO> listCases(Long subjectId, boolean sampleOnly) {
        LambdaQueryWrapper<SubjectCodeCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectCodeCase::getSubjectId, subjectId);
        if (sampleOnly) {
            wrapper.eq(SubjectCodeCase::getSampleCase, 1);
        }
        List<SubjectCodeCase> cases = subjectCodeCaseMapper.selectList(wrapper);
        return cases.stream()
                .sorted(Comparator.comparing(SubjectCodeCase::getCaseNo))
                .map(this::toBO)
                .toList();
    }

    private SubjectCodeBO toBO(SubjectCode entity) {
        SubjectCodeBO bo = new SubjectCodeBO();
        bo.setJudgeMode(entity.getJudgeMode());
        bo.setTimeLimitMs(entity.getTimeLimitMs());
        bo.setMemoryLimitKb(entity.getMemoryLimitKb());
        bo.setSupportedLanguages(readList(entity.getSupportedLanguagesJson()));
        bo.setTemplateCode(readTemplateCode(entity.getTemplateCodeJson()));
        bo.setInputExample(entity.getInputExample());
        bo.setOutputExample(entity.getOutputExample());
        return bo;
    }

    private SubjectCodeCaseBO toBO(SubjectCodeCase entity) {
        SubjectCodeCaseBO bo = new SubjectCodeCaseBO();
        bo.setCaseNo(entity.getCaseNo());
        bo.setStdinText(entity.getStdinText());
        bo.setExpectedStdout(entity.getExpectedStdout());
        bo.setSampleCase(entity.getSampleCase());
        bo.setScore(entity.getScore());
        return bo;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "编程题配置序列化失败");
        }
    }

    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "编程题语言配置解析失败");
        }
    }

    private Map<String, String> readTemplateCode(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "编程题模板代码解析失败");
        }
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
