package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户设置视图对象
 * 用于返回当前用户的通知偏好设置
 *
 * @author campus
 */
@Data
@Schema(description = "用户设置视图")
public class UserSettingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论通知开关 1开启 0关闭 */
    @Schema(description = "评论通知开关 1开启 0关闭", example = "1")
    private Integer notifyComment;

    /** 点赞通知开关 1开启 0关闭 */
    @Schema(description = "点赞通知开关 1开启 0关闭", example = "1")
    private Integer notifyLike;

    /** 私信通知开关 1开启 0关闭 */
    @Schema(description = "私信通知开关 1开启 0关闭", example = "1")
    private Integer notifyMessage;

}
