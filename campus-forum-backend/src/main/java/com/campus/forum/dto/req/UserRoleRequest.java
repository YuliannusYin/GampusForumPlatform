package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色修改请求 DTO
 * 传入新的角色ID列表，将覆盖用户原有角色
 *
 * @author campus
 */
@Data
@Schema(description = "用户角色修改请求")
public class UserRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID列表 */
    @Schema(description = "角色ID列表", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;

}
