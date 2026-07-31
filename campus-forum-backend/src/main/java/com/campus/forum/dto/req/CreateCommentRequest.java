package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发表评论/回复请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "发表评论/回复请求")
public class CreateCommentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论内容 */
    @Schema(description = "评论内容（1-2000 个字符）", example = "写得很不错，学到很多！", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 2000, message = "评论内容长度需在 1-2000 个字符之间")
    private String content;

    /** 父评论ID（可选，0或空表示顶级评论） */
    @Schema(description = "父评论ID（可选，0或空表示顶级评论）", example = "0")
    private Long parentId;

}
