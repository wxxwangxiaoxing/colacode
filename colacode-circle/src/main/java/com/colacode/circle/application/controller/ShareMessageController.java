package com.colacode.circle.application.controller;

import com.colacode.circle.application.converter.CircleDTOConverter;
import com.colacode.circle.application.dto.MessageReadDTO;
import com.colacode.circle.application.dto.ShareMessageDTO;
import com.colacode.circle.domain.bo.ShareMessageBO;
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
@RequestMapping("/circle/message")
public class ShareMessageController {

    private final CircleDomainService circleDomainService;

    public ShareMessageController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @GetMapping("/unRead")
    public Result<Boolean> hasUnreadMessage() {
        Long userId = LoginUserContext.getLoginUserId();
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        return Result.success(circleDomainService.hasUnreadMessage(userId));
    }

    @GetMapping("/list")
    public Result<PageResult<ShareMessageDTO>> getMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.getLoginUserId();
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        PageResult<ShareMessageBO> pageResult = circleDomainService.getMessages(userId, pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toMessageDTOList(pageResult.getRecords())));
    }

    @PostMapping("/markRead")
    public Result<Void> markAsRead(@Valid @RequestBody MessageReadDTO readDTO) {
        circleDomainService.markMessageAsRead(readDTO.getMessageId());
        return Result.success();
    }

    @PostMapping("/markAllRead")
    public Result<Void> markAllAsRead() {
        Long userId = LoginUserContext.getLoginUserId();
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        circleDomainService.markAllMessagesAsRead(userId);
        return Result.success();
    }
}
