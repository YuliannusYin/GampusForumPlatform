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
 * 收藏实体类（完整版）
 * 对应数据库 favorite 表，记录用户对帖子的收藏行为
 * 唯一索引：(user_id, post_id)，逻辑删除后再次收藏通过切换 deleted 字段恢复
 *
 * @author campus
 */
@Data
@TableName("favorite")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 帖子ID */
    private Long postId;

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
