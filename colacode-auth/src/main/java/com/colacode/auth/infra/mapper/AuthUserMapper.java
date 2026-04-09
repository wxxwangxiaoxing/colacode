package com.colacode.auth.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.auth.infra.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUser> {

    @Select("SELECT r.role_key FROM auth_role r " +
            "INNER JOIN auth_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<String> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT p.permission_key FROM auth_permission p " +
            "INNER JOIN auth_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN auth_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.is_deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT COUNT(DISTINCT u.id)",
            "FROM auth_user u",
            "WHERE u.is_deleted = 0",
            "<if test='excludeUserId != null'>AND u.id != #{excludeUserId}</if>",
            "<if test='enabledOnly'>AND u.status = 0</if>",
            "AND (",
            "<trim prefixOverrides='OR'>",
            "<if test='roleKeys != null and roleKeys.size() > 0'>",
            "OR EXISTS (",
            "SELECT 1 FROM auth_user_role ur",
            "INNER JOIN auth_role r ON r.id = ur.role_id AND r.is_deleted = 0",
            "WHERE ur.user_id = u.id",
            "AND r.role_key IN",
            "<foreach collection='roleKeys' item='roleKey' open='(' separator=',' close=')'>#{roleKey}</foreach>",
            ")",
            "</if>",
            "<if test='permissionKeys != null and permissionKeys.size() > 0'>",
            "OR EXISTS (",
            "SELECT 1 FROM auth_user_role ur",
            "INNER JOIN auth_role_permission rp ON rp.role_id = ur.role_id",
            "INNER JOIN auth_permission p ON p.id = rp.permission_id AND p.is_deleted = 0",
            "WHERE ur.user_id = u.id",
            "AND p.permission_key IN",
            "<foreach collection='permissionKeys' item='permissionKey' open='(' separator=',' close=')'>#{permissionKey}</foreach>",
            ")",
            "</if>",
            "</trim>",
            ")",
            "</script>"
    })
    long countAdminUsers(@Param("roleKeys") List<String> roleKeys,
                         @Param("permissionKeys") List<String> permissionKeys,
                         @Param("excludeUserId") Long excludeUserId,
                         @Param("enabledOnly") boolean enabledOnly);
}
