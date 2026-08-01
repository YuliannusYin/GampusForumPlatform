package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户设置请求 DTO
 * 仅更新请求中非空字段
 *
 * @author campus
 */
@Data
@Schema(description = "修改用户设置请求")
public class UpdateSettingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论通知开关 1开启 0关闭（可选） */
    @Schema(description = "评论通知开关 1开启 0关闭", example = "1")
    private Integer notifyComment;

    /** 点赞通知开关 1开启 0关闭（可选） */
    @Schema(description = "点赞通知开关 1开启 0关闭", example = "1")
    private Integer notifyLike;

    /** 私信通知开关 1开启 0关闭（可选） */
    @Schema(description = "私信通知开关 1开启 0关闭", example = "1")
    private Integer notifyMessage;

}
