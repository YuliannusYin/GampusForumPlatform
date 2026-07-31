package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子置顶修改请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "帖子置顶修改请求")
public class PostTopRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否置顶 0否 1是 */
    @Schema(description = "是否置顶 0否 1是", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "isTop不能为空")
    private Integer isTop;

}
