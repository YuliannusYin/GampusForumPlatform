package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改个人资料请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "修改个人资料请求")
public class UpdateProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 昵称 */
    @Schema(description = "昵称", example = "Campus 小明")
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /** 个人简介 */
    @Schema(description = "个人简介", example = "热爱分享的校园博主")
    @Size(max = 255, message = "个人简介长度不能超过 255 个字符")
    private String bio;

    /** 头像URL */
    @Schema(description = "头像URL", example = "/uploads/avatar/1.png")
    @Size(max = 255, message = "头像URL长度不能超过 255 个字符")
    private String avatar;

    /** 性别 0未知 1男 2女 */
    @Schema(description = "性别 0未知 1男 2女", example = "1")
    private Integer gender;

}
