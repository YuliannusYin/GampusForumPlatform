package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 社团简要信息视图对象
 * 用于用户主页展示已加入的社团
 *
 * @author campus
 */
@Data
@Schema(description = "社团简要信息")
public class ClubSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 社团ID */
    @Schema(description = "社团ID", example = "1")
    private Long id;

    /** 社团名称 */
    @Schema(description = "社团名称", example = "编程爱好者协会")
    private String name;

    /** 社团Logo URL */
    @Schema(description = "社团Logo URL", example = "/uploads/club/1.png")
    private String logo;

    /** 社团简介 */
    @Schema(description = "社团简介", example = "致力于推广编程文化的校园社团")
    private String description;

    /** 成员数 */
    @Schema(description = "成员数", example = "50")
    private Integer memberCount;

}
