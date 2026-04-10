package com.colacode.circle.application.controller;

import com.colacode.circle.application.converter.CircleDTOConverter;
import com.colacode.circle.application.dto.MessageReadDTO;
import com.colacode.circle.application.dto.ShareMessageDTO;
import com.colacode.circle.domain.bo.ShareMessageBO;
import com.colacode.circle.domain.service.CircleDomainService;
import com.colacode.common.LoginUserContext;
import com.colacode.common.PageResult;
import com.colacode.common.Result;
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
@RequestMapping("/circle/message")
@Tag(name = "消息管理", description = "社区消息通知")
public class ShareMessageController {

    private final CircleDomainService circleDomainService;

    public ShareMessageController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @GetMapping("/unRead")
    @Operation(summary = "检查未读消息", description = "检查是否有未读消息")
    public Result<Boolean> hasUnreadMessage() {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(circleDomainService.hasUnreadMessage(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "分页获取消息列表")
    public Result<PageResult<ShareMessageDTO>> getMessages(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.requireLoginUserId();
        PageResult<ShareMessageBO> pageResult = circleDomainService.getMessages(userId, pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                CircleDTOConverter.INSTANCE.toMessageDTOList(pageResult.getRecords())));
    }

    @PostMapping("/markRead")
    @Operation(summary = "标记已读", description = "标记消息为已读")
    public Result<Void> markAsRead(@Valid @RequestBody MessageReadDTO readDTO) {
        circleDomainService.markMessageAsRead(readDTO.getMessageId());
        return Result.success();
    }

    @PostMapping("/markAllRead")
    @Operation(summary = "全部已读", description = "标记所有消息为已读")
    public Result<Void> markAllAsRead() {
        Long userId = LoginUserContext.requireLoginUserId();
        circleDomainService.markAllMessagesAsRead(userId);
        return Result.success();
    }
}
