package com.colacode.auth.application.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.colacode.auth.application.converter.UserDTOConverter;
import com.colacode.auth.application.dto.ChangePasswordDTO;
import com.colacode.auth.application.dto.ChangeUserStatusDTO;
import com.colacode.auth.application.dto.LoginDTO;
import com.colacode.auth.application.dto.RegisterUserDTO;
import com.colacode.auth.application.dto.UpdateUserDTO;
import com.colacode.auth.application.dto.UserDTO;
import com.colacode.auth.domain.bo.UserBO;
import com.colacode.auth.domain.service.UserDomainService;
import com.colacode.auth.support.AdminAuthorizationSupport;
import com.colacode.common.PageResult;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器
 * 提供用户登录、注册、信息管理等RESTful接口
 *
 * @author wxx
 */
@RestController
@RequestMapping("/auth/user")
@Tag(name = "用户管理", description = "用户登录、注册、信息管理等")
public class UserController {

    private final UserDomainService userDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public UserController(UserDomainService userDomainService,
                          AdminAuthorizationSupport adminAuthorizationSupport) {
        this.userDomainService = userDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    /**
     * 用户登录接口
     * 根据用户名密码进行登录认证，返回token
     *
     * @param loginDTO 登录请求DTO，包含用户名和密码
     * @return 登录成功返回token
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名密码登录，返回token")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        Long userId = userDomainService.login(loginDTO.getUserName(), loginDTO.getPassword());
        StpUtil.login(userId);
        return Result.success(StpUtil.getTokenValue());
    }

    /**
     * 用户登出接口
     * 退出当前登录状态
     *
     * @return 登出成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录状态")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息接口
     * 获取已登录用户的信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取登录用户的信息")
    public Result<UserDTO> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserBO userBO = userDomainService.getUserById(userId);
        return Result.success(UserDTOConverter.INSTANCE.convertToDTO(userBO));
    }

    /**
     * 根据用户ID获取用户信息接口
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户信息")
    public Result<UserDTO> getUserById(@Parameter(description = "用户ID") @PathVariable Long userId) {
        adminAuthorizationSupport.assertCanAccessUser(userId);
        UserBO userBO = userDomainService.getUserById(userId);
        return Result.success(UserDTOConverter.INSTANCE.convertToDTO(userBO));
    }

    /**
     * 新增用户接口
     * 管理员添加新用户
     *
     * @param userDTO 用户信息
     * @return 添加成功
     */
    @PostMapping("/add")
    @Operation(summary = "新增用户", description = "管理员添加新用户")
    public Result<Void> addUser(@RequestBody UserDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        UserBO userBO = UserDTOConverter.INSTANCE.convertToBO(userDTO);
        userDomainService.addUser(userBO);
        return Result.success();
    }

    /**
     * 更新用户信息接口
     * 更新用户基本信息
     *
     * @param userDTO 用户信息
     * @return 更新成功
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户信息")
    public Result<Void> updateUser(@Valid @RequestBody UpdateUserDTO userDTO) {
        if (userDTO.getId() == null) {
            if (!StpUtil.isLogin()) {
                throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
            }
            userDTO.setId(StpUtil.getLoginIdAsLong());
        }
        adminAuthorizationSupport.assertCanAccessUser(userDTO.getId());
        UserBO userBO = UserDTOConverter.INSTANCE.convertToBO(userDTO);
        userDomainService.updateUser(userBO);
        return Result.success();
    }

    /**
     * 修改密码接口
     * 修改当前用户密码
     *
     * @param changePasswordDTO 密码修改请求DTO
     * @return 修改成功
     */
    @PostMapping("/changePassword")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        userDomainService.changePassword(userId, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword(), changePasswordDTO.getConfirmPassword());
        return Result.success();
    }

    /**
     * 获取用户列表接口
     * 分页获取用户列表
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 用户分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    public Result<PageResult<UserDTO>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        adminAuthorizationSupport.assertAdminAccess();
        PageResult<UserBO> pageResult = userDomainService.listUsers(pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                UserDTOConverter.INSTANCE.convertToDTOList(pageResult.getRecords())));
    }

    /**
     * 用户注册接口
     * 新用户注册
     *
     * @param registerUserDTO 注册信息
     * @return 注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        UserBO userBO = UserDTOConverter.INSTANCE.convertToBO(registerUserDTO);
        userDomainService.register(userBO);
        return Result.success();
    }

    /**
     * 删除用户接口
     * 管理员删除指定用户
     *
     * @param userDTO 用户信息，包含待删除用户ID
     * @return 删除成功
     */
    @PostMapping("/delete")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    public Result<Void> deleteUser(@RequestBody UserDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (userDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "用户ID不能为空");
        }
        userDomainService.deleteUser(StpUtil.getLoginIdAsLong(), userDTO.getId());
        return Result.success();
    }

    /**
     * 修改用户状态接口
     * 启用或禁用指定用户
     *
     * @param userDTO 用户状态修改请求
     * @return 修改成功
     */
    @PostMapping("/changeStatus")
    @Operation(summary = "修改用户状态", description = "启用/禁用用户")
    public Result<Void> changeStatus(@Valid @RequestBody ChangeUserStatusDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        userDomainService.changeStatus(StpUtil.getLoginIdAsLong(), userDTO.getId(), userDTO.getStatus());
        return Result.success();
    }

    /**
     * 批量获取用户接口
     * 根据用户ID列表批量获取用户信息
     *
     * @param ids 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("/listByIds")
    @Operation(summary = "批量获取用户", description = "根据ID列表批量获取用户信息")
    public Result<List<UserDTO>> listByIds(@Parameter(description = "用户ID列表") @RequestBody List<Long> ids) {
        adminAuthorizationSupport.assertAdminAccess();
        List<UserBO> userBOList = userDomainService.listUsersByIds(ids);
        return Result.success(UserDTOConverter.INSTANCE.convertToDTOList(userBOList));
    }

    /**
     * 检查登录状态接口
     * 检查当前是否已登录
     *
     * @return 登录状态
     */
    @GetMapping("/isLogin")
    @Operation(summary = "检查登录状态", description = "检查当前是否已登录")
    public Result<Boolean> isLogin() {
        return Result.success(StpUtil.isLogin());
    }
}
