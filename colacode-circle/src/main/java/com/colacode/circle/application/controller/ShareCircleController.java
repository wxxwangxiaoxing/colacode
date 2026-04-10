package com.colacode.circle.application.controller;

import com.colacode.circle.application.converter.ShareCircleDTOConverter;
import com.colacode.circle.application.dto.ShareCircleDTO;
import com.colacode.circle.domain.bo.ShareCircleBO;
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
@RequestMapping("/circle/share")
@Tag(name = "圈子管理", description = "社区圈子管理")
public class ShareCircleController {

    private final CircleDomainService circleDomainService;

    public ShareCircleController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @PostMapping("/add")
    @Operation(summary = "创建圈子", description = "创建新的社区圈子")
    public Result<Void> addCircle(@Valid @RequestBody ShareCircleDTO circleDTO) {
        ShareCircleBO circleBO = ShareCircleDTOConverter.INSTANCE.convertToBO(circleDTO);
        Long userId = LoginUserContext.getLoginUserIdOrDefault(circleBO.getUserId());
        if (userId == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "未获取到登录用户信息");
        }
        circleBO.setUserId(userId);
        circleDomainService.addCircle(circleBO);
        return Result.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新圈子", description = "更新圈子信息")
    public Result<Void> updateCircle(@Valid @RequestBody ShareCircleDTO circleDTO) {
        if (circleDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "圈子ID不能为空");
        }
        circleDomainService.updateCircle(ShareCircleDTOConverter.INSTANCE.convertToBO(circleDTO));
        return Result.success();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除圈子", description = "删除圈子")
    public Result<Void> deleteCircle(@RequestBody ShareCircleDTO circleDTO) {
        if (circleDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "圈子ID不能为空");
        }
        circleDomainService.deleteCircle(circleDTO.getId());
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取圈子列表", description = "分页获取圈子列表")
    public Result<PageResult<ShareCircleDTO>> listCircles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<ShareCircleBO> pageResult = circleDomainService.listCircles(pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                ShareCircleDTOConverter.INSTANCE.convertToDTOList(pageResult.getRecords())));
    }
}
