package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签创建/修改请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "标签创建/修改请求")
public class TagRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签名 */
    @Schema(description = "标签名", example = "求助", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名长度不能超过 50 个字符")
    private String name;

}
