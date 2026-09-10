package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员处理举报请求
 *
 * @author campus
 */
@Data
@Schema(description = "处理举报请求")
public class HandleReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 处理结果 1属实已处理 2驳回 */
    @Schema(description = "处理结果 1属实已处理 2驳回", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理结果不能为空")
    @Min(value = 1, message = "处理结果不合法")
    @Max(value = 2, message = "处理结果不合法")
    private Integer status;

    /** 处理备注 */
    @Schema(description = "处理备注（可选）", example = "已删除违规内容")
    @Size(max = 255, message = "处理备注不能超过 255 字")
    private String handleRemark;

}
