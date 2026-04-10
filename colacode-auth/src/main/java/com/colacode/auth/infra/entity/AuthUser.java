package com.colacode.auth.infra.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * 对应数据库表auth_user，存储用户基本信息
 *
 * @author wxx
 */
@Data
@TableName("auth_user")
public class AuthUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer sex;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态：0-正常，1-禁用
     */
    private Integer status;

    /**
     * 个人简介
     */
    private String introduce;

    /**
     * 扩展JSON字段
     */
    private String extJson;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
