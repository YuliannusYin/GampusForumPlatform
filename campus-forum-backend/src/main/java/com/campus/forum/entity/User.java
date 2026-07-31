package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * 对应数据库 user 表，存储用户账号、资料、积分等信息
 *
 * @author campus
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密） */
    @TableField("password")
    private String password;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 个人简介 */
    private String bio;

    /** 积分 */
    private Integer points;

    /** 等级 */
    private Integer level;

    /** 状态 0正常 1封禁 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 逻辑删除 0未删除 1已删除 */
    @TableLogic
    private Integer deleted;

}
