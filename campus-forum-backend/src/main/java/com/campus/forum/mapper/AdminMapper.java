package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.PostListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员统计与查询 Mapper 接口
 * 集中存放后台管理相关的统计与列表查询（不含对应实体，纯自定义 SQL）
 *
 * @author campus
 */
@Mapper
public interface AdminMapper {

    /**
     * 分页查询所有帖子（含已删除/草稿，供管理员查看）
     * 联表 user 与 section，不限制 status 与 deleted，以便管理员查看全部帖子
     * 实际 SQL 在 AdminMapper.xml 中实现
     *
     * @param page      分页对象
     * @param keyword   标题关键词（可选）
     * @param sectionId 板块ID（可选）
     * @param status    帖子状态（可选，0已发布 1草稿 2已删除）
     * @return 分页结果
     */
    IPage<PostListVO> selectAdminPostList(IPage<PostListVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("sectionId") Long sectionId,
                                          @Param("status") Integer status);

    /**
     * 统计用户总数（未删除）
     *
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    Long countTotalUsers();

    /**
     * 统计已发布帖子总数（status=0 且未删除）
     *
     * @return 已发布帖子总数
     */
    @Select("SELECT COUNT(*) FROM post WHERE status = 0 AND deleted = 0")
    Long countTotalPosts();

    /**
     * 统计评论总数（status=0 且未删除）
     * comment 表暂无对应实体，直接用原生 SQL 统计
     *
     * @return 评论总数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE status = 0 AND deleted = 0")
    Long countTotalComments();

    /**
     * 统计今日新增用户数
     *
     * @return 今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    Long countTodayNewUsers();

    /**
     * 统计今日新增帖子数
     *
     * @return 今日新增帖子数
     */
    @Select("SELECT COUNT(*) FROM post WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    Long countTodayNewPosts();

    /**
     * 查询近 N 天发帖趋势
     * 按日期分组统计发帖数，仅返回有帖子的日期
     *
     * @param startTime 起始时间
     * @return 每日发帖统计列表，每项 {date: "yyyy-MM-dd", count: 数量}
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM post WHERE create_time >= #{startTime} AND deleted = 0 " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> selectPostTrend(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询板块帖子分布
     * 联查 post 与 section，统计每个板块下已发布且未删除的帖子数
     * 使用 snake_case 别名，由 mapUnderscoreToCamelCase 自动转为 camelCase 键名
     *
     * @return 板块分布列表，每项 {sectionId, sectionName, count}
     */
    @Select("SELECT p.section_id AS section_id, s.name AS section_name, COUNT(*) AS count " +
            "FROM post p LEFT JOIN section s ON p.section_id = s.id " +
            "WHERE p.deleted = 0 AND p.status = 0 " +
            "GROUP BY p.section_id, s.name ORDER BY count DESC")
    List<Map<String, Object>> selectSectionDistribution();

}
