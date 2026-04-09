package com.colacode.subject.application.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.application.mq.SubjectLikedMessage;
import com.colacode.subject.application.mq.SubjectLikedProducer;
import com.colacode.subject.domain.service.SubjectLikedDomainService;
import com.colacode.subject.infra.entity.SubjectLiked;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/liked")
public class SubjectLikedController {

    private final SubjectLikedProducer subjectLikedProducer;
    private final SubjectLikedDomainService subjectLikedDomainService;

    public SubjectLikedController(SubjectLikedProducer subjectLikedProducer,
                                  SubjectLikedDomainService subjectLikedDomainService) {
        this.subjectLikedProducer = subjectLikedProducer;
        this.subjectLikedDomainService = subjectLikedDomainService;
    }

    @PostMapping("/doLike")
    public Result<Void> doLike(
            @RequestParam Long subjectId,
            @RequestParam Integer likedStatus) {
        Long userId = StpUtil.getLoginIdAsLong();
        SubjectLikedMessage message = new SubjectLikedMessage();
        message.setSubjectId(subjectId);
        message.setLikedUserId(userId);
        message.setLikedStatus(likedStatus);
        subjectLikedProducer.sendLikedMessage(message);
        return Result.success();
    }

    @GetMapping("/myLikedPage")
    public Result<PageResult<SubjectLiked>> getMyLikedPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<SubjectLiked> page = subjectLikedDomainService.getLikedPage(userId, pageNo, pageSize);
        List<SubjectLiked> dtoList = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, dtoList));
    }

    @PostMapping("/delete")
    public Result<Void> deleteLiked(@RequestParam Long id) {
        subjectLikedDomainService.delete(id);
        return Result.success();
    }
}
