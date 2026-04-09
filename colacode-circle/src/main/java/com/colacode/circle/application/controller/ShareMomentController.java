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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/circle/moment")
public class ShareMomentController {

    private final CircleDomainService circleDomainService;

    public ShareMomentController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @PostMapping("/add")
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
    public Result<PageResult<ShareMomentDTO>> listMoments(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShareMomentBO> pageResult = circleDomainService.listMoments(pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toMomentDTOList(pageResult.getRecords())));
    }

    @PostMapping("/delete")
    public Result<Void> deleteMoment(@RequestBody ShareMomentDTO momentDTO) {
        if (momentDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "动态ID不能为空");
        }
        circleDomainService.deleteMoment(momentDTO.getId());
        return Result.success();
    }

    @PostMapping("/comment")
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
    public Result<PageResult<ShareCommentReplyDTO>> getComments(
            @RequestParam Long targetId,
            @RequestParam Integer type,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShareCommentReplyBO> pageResult = circleDomainService.getComments(targetId, type, pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toCommentDTOList(pageResult.getRecords())));
    }
}
