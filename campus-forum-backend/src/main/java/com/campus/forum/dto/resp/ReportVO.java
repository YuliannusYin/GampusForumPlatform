package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 举报表视图（管理端）
 *
 * @author campus
 */
@Data
@Schema(description = "举报信息")
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报ID")
    private Long id;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "举报人用户名")
    private String reporterUsername;

    @Schema(description = "目标类型 1帖子 2评论")
    private Integer targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标摘要（帖子标题或评论内容）")
    private String targetTitle;

    @Schema(description = "目标真实作者用户名（仅管理端）")
    private String targetAuthorUsername;

    @Schema(description = "原因 1垃圾广告 2辱骂骚扰 3色情低俗 4人身攻击 5其他")
    private Integer reason;

    @Schema(description = "补充说明")
    private String description;

    @Schema(description = "状态 0待处理 1属实已处理 2驳回")
    private Integer status;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人用户名")
    private String handlerUsername;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

}
