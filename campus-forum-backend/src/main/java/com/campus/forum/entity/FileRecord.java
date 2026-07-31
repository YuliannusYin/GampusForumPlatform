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
 * 文件记录实体类
 * 对应数据库 file_record 表，记录用户上传的文件信息
 *
 * @author campus
 */
@Data
@TableName("file_record")
public class FileRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上传用户ID */
    private Long userId;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名（UUID + 扩展名） */
    private String storedName;

    /** 文件在磁盘上的存储路径 */
    private String path;

    /** 访问URL */
    private String url;

    /** 文件大小（字节） */
    private Long size;

    /** 文件MIME类型 */
    private String type;

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
