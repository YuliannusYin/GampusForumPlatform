package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子详情视图对象
 * 继承 {@link PostListVO} 的所有字段，并增加正文 content 与更新时间 updateTime
 *
 * @author campus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "帖子详情")
public class PostDetailVO extends PostListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Markdown正文 */
    @Schema(description = "Markdown正文", example = "## 标题\n正文内容...")
    private String content;

    /** 更新时间 */
    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    private Date updateTime;

}
