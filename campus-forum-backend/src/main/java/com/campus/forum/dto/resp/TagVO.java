package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签视图对象
 *
 * @author campus
 */
@Data
@Schema(description = "标签信息")
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标签ID */
    @Schema(description = "标签ID", example = "1")
    private Long id;

    /** 标签名 */
    @Schema(description = "标签名", example = "分享")
    private String name;

    /** 该标签下的帖子数 */
    @Schema(description = "该标签下的帖子数", example = "10")
    private Long postCount;

}
