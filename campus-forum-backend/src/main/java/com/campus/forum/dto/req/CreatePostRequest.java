package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发帖请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "发帖请求")
public class CreatePostRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    @Schema(description = "标题（1-100 个字符）", example = "新生求助：选课相关问题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度需在 1-100 个字符之间")
    private String title;

    /** Markdown正文 */
    @Schema(description = "Markdown正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "正文不能为空")
    private String content;

    /** 摘要（可选） */
    @Schema(description = "摘要（可选）", example = "关于下学期选课的几个问题...")
    private String summary;

    /** 板块ID */
    @Schema(description = "板块ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "板块ID不能为空")
    private Long sectionId;

    /** 标签ID列表（可选） */
    @Schema(description = "标签ID列表（可选）", example = "[1, 2]")
    private List<Long> tagIds;

}
