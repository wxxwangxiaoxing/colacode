package com.colacode.practice.application.controller;

import com.colacode.common.LoginUserContext;
import com.colacode.common.Result;
import com.colacode.practice.application.converter.PracticeDTOConverter;
import com.colacode.practice.application.dto.PracticeDetailDTO;
import com.colacode.practice.application.dto.PracticeInfoDTO;
import com.colacode.practice.application.dto.PracticeSetDTO;
import com.colacode.practice.application.dto.PracticeSubmitDTO;
import com.colacode.practice.domain.bo.PracticeDetailBO;
import com.colacode.practice.domain.bo.PracticeInfoBO;
import com.colacode.practice.domain.bo.PracticeSubmitBO;
import com.colacode.practice.domain.service.PracticeDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "练习管理", description = "题目练习相关接口")
public class PracticeController {

    private final PracticeDomainService practiceDomainService;

    public PracticeController(PracticeDomainService practiceDomainService) {
        this.practiceDomainService = practiceDomainService;
    }

    @PostMapping("/set/add")
    @Operation(summary = "添加练习组", description = "创建新的练习组")
    public Result<Void> addPracticeSet(@Valid @RequestBody PracticeSetDTO setDTO) {
        practiceDomainService.addPracticeSet(PracticeDTOConverter.INSTANCE.toSetBO(setDTO));
        return Result.success();
    }

    @PostMapping("/set/detail/add")
    @Operation(summary = "添加练习组详情", description = "向练习组添加题目")
    public Result<Void> addPracticeSetDetail(
            @Parameter(description = "练习组ID") @RequestParam Long setId,
            @RequestBody List<Long> subjectIds) {
        practiceDomainService.addPracticeSetDetail(setId, subjectIds);
        return Result.success();
    }

    @GetMapping("/set/subjects")
    @Operation(summary = "获取练习组题目", description = "根据练习组ID获取题目ID列表")
    public Result<List<Long>> getSubjectIdsBySetId(@Parameter(description = "练习组ID") @RequestParam Long setId) {
        return Result.success(practiceDomainService.getSubjectIdsBySetId(setId));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交练习", description = "提交练习答案")
    public Result<PracticeInfoDTO> submitPractice(@Valid @RequestBody PracticeSubmitDTO submitDTO) {
        PracticeSubmitBO submitBO = PracticeDTOConverter.INSTANCE.toSubmitBO(submitDTO);
        Long userId = submitBO.getUserId() != null ? submitBO.getUserId() : LoginUserContext.requireLoginUserId();
        submitBO.setUserId(userId);
        PracticeInfoBO practiceInfoBO = practiceDomainService.submitPractice(submitBO);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(practiceInfoBO));
    }

    @GetMapping("/history")
    @Operation(summary = "获取练习历史", description = "获取用户的练习历史记录")
    public Result<List<PracticeInfoDTO>> getPracticeHistory(@Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        userId = userId != null ? userId : LoginUserContext.requireLoginUserId();
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getPracticeHistory(userId)));
    }

    @GetMapping("/scoreDetail")
    @Operation(summary = "获取练习详情", description = "获取练习的详细得分信息")
    public Result<List<PracticeDetailDTO>> getScoreDetail(@Parameter(description = "练习ID") @RequestParam Long practiceId) {
        List<PracticeDetailBO> detailBOList = practiceDomainService.getScoreDetail(practiceId);
        return Result.success(PracticeDTOConverter.INSTANCE.toDetailDTOList(detailBOList));
    }

    @GetMapping("/subjectDetail")
    @Operation(summary = "获取题目详情", description = "获取练习中某道题目的详情")
    public Result<PracticeDetailDTO> getSubjectDetail(
            @Parameter(description = "练习ID") @RequestParam Long practiceId,
            @Parameter(description = "题目ID") @RequestParam Long subjectId) {
        PracticeDetailBO detail = practiceDomainService.getSubjectDetail(practiceId, subjectId);
        return Result.success(PracticeDTOConverter.INSTANCE.toDetailDTO(detail));
    }

    @GetMapping("/report")
    @Operation(summary = "获取练习报告", description = "获取练习报告")
    public Result<PracticeInfoDTO> getReport(@Parameter(description = "练习ID") @RequestParam Long practiceId) {
        PracticeInfoBO report = practiceDomainService.getReport(practiceId);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(report));
    }

    @GetMapping("/rankList")
    @Operation(summary = "获取排行榜", description = "获取练习排行榜")
    public Result<List<PracticeInfoDTO>> getPracticeRankList() {
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getPracticeRankList()));
    }

    @PostMapping("/giveUp")
    @Operation(summary = "放弃练习", description = "放弃进行中的练习")
    public Result<Void> giveUpPractice(@Parameter(description = "练习ID") @RequestParam Long practiceId) {
        practiceDomainService.giveUpPractice(practiceId);
        return Result.success();
    }

    @GetMapping("/preSet/list")
    @Operation(summary = "获取预设练习组", description = "获取预设练习组列表")
    public Result<List<PracticeSetDTO>> getPreSetList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "名称") @RequestParam(required = false) String name) {
        return Result.success(PracticeDTOConverter.INSTANCE.toSetDTOList(practiceDomainService.getPreSetList(pageNo, pageSize, name)));
    }

    @GetMapping("/special")
    @Operation(summary = "专项练习", description = "根据标签获取专项练习题目")
    public Result<List<com.colacode.practice.application.feign.dto.SubjectInfoDTO>> getSpecialPractice(
            @Parameter(description = "标签ID列表") @RequestParam List<Long> labelIds,
            @Parameter(description = "题目数量") @RequestParam(defaultValue = "10") int count) {
        return Result.success(practiceDomainService.getSpecialPracticeSubjects(labelIds, count));
    }

    @GetMapping("/unComplete")
    @Operation(summary = "获取未完成练习", description = "获取用户未完成的练习")
    public Result<List<PracticeInfoDTO>> getUnCompletePractice(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        userId = userId != null ? userId : LoginUserContext.requireLoginUserId();
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTOList(practiceDomainService.getUnCompletePractice(userId, pageNo, pageSize)));
    }

    @PostMapping("/submitSingle")
    @Operation(summary = "提交单题答案", description = "提交单道题的答案")
    public Result<PracticeInfoDTO> submitSingleSubject(
            @Parameter(description = "练习ID") @RequestParam Long practiceId,
            @Parameter(description = "题目ID") @RequestParam Long subjectId,
            @Parameter(description = "用户答案") @RequestParam String userAnswer,
            @Parameter(description = "用时(秒)") @RequestParam(required = false, defaultValue = "0") Integer timeUse) {
        PracticeInfoBO practiceInfoBO = practiceDomainService.submitSingleSubject(practiceId, subjectId, userAnswer, timeUse);
        return Result.success(PracticeDTOConverter.INSTANCE.toInfoDTO(practiceInfoBO));
    }
}
