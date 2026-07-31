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
 * 聊天消息实体类
 * 对应数据库 chat_message 表，存储会话中的每一条消息
 *
 * @author campus
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

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
