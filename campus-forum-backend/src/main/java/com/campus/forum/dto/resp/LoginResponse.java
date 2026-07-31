package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    @Schema(description = "访问令牌（accessToken）")
    private String accessToken;

    /** 刷新令牌 */
    @Schema(description = "刷新令牌（refreshToken）")
    private String refreshToken;

    /** 用户信息 */
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, UserInfoVO userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userInfo = userInfo;
    }

}
