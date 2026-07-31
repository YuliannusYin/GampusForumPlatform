package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.NotificationVO;
import com.campus.forum.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知 Mapper 接口
 * 提供基础的 BaseMapper 方法以及自定义的联表查询（实际 SQL 在 NotificationMapper.xml 中实现）
 *
 * @author campus
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询通知列表（联表 user 获取发送者用户名和头像）
     * 按 create_time 倒序，仅查询当前用户的通知
     *
     * @param page   分页对象
     * @param userId 接收者用户ID
     * @param type   通知类型（可选，为 null 时不限制）
     * @param isRead 是否已读（可选，为 null 时不限制）
     * @return 分页结果
     */
    IPage<NotificationVO> selectNotificationList(IPage<NotificationVO> page,
                                                  @Param("userId") Long userId,
                                                  @Param("type") Integer type,
                                                  @Param("isRead") Integer isRead);

}
