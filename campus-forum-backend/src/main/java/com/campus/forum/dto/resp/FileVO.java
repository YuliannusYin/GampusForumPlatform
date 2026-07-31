package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传响应视图对象
 *
 * @author campus
 */
@Data
@Schema(description = "文件上传响应")
public class FileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问URL */
    @Schema(description = "访问URL", example = "/uploads/2024/01/01/a1b2c3d4.jpg")
    private String url;

    /** 原始文件名 */
    @Schema(description = "原始文件名", example = "avatar.jpg")
    private String originalName;

    /** 文件大小（字节） */
    @Schema(description = "文件大小（字节）", example = "102400")
    private Long size;

    /** 文件MIME类型 */
    @Schema(description = "文件MIME类型", example = "image/jpeg")
    private String type;

}
