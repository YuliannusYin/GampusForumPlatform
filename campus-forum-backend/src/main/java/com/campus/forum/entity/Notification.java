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
 * 通知实体类
 * 对应数据库 notification 表，存储用户接收到的各类通知（评论、点赞、私信、系统）
 *
 * @author campus
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者ID */
    private Long userId;

    /** 发送者ID（系统通知为NULL） */
    private Long fromUserId;

    /** 类型 1评论 2点赞 3私信 4系统 */
    private Integer type;

    /** 通知内容 */
    private String content;

    /** 目标对象ID */
    private Long targetId;

    /** 目标类型 */
    private Integer targetType;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

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
