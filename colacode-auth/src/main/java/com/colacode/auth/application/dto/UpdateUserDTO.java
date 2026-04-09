package com.colacode.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 32, message = "用户名长度不能超过32个字符")
    private String userName;

    @Size(max = 32, message = "昵称长度不能超过32个字符")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱长度不能超过64个字符")
    private String email;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, max = 64, message = "密码长度需在6到64个字符之间")
    private String password;

    private Integer sex;

    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatar;

    private Integer status;

    @Size(max = 255, message = "个人简介长度不能超过255个字符")
    private String introduce;
}
