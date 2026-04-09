package com.colacode.subject.application.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.domain.service.SubjectFavoriteDomainService;
import com.colacode.subject.infra.entity.SubjectFavorite;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/favorite")
public class SubjectFavoriteController {

    private final SubjectFavoriteDomainService subjectFavoriteDomainService;

    public SubjectFavoriteController(SubjectFavoriteDomainService subjectFavoriteDomainService) {
        this.subjectFavoriteDomainService = subjectFavoriteDomainService;
    }

    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestParam Long subjectId) {
        Long userId = StpUtil.getLoginIdAsLong();
        subjectFavoriteDomainService.addFavorite(subjectId, userId);
        return Result.success();
    }

    @PostMapping("/remove")
    public Result<Void> removeFavorite(@RequestParam Long subjectId) {
        Long userId = StpUtil.getLoginIdAsLong();
        subjectFavoriteDomainService.removeFavorite(subjectId, userId);
        return Result.success();
    }

    @GetMapping("/myPage")
    public Result<PageResult<SubjectFavorite>> getMyFavoritePage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<SubjectFavorite> page = subjectFavoriteDomainService.getFavoritePage(userId, pageNo, pageSize);
        List<SubjectFavorite> list = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, list));
    }

    @GetMapping("/isFavorited")
    public Result<Boolean> isFavorited(@RequestParam Long subjectId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(subjectFavoriteDomainService.isFavorited(subjectId, userId));
    }
}
