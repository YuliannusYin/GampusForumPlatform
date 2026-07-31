package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.ChatMessageVO;
import com.campus.forum.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 聊天消息 Mapper 接口
 * 提供基础的 BaseMapper 方法以及自定义的消息分页查询与已读标记
 *
 * @author campus
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 分页查询会话历史消息（联查 user 获取发送者用户名与头像）
     * 按 create_time 正序（先旧后新）
     *
     * @param page      分页对象
     * @param sessionId 会话ID
     * @return 分页结果
     */
    @Select("SELECT " +
            "  m.id AS id, " +
            "  m.session_id AS sessionId, " +
            "  m.sender_id AS senderId, " +
            "  u.username AS senderUsername, " +
            "  u.avatar AS senderAvatar, " +
            "  m.receiver_id AS receiverId, " +
            "  m.content AS content, " +
            "  m.is_read AS isRead, " +
            "  m.create_time AS createTime " +
            "FROM chat_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id AND u.deleted = 0 " +
            "WHERE m.deleted = 0 AND m.session_id = #{sessionId} " +
            "ORDER BY m.create_time ASC")
    IPage<ChatMessageVO> selectMessagePage(IPage<ChatMessageVO> page,
                                            @Param("sessionId") Long sessionId);

    /**
     * 标记某会话中发送给当前用户的所有未读消息为已读
     * SQL 语义：UPDATE chat_message SET is_read=1 WHERE session_id=? AND receiver_id=? AND is_read=0
     *
     * @param sessionId 会话ID
     * @param receiverId 接收者ID（当前用户）
     * @return 受影响行数
     */
    @Update("UPDATE chat_message SET is_read = 1 " +
            "WHERE session_id = #{sessionId} AND receiver_id = #{receiverId} " +
            "AND is_read = 0 AND deleted = 0")
    int markRead(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);

}
