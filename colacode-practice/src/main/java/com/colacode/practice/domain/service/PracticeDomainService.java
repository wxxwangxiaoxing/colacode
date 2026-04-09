package com.colacode.practice.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.common.Result;
import com.colacode.practice.application.feign.SubjectFeignClient;
import com.colacode.practice.application.feign.dto.SubjectInfoDTO;
import com.colacode.practice.domain.bo.PracticeDetailBO;
import com.colacode.practice.domain.bo.PracticeInfoBO;
import com.colacode.practice.domain.bo.PracticeSetBO;
import com.colacode.practice.domain.bo.PracticeSubmitBO;
import com.colacode.practice.domain.converter.PracticeSetBOConverter;
import com.colacode.practice.infra.entity.PracticeDetail;
import com.colacode.practice.infra.entity.PracticeInfo;
import com.colacode.practice.infra.entity.PracticeSet;
import com.colacode.practice.infra.entity.PracticeSetDetail;
import com.colacode.practice.infra.mapper.PracticeDetailMapper;
import com.colacode.practice.infra.mapper.PracticeInfoMapper;
import com.colacode.practice.infra.mapper.PracticeSetDetailMapper;
import com.colacode.practice.infra.mapper.PracticeSetMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PracticeDomainService {

    private final PracticeSetMapper practiceSetMapper;
    private final PracticeSetDetailMapper practiceSetDetailMapper;
    private final PracticeInfoMapper practiceInfoMapper;
    private final PracticeDetailMapper practiceDetailMapper;
    private final SubjectFeignClient subjectFeignClient;

    public PracticeDomainService(PracticeSetMapper practiceSetMapper,
                                 PracticeSetDetailMapper practiceSetDetailMapper,
                                 PracticeInfoMapper practiceInfoMapper,
                                 PracticeDetailMapper practiceDetailMapper,
                                 SubjectFeignClient subjectFeignClient) {
        this.practiceSetMapper = practiceSetMapper;
        this.practiceSetDetailMapper = practiceSetDetailMapper;
        this.practiceInfoMapper = practiceInfoMapper;
        this.practiceDetailMapper = practiceDetailMapper;
        this.subjectFeignClient = subjectFeignClient;
    }

    public void addPracticeSet(PracticeSetBO setBO) {
        PracticeSet entity = PracticeSetBOConverter.INSTANCE.convertToEntity(setBO);
        practiceSetMapper.insert(entity);
        log.info("创建套题成功, id: {}", entity.getId());
    }

    public void addPracticeSetDetail(Long setId, List<Long> subjectIds) {
        int sort = 1;
        for (Long subjectId : subjectIds) {
            PracticeSetDetail detail = new PracticeSetDetail();
            detail.setSetId(setId);
            detail.setSubjectId(subjectId);
            detail.setSort(sort++);
            practiceSetDetailMapper.insert(detail);
        }
        log.info("添加套题题目成功, setId: {}, count: {}", setId, subjectIds.size());
    }

    public List<Long> getSubjectIdsBySetId(Long setId) {
        LambdaQueryWrapper<PracticeSetDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSetDetail::getSetId, setId);
        wrapper.orderByAsc(PracticeSetDetail::getSort);
        List<PracticeSetDetail> details = practiceSetDetailMapper.selectList(wrapper);
        List<Long> subjectIds = new ArrayList<>();
        for (PracticeSetDetail detail : details) {
            subjectIds.add(detail.getSubjectId());
        }
        return subjectIds;
    }

    public PracticeInfoBO submitPractice(PracticeSubmitBO submitBO) {
        int correctCount = 0;
        int totalCount = submitBO.getAnswers().size();

        PracticeInfo practiceInfo = new PracticeInfo();
        practiceInfo.setSetId(submitBO.getSetId());
        practiceInfo.setUserId(submitBO.getUserId());
        practiceInfo.setSubmitTime(new Date());
        practiceInfoMapper.insert(practiceInfo);

        for (PracticeSubmitBO.AnswerItemBO answer : submitBO.getAnswers()) {
            SubjectInfoDTO subjectInfo = getSubjectById(answer.getSubjectId());
            if (subjectInfo == null) continue;

            boolean isCorrect = checkAnswer(subjectInfo, answer.getUserAnswer());
            if (isCorrect) correctCount++;

            PracticeDetail detail = new PracticeDetail();
            detail.setPracticeId(practiceInfo.getId());
            detail.setSubjectId(answer.getSubjectId());
            detail.setUserAnswer(answer.getUserAnswer());
            detail.setCorrectAnswer(getCorrectAnswer(subjectInfo));
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setTimeUse(answer.getTimeUse());
            practiceDetailMapper.insert(detail);
        }

        int wrongCount = totalCount - correctCount;
        int totalScore = totalCount > 0 ? (correctCount * 100 / totalCount) : 0;

        practiceInfo.setTotalScore(totalScore);
        practiceInfo.setCorrectCount(correctCount);
        practiceInfo.setWrongCount(wrongCount);
        practiceInfoMapper.updateById(practiceInfo);

        PracticeInfoBO resultBO = new PracticeInfoBO();
        resultBO.setId(practiceInfo.getId());
        resultBO.setTotalScore(totalScore);
        resultBO.setCorrectCount(correctCount);
        resultBO.setWrongCount(wrongCount);

        log.info("提交练习成功, practiceId: {}, score: {}", practiceInfo.getId(), totalScore);
        return resultBO;
    }

    public List<PracticeInfoBO> getPracticeHistory(Long userId) {
        LambdaQueryWrapper<PracticeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeInfo::getUserId, userId);
        wrapper.orderByDesc(PracticeInfo::getCreatedTime);
        List<PracticeInfo> practiceList = practiceInfoMapper.selectList(wrapper);
        return practiceList.stream().map(p -> {
            PracticeInfoBO bo = new PracticeInfoBO();
            bo.setId(p.getId());
            bo.setSetId(p.getSetId());
            bo.setUserId(p.getUserId());
            bo.setTotalScore(p.getTotalScore());
            bo.setCorrectCount(p.getCorrectCount());
            bo.setWrongCount(p.getWrongCount());
            bo.setSubmitTime(p.getSubmitTime() != null ? p.getSubmitTime().toString() : null);
            return bo;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<PracticeDetailBO> getScoreDetail(Long practiceId) {
        LambdaQueryWrapper<PracticeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeDetail::getPracticeId, practiceId);
        List<PracticeDetail> details = practiceDetailMapper.selectList(wrapper);
        return details.stream().map(d -> {
            PracticeDetailBO bo = new PracticeDetailBO();
            bo.setId(d.getId());
            bo.setPracticeId(d.getPracticeId());
            bo.setSubjectId(d.getSubjectId());
            bo.setUserAnswer(d.getUserAnswer());
            bo.setCorrectAnswer(d.getCorrectAnswer());
            bo.setIsCorrect(d.getIsCorrect());
            return bo;
        }).collect(java.util.stream.Collectors.toList());
    }

    public PracticeDetailBO getSubjectDetail(Long practiceId, Long subjectId) {
        LambdaQueryWrapper<PracticeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeDetail::getPracticeId, practiceId);
        wrapper.eq(PracticeDetail::getSubjectId, subjectId);
        PracticeDetail detail = practiceDetailMapper.selectOne(wrapper);
        if (detail == null) {
            return null;
        }
        PracticeDetailBO bo = new PracticeDetailBO();
        bo.setId(detail.getId());
        bo.setPracticeId(detail.getPracticeId());
        bo.setSubjectId(detail.getSubjectId());
        bo.setUserAnswer(detail.getUserAnswer());
        bo.setCorrectAnswer(detail.getCorrectAnswer());
        bo.setIsCorrect(detail.getIsCorrect());
        return bo;
    }

    public PracticeInfoBO getReport(Long practiceId) {
        PracticeInfo practice = practiceInfoMapper.selectById(practiceId);
        if (practice == null) {
            return null;
        }
        PracticeInfoBO bo = new PracticeInfoBO();
        bo.setId(practice.getId());
        bo.setSetId(practice.getSetId());
        bo.setUserId(practice.getUserId());
        bo.setTotalScore(practice.getTotalScore());
        bo.setCorrectCount(practice.getCorrectCount());
        bo.setWrongCount(practice.getWrongCount());
        bo.setSubmitTime(practice.getSubmitTime() != null ? practice.getSubmitTime().toString() : null);

        PracticeSet practiceSet = practiceSetMapper.selectById(practice.getSetId());
        if (practiceSet != null) {
            bo.setSetName(practiceSet.getSetName());
        }

        return bo;
    }

    public List<PracticeInfoBO> getPracticeRankList() {
        LambdaQueryWrapper<PracticeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PracticeInfo::getUserId, PracticeInfo::getTotalScore);
        wrapper.orderByDesc(PracticeInfo::getTotalScore);
        wrapper.last("LIMIT 50");
        List<PracticeInfo> list = practiceInfoMapper.selectList(wrapper);
        return list.stream().map(p -> {
            PracticeInfoBO bo = new PracticeInfoBO();
            bo.setUserId(p.getUserId());
            bo.setTotalScore(p.getTotalScore());
            return bo;
        }).collect(java.util.stream.Collectors.toList());
    }

    public void giveUpPractice(Long practiceId) {
        PracticeInfo practice = new PracticeInfo();
        practice.setId(practiceId);
        practice.setTotalScore(0);
        practice.setCorrectCount(0);
        practiceInfoMapper.updateById(practice);
    }

    public List<PracticeSetBO> getPreSetList(int pageNo, int pageSize, String nameFilter) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PracticeSet> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PracticeSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSet::getStatus, 1);
        if (nameFilter != null && !nameFilter.isEmpty()) {
            wrapper.like(PracticeSet::getSetName, nameFilter);
        }
        wrapper.orderByDesc(PracticeSet::getCreatedTime);
        practiceSetMapper.selectPage(page, wrapper);
        return page.getRecords().stream().map(set -> {
            PracticeSetBO bo = new PracticeSetBO();
            bo.setId(set.getId());
            bo.setSetName(set.getSetName());
            bo.setDescription(set.getDescription());
            bo.setStatus(set.getStatus());
            return bo;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<Long> getSubjectIdsByLabelIds(List<Long> labelIds) {
        return new ArrayList<>();
    }

    public List<SubjectInfoDTO> getSpecialPracticeSubjects(List<Long> labelIds, int count) {
        List<SubjectInfoDTO> subjects = new ArrayList<>();
        try {
            Result<List<SubjectInfoDTO>> result = subjectFeignClient.batchQuerySubjects(new ArrayList<>());
            if (result != null && result.isSuccess() && result.getData() != null) {
                subjects = result.getData();
            }
        } catch (Exception e) {
            log.error("获取专项练习题目失败", e);
        }
        if (subjects.size() > count) {
            java.util.Collections.shuffle(subjects);
            subjects = subjects.subList(0, count);
        }
        return subjects;
    }

    public List<PracticeInfoBO> getUnCompletePractice(Long userId, int pageNo, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PracticeInfo> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PracticeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeInfo::getUserId, userId);
        wrapper.and(w -> w.isNull(PracticeInfo::getTotalScore).or().eq(PracticeInfo::getTotalScore, 0));
        wrapper.orderByDesc(PracticeInfo::getCreatedTime);
        practiceInfoMapper.selectPage(page, wrapper);
        return page.getRecords().stream().map(p -> {
            PracticeInfoBO bo = new PracticeInfoBO();
            bo.setId(p.getId());
            bo.setSetId(p.getSetId());
            bo.setUserId(p.getUserId());
            bo.setTotalScore(p.getTotalScore());
            bo.setCorrectCount(p.getCorrectCount());
            bo.setWrongCount(p.getWrongCount());
            bo.setSubmitTime(p.getSubmitTime() != null ? p.getSubmitTime().toString() : null);
            return bo;
        }).collect(java.util.stream.Collectors.toList());
    }

    public PracticeInfoBO submitSingleSubject(Long practiceId, Long subjectId, String userAnswer, Integer timeUse) {
        SubjectInfoDTO subjectInfo = getSubjectById(subjectId);
        if (subjectInfo == null) return null;

        boolean isCorrect = checkAnswer(subjectInfo, userAnswer);

        PracticeDetail detail = new PracticeDetail();
        detail.setPracticeId(practiceId);
        detail.setSubjectId(subjectId);
        detail.setUserAnswer(userAnswer);
        detail.setCorrectAnswer(getCorrectAnswer(subjectInfo));
        detail.setIsCorrect(isCorrect ? 1 : 0);
        detail.setTimeUse(timeUse);
        practiceDetailMapper.insert(detail);

        PracticeDetailBO detailBO = new PracticeDetailBO();
        detailBO.setId(detail.getId());
        detailBO.setSubjectId(subjectId);
        detailBO.setUserAnswer(userAnswer);
        detailBO.setCorrectAnswer(getCorrectAnswer(subjectInfo));
        detailBO.setIsCorrect(isCorrect ? 1 : 0);
        detailBO.setTimeUse(timeUse);

        PracticeInfoBO resultBO = new PracticeInfoBO();
        resultBO.setDetail(detailBO);
        return resultBO;
    }

    private SubjectInfoDTO getSubjectById(Long subjectId) {
        try {
            Result<SubjectInfoDTO> result = subjectFeignClient.querySubject(subjectId);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("调用 Subject 服务失败, subjectId: {}", subjectId, e);
        }
        return null;
    }

    private boolean checkAnswer(SubjectInfoDTO subjectInfo, String userAnswer) {
        if (subjectInfo == null || userAnswer == null) return false;
        return subjectInfo.getSubjectParse() != null &&
                subjectInfo.getSubjectParse().trim().equalsIgnoreCase(userAnswer.trim());
    }

    private String getCorrectAnswer(SubjectInfoDTO subjectInfo) {
        return subjectInfo != null && subjectInfo.getSubjectParse() != null
                ? subjectInfo.getSubjectParse() : "";
    }
}
