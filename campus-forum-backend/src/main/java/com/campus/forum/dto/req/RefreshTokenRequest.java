package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * Token 刷新请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "Token 刷新请求")
public class RefreshTokenRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 刷新令牌 */
    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;

}
