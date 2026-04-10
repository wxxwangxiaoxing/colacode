package com.colacode.circle.application.controller;

import com.colacode.circle.application.converter.CircleDTOConverter;
import com.colacode.circle.application.dto.ShareCommentReplyDTO;
import com.colacode.circle.application.dto.ShareMomentDTO;
import com.colacode.circle.domain.bo.ShareCommentReplyBO;
import com.colacode.circle.domain.bo.ShareMomentBO;
import com.colacode.circle.domain.service.CircleDomainService;
import com.colacode.common.LoginUserContext;
import com.colacode.common.PageResult;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
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

@RestController
@RequestMapping("/circle/moment")
@Tag(name = "动态管理", description = "社区动态发布与评论")
public class ShareMomentController {

    private final CircleDomainService circleDomainService;

    public ShareMomentController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "发布动态", description = "发布新的社区动态")
    public Result<Void> addMoment(@Valid @RequestBody ShareMomentDTO momentDTO) {
        ShareMomentBO momentBO = CircleDTOConverter.INSTANCE.toMomentBO(momentDTO);
        Long userId = LoginUserContext.getLoginUserIdOrDefault(momentBO.getUserId());
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        momentBO.setUserId(userId);
        circleDomainService.addMoment(momentBO);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取动态列表", description = "分页获取社区动态列表")
    public Result<PageResult<ShareMomentDTO>> listMoments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShareMomentBO> pageResult = circleDomainService.listMoments(pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toMomentDTOList(pageResult.getRecords())));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除动态", description = "删除社区动态")
    public Result<Void> deleteMoment(@RequestBody ShareMomentDTO momentDTO) {
        if (momentDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "动态ID不能为空");
        }
        circleDomainService.deleteMoment(momentDTO.getId());
        return Result.success();
    }

    @PostMapping("/comment")
    @Operation(summary = "发表评论", description = "对动态发表评论或回复")
    public Result<Void> addComment(@Valid @RequestBody ShareCommentReplyDTO commentDTO) {
        ShareCommentReplyBO commentBO = CircleDTOConverter.INSTANCE.toCommentBO(commentDTO);
        Long userId = LoginUserContext.getLoginUserIdOrDefault(commentBO.getUserId());
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        commentBO.setUserId(userId);
        circleDomainService.addComment(commentBO);
        return Result.success();
    }

    @GetMapping("/comment/list")
    @Operation(summary = "获取评论列表", description = "分页获取动态的评论列表")
    public Result<PageResult<ShareCommentReplyDTO>> getComments(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "类型") @RequestParam Integer type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShareCommentReplyBO> pageResult = circleDomainService.getComments(targetId, type, pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toCommentDTOList(pageResult.getRecords())));
    }
}
