package com.colacode.subject.application.controller;

import com.colacode.common.LoginUserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.common.PageResult;
import com.colacode.common.PageUtil;
import com.colacode.common.Result;
import com.colacode.subject.domain.service.SubjectFavoriteDomainService;
import com.colacode.subject.infra.entity.SubjectFavorite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject/favorite")
@Tag(name = "收藏管理", description = "用户题目收藏管理")
public class SubjectFavoriteController {

    private final SubjectFavoriteDomainService subjectFavoriteDomainService;

    public SubjectFavoriteController(SubjectFavoriteDomainService subjectFavoriteDomainService) {
        this.subjectFavoriteDomainService = subjectFavoriteDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "添加收藏", description = "收藏题目")
    public Result<Void> addFavorite(@Parameter(description = "题目ID") @RequestParam Long subjectId) {
        Long userId = LoginUserContext.requireLoginUserId();
        subjectFavoriteDomainService.addFavorite(subjectId, userId);
        return Result.success();
    }

    @PostMapping("/remove")
    @Operation(summary = "取消收藏", description = "取消收藏题目")
    public Result<Void> removeFavorite(@Parameter(description = "题目ID") @RequestParam Long subjectId) {
        Long userId = LoginUserContext.requireLoginUserId();
        subjectFavoriteDomainService.removeFavorite(subjectId, userId);
        return Result.success();
    }

    @GetMapping("/myPage")
    @Operation(summary = "分页获取收藏", description = "分页获取当前用户的收藏列表")
    public Result<PageResult<SubjectFavorite>> getMyFavoritePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = LoginUserContext.requireLoginUserId();
        Page<SubjectFavorite> page = subjectFavoriteDomainService.getFavoritePage(userId, pageNo, pageSize);
        List<SubjectFavorite> list = page.getRecords();
        return Result.success(PageUtil.toPageResult(page, list));
    }

    @GetMapping("/isFavorited")
    @Operation(summary = "检查是否收藏", description = "检查题目是否已收藏")
    public Result<Boolean> isFavorited(@Parameter(description = "题目ID") @RequestParam Long subjectId) {
        Long userId = LoginUserContext.requireLoginUserId();
        return Result.success(subjectFavoriteDomainService.isFavorited(subjectId, userId));
    }
}
