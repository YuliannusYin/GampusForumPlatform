package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 签到状态视图对象
 *
 * @author campus
 */
@Data
@Schema(description = "签到状态")
public class SignInStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日是否已签到 */
    @Schema(description = "今日是否已签到", example = "false")
    private Boolean signedToday;

    /** 连续签到天数 */
    @Schema(description = "连续签到天数", example = "6")
    private Integer continuousDays;

    /** 用户当前总积分 */
    @Schema(description = "用户当前总积分", example = "105")
    private Integer totalPoints;

    /** 用户当前等级 */
    @Schema(description = "用户当前等级", example = "2")
    private Integer level;

}
