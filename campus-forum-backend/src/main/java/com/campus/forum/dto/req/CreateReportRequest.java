package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交举报请求
 *
 * @author campus
 */
@Data
@Schema(description = "提交举报请求")
public class CreateReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标类型 1帖子 2评论 */
    @Schema(description = "目标类型 1帖子 2评论", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标类型不能为空")
    @Min(value = 1, message = "目标类型不合法")
    @Max(value = 2, message = "目标类型不合法")
    private Integer targetType;

    /** 目标ID */
    @Schema(description = "目标ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /** 原因 1垃圾广告 2辱骂骚扰 3色情低俗 4人身攻击 5其他 */
    @Schema(description = "原因 1垃圾广告 2辱骂骚扰 3色情低俗 4人身攻击 5其他", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "举报原因不能为空")
    @Min(value = 1, message = "举报原因不合法")
    @Max(value = 5, message = "举报原因不合法")
    private Integer reason;

    /** 补充说明 */
    @Schema(description = "补充说明（可选）", example = "内容含人身攻击")
    @Size(max = 500, message = "补充说明不能超过 500 字")
    private String description;

}
