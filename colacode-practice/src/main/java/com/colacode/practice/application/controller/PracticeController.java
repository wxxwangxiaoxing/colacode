package com.colacode.practice.application.controller;

import com.colacode.common.LoginUserContext;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import com.colacode.practice.application.converter.PracticeDTOConverter;
import com.colacode.practice.application.dto.PracticeDetailDTO;
import com.colacode.practice.application.dto.PracticeInfoDTO;
import com.colacode.practice.application.dto.PracticeSetDTO;
import com.colacode.practice.application.dto.PracticeSubmitDTO;
import com.colacode.practice.domain.bo.PracticeDetailBO;
import com.colacode.practice.domain.bo.PracticeInfoBO;
import com.colacode.practice.domain.bo.PracticeSubmitBO;
import com.colacode.practice.domain.service.PracticeDomainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/practice")
public class PracticeController {

    private final PracticeDomainService practiceDomainService;

    public PracticeController(PracticeDomainService practiceDomainService) {
        this.practiceDomainService = practiceDomainService;
    }

    @PostMapping("/set/add")
    public Result<Void> addPracticeSet(@Valid @RequestBody PracticeSetDTO setDTO) {
        practiceDomainService.addPracticeSet(PracticeDTOConverter.INSTANCE.toSetBO(setDTO));
        return Result.success();
    }

    @PostMapping("/set/detail/add")
    public Result<Void> addPracticeSetDetail(
            @RequestParam Long setId,
            @RequestBody List<Long> subjectIds) {
        practiceDomainService.addPracticeSetDetail(setId, subjectIds);
        return Result.success();
    }

    @GetMapping("/set/subjects")
    public Result<List<Long>> getSubjectIdsBySetId(@RequestParam Long setId) {
        return Result.success(practiceDomainService.getSubjectIdsBySetId(setId));
    }

    @PostMapping("/submit")
    public Result<PracticeInfoDTO> submitPractice(@Valid @RequestBody PracticeSubmitDTO submitDTO) {
        PracticeSubmitBO submitBO = PracticeDTOConverter.INSTANCE.toSubmitBO(submitDTO);
        Long userId = LoginUserContext.getLoginUserIdOrDefault(submitBO.getUserId());
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        submitBO.setUserId(userId);
        PracticeInfoBO practiceInfoBO = practiceDomainService.submitPractice(submitBO);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(practiceInfoBO));
    }

    @GetMapping("/history")
    public Result<List<PracticeInfoDTO>> getPracticeHistory(@RequestParam(required = false) Long userId) {
        userId = LoginUserContext.getLoginUserIdOrDefault(userId);
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getPracticeHistory(userId)));
    }

    @GetMapping("/scoreDetail")
    public Result<List<PracticeDetailDTO>> getScoreDetail(@RequestParam Long practiceId) {
        List<PracticeDetailBO> detailBOList = practiceDomainService.getScoreDetail(practiceId);
        return Result.success(PracticeDTOConverter.INSTANCE.toDetailDTOList(detailBOList));
    }

    @GetMapping("/subjectDetail")
    public Result<PracticeDetailDTO> getSubjectDetail(
            @RequestParam Long practiceId,
            @RequestParam Long subjectId) {
        PracticeDetailBO detail = practiceDomainService.getSubjectDetail(practiceId, subjectId);
        return Result.success(PracticeDTOConverter.INSTANCE.toDetailDTO(detail));
    }

    @GetMapping("/report")
    public Result<PracticeInfoDTO> getReport(@RequestParam Long practiceId) {
        PracticeInfoBO report = practiceDomainService.getReport(practiceId);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(report));
    }

    @GetMapping("/rankList")
    public Result<List<PracticeInfoDTO>> getPracticeRankList() {
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getPracticeRankList()));
    }

    @PostMapping("/giveUp")
    public Result<Void> giveUpPractice(@RequestParam Long practiceId) {
        practiceDomainService.giveUpPractice(practiceId);
        return Result.success();
    }

    @GetMapping("/preSet/list")
    public Result<List<PracticeSetDTO>> getPreSetList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(PracticeDTOConverter.INSTANCE.toSetDTOList(practiceDomainService.getPreSetList(pageNo, pageSize, name)));
    }

    @GetMapping("/special")
    public Result<List<com.colacode.practice.application.feign.dto.SubjectInfoDTO>> getSpecialPractice(
            @RequestParam List<Long> labelIds,
            @RequestParam(defaultValue = "10") int count) {
        return Result.success(practiceDomainService.getSpecialPracticeSubjects(labelIds, count));
    }

    @GetMapping("/unComplete")
    public Result<List<PracticeInfoDTO>> getUnCompletePractice(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        userId = LoginUserContext.getLoginUserIdOrDefault(userId);
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getUnCompletePractice(userId, pageNo, pageSize)));
    }

    @PostMapping("/submitSingle")
    public Result<PracticeInfoDTO> submitSingleSubject(
            @RequestParam Long practiceId,
            @RequestParam Long subjectId,
            @RequestParam String userAnswer,
            @RequestParam(required = false, defaultValue = "0") Integer timeUse) {
        PracticeInfoBO practiceInfoBO = practiceDomainService.submitSingleSubject(practiceId, subjectId, userAnswer, timeUse);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(practiceInfoBO));
    }
}
