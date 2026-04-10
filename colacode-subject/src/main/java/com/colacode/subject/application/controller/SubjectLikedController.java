package com.colacode.subject.application.controller;

import com.colacode.common.LoginUserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.application.mq.SubjectLikedMessage;
import com.colacode.subject.application.mq.SubjectLikedProducer;
import com.colacode.subject.domain.service.SubjectLikedDomainService;
import com.colacode.subject.infra.entity.SubjectLiked;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/liked")
@Tag(name = "点赞管理", description="用户题目点赞管理")
public class SubjectLikedController {

    private final SubjectLikedProducer subjectLikedProducer;
    private final SubjectLikedDomainService subjectLikedDomainService;

    public SubjectLikedController(SubjectLikedProducer subjectLikedProducer,
                                  SubjectLikedDomainService subjectLikedDomainService) {
        this.subjectLikedProducer = subjectLikedProducer;
        this.subjectLikedDomainService = subjectLikedDomainService;
    }

    @PostMapping("/doLike")
    @Operation(summary = "点赞/取消点赞", description = "对题目进行点赞或取消点赞")
    public Result<Void> doLike(
            @Parameter(description = "题目ID") @RequestParam Long subjectId,
            @Parameter(description = "点赞状态(1点赞,0取消)") @RequestParam Integer likedStatus) {
        Long userId = LoginUserContext.requireLoginUserId();
        SubjectLikedMessage message = new SubjectLikedMessage();
        message.setSubjectId(subjectId);
        message.setLikedUserId(userId);
        message.setLikedStatus(likedStatus);
        subjectLikedProducer.sendLikedMessage(message);
        return Result.success();
    }

    @GetMapping("/myLikedPage")
    @Operation(summary = "分页获取点赞", description = "分页获取当前用户的点赞列表")
    public Result<PageResult<SubjectLiked>> getMyLikedPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.requireLoginUserId();
        Page<SubjectLiked> page = subjectLikedDomainService.getLikedPage(userId, pageNo, pageSize);
        List<SubjectLiked> dtoList = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, dtoList));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除点赞记录", description = "删除点赞记录")
    public Result<Void> deleteLiked(@Parameter(description = "点赞记录ID") @RequestParam Long id) {
        subjectLikedDomainService.delete(id);
        return Result.success();
    }
}
