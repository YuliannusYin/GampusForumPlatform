package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子列表视图对象
 * 用于帖子分页列表展示，不包含正文 content
 *
 * @author campus
 */
@Data
@Schema(description = "帖子列表项")
public class PostListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    @Schema(description = "帖子ID", example = "1")
    private Long id;

    /** 标题 */
    @Schema(description = "标题", example = "新生求助：选课相关问题")
    private String title;

    /** 摘要 */
    @Schema(description = "摘要", example = "关于下学期选课的几个问题...")
    private String summary;

    /** 作者ID */
    @Schema(description = "作者ID", example = "1")
    private Long userId;

    /** 作者用户名 */
    @Schema(description = "作者用户名", example = "admin")
    private String username;

    /** 作者头像URL */
    @Schema(description = "作者头像URL", example = "/uploads/avatar/1.png")
    private String userAvatar;

    /** 板块ID */
    @Schema(description = "板块ID", example = "1")
    private Long sectionId;

    /** 板块名 */
    @Schema(description = "板块名", example = "校园生活")
    private String sectionName;

    /** 标签列表 */
    @Schema(description = "标签列表")
    private List<TagVO> tags;

    /** 浏览数 */
    @Schema(description = "浏览数", example = "100")
    private Integer viewCount;

    /** 点赞数 */
    @Schema(description = "点赞数", example = "20")
    private Integer likeCount;

    /** 评论数 */
    @Schema(description = "评论数", example = "5")
    private Integer commentCount;

    /** 收藏数 */
    @Schema(description = "收藏数", example = "3")
    private Integer favoriteCount;

    /** 是否置顶 0否 1是 */
    @Schema(description = "是否置顶 0否 1是", example = "0")
    private Integer isTop;

    /** 是否精华 0否 1是 */
    @Schema(description = "是否精华 0否 1是", example = "0")
    private Integer isEssence;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
