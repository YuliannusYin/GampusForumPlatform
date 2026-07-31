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
 * 板块实体类
 * 对应数据库 section 表，存储论坛板块信息
 *
 * @author campus
 */
@Data
@TableName("section")
public class Section implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 板块ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 板块名 */
    private String name;

    /** 描述 */
    private String description;

    /** 图标URL */
    private String icon;

    /** 排序（升序） */
    private Integer sort;

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
