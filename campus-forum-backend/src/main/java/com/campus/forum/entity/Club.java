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
 * 社团实体类
 *
 * @author campus
 */
@Data
@TableName("club")
public class Club implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 社团ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社团名称 */
    private String name;

    /** 社团简介 */
    private String description;

    /** 社团Logo URL */
    private String logo;

    /** 创建者用户ID */
    private Long creatorId;

    /** 状态 0待审核 1正常 2禁用 */
    private Integer status;

    /** 成员数 */
    private Integer memberCount;

    /** 帖子数 */
    private Integer postCount;

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
