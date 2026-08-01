package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息视图对象
 * 用于关注/粉丝列表展示
 *
 * @author campus
 */
@Data
@Schema(description = "用户简要信息")
public class UserSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    /** 头像URL */
    @Schema(description = "头像URL", example = "/uploads/avatar/1.png")
    private String avatar;

}
