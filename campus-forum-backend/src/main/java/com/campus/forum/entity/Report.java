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
 * 举报实体
 * 对应 report 表，记录对表白墙帖子/评论的举报与处理结果
 *
 * @author campus
 */
@Data
@TableName("report")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    private Long reporterId;

    /** 目标类型 1帖子 2评论 */
    private Integer targetType;

    /** 目标ID */
    private Long targetId;

    /** 原因 1垃圾广告 2辱骂骚扰 3色情低俗 4人身攻击 5其他 */
    private Integer reason;

    /** 补充说明 */
    private String description;

    /** 状态 0待处理 1属实已处理 2驳回 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

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
