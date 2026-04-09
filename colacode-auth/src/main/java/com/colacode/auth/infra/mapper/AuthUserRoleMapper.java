package com.colacode.auth.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.auth.infra.entity.AuthUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthUserRoleMapper extends BaseMapper<AuthUserRole> {
}
