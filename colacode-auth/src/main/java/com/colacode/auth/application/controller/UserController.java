package com.colacode.auth.application.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.colacode.auth.application.converter.UserDTOConverter;
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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserDomainService userDomainService;
    private final AdminAuthorizationSupport adminAuthorizationSupport;

    public UserController(UserDomainService userDomainService,
                          AdminAuthorizationSupport adminAuthorizationSupport) {
        this.userDomainService = userDomainService;
        this.adminAuthorizationSupport = adminAuthorizationSupport;
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        Long userId = userDomainService.login(loginDTO.getUserName(), loginDTO.getPassword());
        StpUtil.login(userId);
        return Result.success(StpUtil.getTokenValue());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserBO userBO = userDomainService.getUserById(userId);
        return Result.success(UserDTOConverter.INSTANCE.convertToDTO(userBO));
    }

    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody UserDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        UserBO userBO = UserDTOConverter.INSTANCE.convertToBO(userDTO);
        userDomainService.addUser(userBO);
        return Result.success();
    }

    @PostMapping("/update")
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

    @GetMapping("/list")
    public Result<PageResult<UserDTO>> listUsers(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        adminAuthorizationSupport.assertAdminAccess();
        PageResult<UserBO> pageResult = userDomainService.listUsers(pageNo, pageSize);
        return Result.success(new PageResult<>(
                pageResult.getPageNo(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                UserDTOConverter.INSTANCE.convertToDTOList(pageResult.getRecords())));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        UserBO userBO = UserDTOConverter.INSTANCE.convertToBO(registerUserDTO);
        userDomainService.register(userBO);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestBody UserDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        if (userDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "用户ID不能为空");
        }
        userDomainService.deleteUser(StpUtil.getLoginIdAsLong(), userDTO.getId());
        return Result.success();
    }

    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody ChangeUserStatusDTO userDTO) {
        adminAuthorizationSupport.assertAdminAccess();
        userDomainService.changeStatus(StpUtil.getLoginIdAsLong(), userDTO.getId(), userDTO.getStatus());
        return Result.success();
    }

    @PostMapping("/listByIds")
    public Result<List<UserDTO>> listByIds(@RequestBody List<Long> ids) {
        adminAuthorizationSupport.assertAdminAccess();
        List<UserBO> userBOList = userDomainService.listUsersByIds(ids);
        return Result.success(UserDTOConverter.INSTANCE.convertToDTOList(userBOList));
    }

    @GetMapping("/isLogin")
    public Result<Boolean> isLogin() {
        return Result.success(StpUtil.isLogin());
    }
}
