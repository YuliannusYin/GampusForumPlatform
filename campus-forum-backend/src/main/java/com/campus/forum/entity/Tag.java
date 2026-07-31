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
 * 标签实体类
 * 对应数据库 tag 表，存储帖子标签信息（name 唯一）
 *
 * @author campus
 */
@Data
@TableName("tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名（唯一） */
    private String name;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 逻辑删除 0未删除 1已删除 */
    @TableLogic
    private Integer deleted;

}
