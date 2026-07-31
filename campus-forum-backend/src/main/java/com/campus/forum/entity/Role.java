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
 * 角色实体类
 * 对应数据库 role 表，存储角色编码、名称等信息
 *
 * @author campus
 */
@Data
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码（如 ROLE_USER / ROLE_ADMIN） */
    private String code;

    /** 角色名 */
    private String name;

    /** 描述 */
    private String description;

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
