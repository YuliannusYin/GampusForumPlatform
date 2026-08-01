package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社团成员视图对象
 * 用于社团成员列表展示
 *
 * @author campus
 */
@Data
@Schema(description = "社团成员信息")
public class ClubMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成员记录ID */
    @Schema(description = "成员记录ID", example = "1")
    private Long id;

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    /** 头像URL */
    @Schema(description = "头像URL", example = "/uploads/avatar/1.png")
    private String avatar;

    /** 角色 0普通成员 1社长 */
    @Schema(description = "角色 0普通成员 1社长", example = "0")
    private Integer role;

    /** 加入时间 */
    @Schema(description = "加入时间", example = "2024-01-01 12:00:00")
    private Date joinedTime;

}
