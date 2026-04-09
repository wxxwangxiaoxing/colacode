package com.colacode.auth.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.auth.config.AdminAuthorizationProperties;
import com.colacode.auth.domain.bo.UserBO;
import com.colacode.auth.domain.converter.UserBOConverter;
import com.colacode.auth.infra.entity.AuthUser;
import com.colacode.auth.infra.mapper.AuthUserMapper;
import com.colacode.auth.support.LoginSecurityService;
import com.colacode.common.PageResult;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserDomainService {

    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthorizationProperties adminAuthorizationProperties;
    private final LoginSecurityService loginSecurityService;

    public UserDomainService(AuthUserMapper authUserMapper,
                             PasswordEncoder passwordEncoder,
                             AdminAuthorizationProperties adminAuthorizationProperties,
                             LoginSecurityService loginSecurityService) {
        this.authUserMapper = authUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.adminAuthorizationProperties = adminAuthorizationProperties;
        this.loginSecurityService = loginSecurityService;
    }

    public UserBO getUserById(Long id) {
        AuthUser user = authUserMapper.selectById(id);
        return UserBOConverter.INSTANCE.convertToBO(user);
    }

    public UserBO getUserByName(String userName) {
        LambdaQueryWrapper<AuthUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthUser::getUserName, userName);
        AuthUser user = authUserMapper.selectOne(wrapper);
        return UserBOConverter.INSTANCE.convertToBO(user);
    }

    public Long login(String userName, String password) {
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "用户名和密码不能为空");
        }

        String loginRiskMessage = loginSecurityService.checkLoginRisk(userName);
        if (loginRiskMessage != null) {
            throw new BusinessException(ResultCodeEnum.TOO_MANY_REQUESTS, loginRiskMessage);
        }

        UserBO userBO = getUserByName(userName);
        if (userBO == null) {
            loginSecurityService.recordLoginFailure(userName);
            throw new BusinessException(ResultCodeEnum.LOGIN_FAILED);
        }
        if (Integer.valueOf(1).equals(userBO.getStatus())) {
            throw new BusinessException(ResultCodeEnum.USER_DISABLED);
        }
        if (!matchesPassword(password, userBO.getPassword())) {
            loginSecurityService.recordLoginFailure(userName);
            throw new BusinessException(ResultCodeEnum.LOGIN_FAILED);
        }

        loginSecurityService.recordLoginSuccess(userName);
        upgradePasswordIfNecessary(userBO.getId(), password, userBO.getPassword());
        return userBO.getId();
    }

    public void register(UserBO userBO) {
        validatePasswordStrength(userBO.getPassword());
        UserBO existUser = getUserByName(userBO.getUserName());
        if (existUser != null) {
            throw new BusinessException(ResultCodeEnum.USERNAME_EXISTS);
        }
        addUser(userBO);
    }

    public void addUser(UserBO userBO) {
        AuthUser entity = UserBOConverter.INSTANCE.convertToEntity(userBO);
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(encodePasswordIfNecessary(entity.getPassword()));
        }
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        authUserMapper.insert(entity);
    }

    public void updateUser(UserBO userBO) {
        if (userBO.getPassword() != null && StringUtils.hasText(userBO.getPassword())) {
            validatePasswordStrength(userBO.getPassword());
        }
        AuthUser entity = UserBOConverter.INSTANCE.convertToEntity(userBO);
        if (entity.getPassword() != null) {
            if (!StringUtils.hasText(entity.getPassword())) {
                entity.setPassword(null);
            } else {
                entity.setPassword(encodePasswordIfNecessary(entity.getPassword()));
            }
        }
        authUserMapper.updateById(entity);
    }

    public PageResult<UserBO> listUsers(int pageNo, int pageSize) {
        Page<AuthUser> page = new Page<>(pageNo, pageSize);
        Page<AuthUser> result = authUserMapper.selectPage(page, null);
        return new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                UserBOConverter.INSTANCE.convertToBOList(result.getRecords()));
    }

    public List<String> getRolesByUserId(Long userId) {
        return authUserMapper.selectRolesByUserId(userId);
    }

    public List<String> getPermissionsByUserId(Long userId) {
        return authUserMapper.selectPermissionsByUserId(userId);
    }

    public void deleteUser(Long operatorUserId, Long targetUserId) {
        assertUserExists(targetUserId);
        if (Objects.equals(operatorUserId, targetUserId)) {
            throw new BusinessException(ResultCodeEnum.CURRENT_USER_DELETE_FORBIDDEN);
        }
        if (isAdminUser(targetUserId) && countAdminUsersExcluding(targetUserId, false) < 1) {
            throw new BusinessException(ResultCodeEnum.LAST_ADMIN_DELETE_FORBIDDEN);
        }
        authUserMapper.deleteById(targetUserId);
    }

    public void changeStatus(Long operatorUserId, Long targetUserId, Integer status) {
        assertUserExists(targetUserId);
        if (Objects.equals(operatorUserId, targetUserId) && Integer.valueOf(1).equals(status)) {
            throw new BusinessException(ResultCodeEnum.CURRENT_USER_DISABLE_FORBIDDEN);
        }
        if (Integer.valueOf(1).equals(status)
                && isAdminUser(targetUserId)
                && countAdminUsersExcluding(targetUserId, true) < 1) {
            throw new BusinessException(ResultCodeEnum.LAST_ADMIN_DISABLE_FORBIDDEN);
        }
        AuthUser user = new AuthUser();
        user.setId(targetUserId);
        user.setStatus(status);
        authUserMapper.updateById(user);
    }

    public List<UserBO> listUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<AuthUser> users = authUserMapper.selectBatchIds(ids);
        return UserBOConverter.INSTANCE.convertToBOList(users);
    }

    public boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    public void upgradePasswordIfNecessary(Long userId, String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword) || isEncodedPassword(storedPassword)) {
            return;
        }
        AuthUser user = new AuthUser();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(rawPassword));
        authUserMapper.updateById(user);
    }

    private void assertUserExists(Long userId) {
        if (userId == null || authUserMapper.selectById(userId) == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
    }

    private boolean isAdminUser(Long userId) {
        List<String> roleKeys = getRolesByUserId(userId);
        for (String roleKey : adminAuthorizationProperties.getRoleKeys()) {
            if (roleKeys.contains(roleKey)) {
                return true;
            }
        }

        List<String> permissionKeys = getPermissionsByUserId(userId);
        for (String permissionKey : adminAuthorizationProperties.getPermissionKeys()) {
            if (permissionKeys.contains(permissionKey)) {
                return true;
            }
        }
        return false;
    }

    private long countAdminUsersExcluding(Long excludeUserId, boolean enabledOnly) {
        return authUserMapper.countAdminUsers(
                adminAuthorizationProperties.getRoleKeys(),
                adminAuthorizationProperties.getPermissionKeys(),
                excludeUserId,
                enabledOnly);
    }

    private String encodePasswordIfNecessary(String password) {
        if (isEncodedPassword(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean isEncodedPassword(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private void validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password)) {
            return;
        }
        if (password.length() < 8) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "密码长度不能少于8位");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "密码必须包含大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "密码必须包含小写字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "密码必须包含数字");
        }
    }
}
