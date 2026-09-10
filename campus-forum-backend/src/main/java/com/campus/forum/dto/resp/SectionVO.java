package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 板块视图对象
 * 包含板块基本信息以及该板块下的帖子数
 *
 * @author campus
 */
@Data
@Schema(description = "板块信息")
public class SectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 板块ID */
    @Schema(description = "板块ID", example = "1")
    private Long id;

    /** 板块名 */
    @Schema(description = "板块名", example = "校园生活")
    private String name;

    /** 板块编码（confession=表白墙） */
    @Schema(description = "板块编码", example = "confession")
    private String code;

    /** 描述 */
    @Schema(description = "描述", example = "分享校园日常、活动资讯、生活经验")
    private String description;

    /** 图标URL */
    @Schema(description = "图标URL", example = "/uploads/section/1.png")
    private String icon;

    /** 排序（升序） */
    @Schema(description = "排序值（升序）", example = "1")
    private Integer sort;

    /** 帖子数 */
    @Schema(description = "该板块下的帖子数", example = "10")
    private Long postCount;

}
