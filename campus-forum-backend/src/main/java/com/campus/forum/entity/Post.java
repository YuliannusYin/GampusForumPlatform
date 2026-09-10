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
 * 帖子实体类（完整版）
 * 对应数据库 post 表，存储帖子标题、正文、作者、板块、统计字段及状态等信息
 *
 * @author campus
 */
@Data
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** Markdown正文 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 作者ID */
    private Long userId;

    /** 板块ID */
    private Long sectionId;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 是否置顶 0否 1是 */
    private Integer isTop;

    /** 是否精华 0否 1是 */
    private Integer isEssence;

    /** 是否匿名 0否 1是 */
    private Integer isAnonymous;

    /** 状态 0已发布 1草稿 2已删除 */
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
