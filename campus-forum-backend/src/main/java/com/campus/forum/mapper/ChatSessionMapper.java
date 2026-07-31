package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.dto.resp.ChatSessionVO;
import com.campus.forum.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天会话 Mapper 接口
 * 提供基础的 BaseMapper 方法以及自定义的会话查询
 *
 * @author campus
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据两个用户ID查询会话（约定 user1_id < user2_id）
     *
     * @param u1 较小的用户ID
     * @param u2 较大的用户ID
     * @return 会话实体，不存在返回 null
     */
    @Select("SELECT * FROM chat_session WHERE user1_id = #{u1} AND user2_id = #{u2} AND deleted = 0")
    ChatSession selectByUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    /**
     * 查询当前用户的会话列表（联查 user 与 chat_message）
     * - 通过 CASE 自动识别会话中的"另一用户"
     * - 联查 user 获取另一用户用户名与头像
     * - 联查 chat_message 获取最后消息内容
     * - 子查询统计当前用户在该会话的未读消息数
     * - 按 last_message_time 倒序
     *
     * @param userId 当前用户ID
     * @return 会话视图列表
     */
    @Select("SELECT " +
            "  cs.id AS id, " +
            "  CASE WHEN cs.user1_id = #{userId} THEN cs.user2_id ELSE cs.user1_id END AS otherUserId, " +
            "  u.username AS otherUsername, " +
            "  u.avatar AS otherAvatar, " +
            "  cm.content AS lastMessageContent, " +
            "  cs.last_message_time AS lastMessageTime, " +
            "  (SELECT COUNT(*) FROM chat_message m " +
            "     WHERE m.session_id = cs.id AND m.receiver_id = #{userId} " +
            "       AND m.is_read = 0 AND m.deleted = 0) AS unreadCount " +
            "FROM chat_session cs " +
            "LEFT JOIN user u " +
            "  ON u.id = (CASE WHEN cs.user1_id = #{userId} THEN cs.user2_id ELSE cs.user1_id END) " +
            "  AND u.deleted = 0 " +
            "LEFT JOIN chat_message cm ON cm.id = cs.last_message_id AND cm.deleted = 0 " +
            "WHERE cs.deleted = 0 AND (cs.user1_id = #{userId} OR cs.user2_id = #{userId}) " +
            "ORDER BY cs.last_message_time DESC")
    List<ChatSessionVO> selectSessionList(@Param("userId") Long userId);

}
