package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社团视图对象
 * 用于社团列表与详情展示
 *
 * @author campus
 */
@Data
@Schema(description = "社团信息")
public class ClubVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 社团ID */
    @Schema(description = "社团ID", example = "1")
    private Long id;

    /** 社团名称 */
    @Schema(description = "社团名称", example = "编程爱好者协会")
    private String name;

    /** 社团简介 */
    @Schema(description = "社团简介", example = "致力于推广编程文化的校园社团")
    private String description;

    /** 社团Logo URL */
    @Schema(description = "社团Logo URL", example = "/uploads/club/1.png")
    private String logo;

    /** 创建者用户ID */
    @Schema(description = "创建者用户ID", example = "1")
    private Long creatorId;

    /** 创建者用户名 */
    @Schema(description = "创建者用户名", example = "admin")
    private String creatorName;

    /** 状态 0待审核 1正常 2禁用 */
    @Schema(description = "状态 0待审核 1正常 2禁用", example = "1")
    private Integer status;

    /** 成员数 */
    @Schema(description = "成员数", example = "50")
    private Integer memberCount;

    /** 帖子数 */
    @Schema(description = "帖子数", example = "20")
    private Integer postCount;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
