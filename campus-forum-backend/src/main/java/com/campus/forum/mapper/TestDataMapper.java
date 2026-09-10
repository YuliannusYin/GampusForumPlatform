package com.campus.forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试数据专用 Mapper
 * 使用原生 SQL 查询（含逻辑删除行）与物理 DELETE，绕过 {@code @TableLogic}，
 * 避免测试数据软删后唯一索引冲突导致无法再次导入
 *
 * @author campus
 */
@Mapper
public interface TestDataMapper {

    /**
     * 统计未删除的测试用户数量
     *
     * @param prefix 用户名前缀（如 test_）
     * @return 未删除测试用户数
     */
    long countActiveTestUsers(@Param("prefix") String prefix);

    /**
     * 查询测试用户 ID（含已逻辑删除）
     *
     * @param prefix 用户名前缀
     * @return 用户 ID 列表
     */
    List<Long> selectTestUserIds(@Param("prefix") String prefix);

    /**
     * 查询未删除的测试用户 ID
     *
     * @param prefix 用户名前缀
     * @return 用户 ID 列表
     */
    List<Long> selectActiveTestUserIds(@Param("prefix") String prefix);

    /**
     * 查询测试社团 ID（含已逻辑删除）
     *
     * @param prefix 社团名前缀
     * @return 社团 ID 列表
     */
    List<Long> selectTestClubIds(@Param("prefix") String prefix);

    /**
     * 查询未删除的测试社团 ID
     *
     * @param prefix 社团名前缀
     * @return 社团 ID 列表
     */
    List<Long> selectActiveTestClubIds(@Param("prefix") String prefix);

    /**
     * 查询测试帖子 ID（含已逻辑删除）
     *
     * @param userIds     测试用户 ID
     * @param titlePrefix 标题前缀
     * @return 帖子 ID 列表
     */
    List<Long> selectTestPostIds(@Param("userIds") List<Long> userIds,
                                 @Param("titlePrefix") String titlePrefix);

    int physicalDeleteClubPostsByClubIds(@Param("ids") List<Long> ids);

    int physicalDeleteClubPostsByPostIds(@Param("ids") List<Long> ids);

    int physicalDeleteClubMembersByClubIds(@Param("ids") List<Long> ids);

    int physicalDeleteClubMembersByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteClubsByIds(@Param("ids") List<Long> ids);

    int physicalDeletePostTagsByPostIds(@Param("ids") List<Long> ids);

    int physicalDeleteCommentsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteLikesByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteFavoritesByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteFollowsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteChatMessagesByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteChatSessionsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteNotificationsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeletePostsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteUserRolesByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteUserSettingsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteSignInRecordsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeletePointsRecordsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteFileRecordsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteReportsByUserIds(@Param("ids") List<Long> ids);

    int physicalDeleteUsersByIds(@Param("ids") List<Long> ids);
}
