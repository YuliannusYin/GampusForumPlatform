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
 * 评论实体类
 * 对应数据库 comment 表，存储帖子评论与回复（parent_id=0 为顶级评论，否则为回复）
 *
 * @author campus
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 评论者ID */
    private Long userId;

    /** 父评论ID，0表示顶级评论 */
    private Long parentId;

    /** Markdown内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态 0正常 1已删除 */
    private Integer status;

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
