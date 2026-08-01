package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建/编辑社团请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "创建社团请求")
public class CreateClubRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 社团名称 */
    @Schema(description = "社团名称（1-50 个字符）", example = "编程爱好者协会", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "社团名称不能为空")
    @Size(min = 1, max = 50, message = "社团名称长度需在 1-50 个字符之间")
    private String name;

    /** 社团简介（可选，最长 500） */
    @Schema(description = "社团简介（可选，最长 500 个字符）", example = "致力于推广编程文化的校园社团")
    @Size(max = 500, message = "社团简介最长 500 个字符")
    private String description;

}
