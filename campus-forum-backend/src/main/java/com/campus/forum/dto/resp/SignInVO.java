package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 签到结果视图对象
 *
 * @author campus
 */
@Data
@Schema(description = "签到结果")
public class SignInVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 签到日期 */
    @Schema(description = "签到日期", example = "2026-07-31")
    private LocalDate signDate;

    /** 连续签到天数 */
    @Schema(description = "连续签到天数", example = "7")
    private Integer continuousDays;

    /** 本次签到获得积分 */
    @Schema(description = "本次签到获得积分", example = "15")
    private Integer points;

    /** 用户当前总积分 */
    @Schema(description = "用户当前总积分", example = "120")
    private Integer totalPoints;

    /** 用户当前等级 */
    @Schema(description = "用户当前等级", example = "2")
    private Integer level;

}
