package com.campus.forum.service;

/**
 * 积分服务接口
 * 提供积分变更与等级计算的公共能力，供签到、发帖、评论、点赞等模块调用
 *
 * @author campus
 */
public interface PointsService {

    /**
     * 增加用户积分并更新等级
     * 保存积分记录、原子更新用户积分、根据总积分重新计算等级
     *
     * @param userId      用户ID
     * @param value       积分变化值（正数为增加，负数为扣减）
     * @param type        积分类型 1签到 2发帖 3评论 4点赞被赞 5管理员调整
     * @param description 变更描述
     */
    void addPoints(Long userId, int value, int type, String description);

    /**
     * 增加用户积分并返回是否升级
     * 与 addPoints 功能一致，额外返回用户等级是否提升
     *
     * @param userId      用户ID
     * @param value       积分变化值（正数为增加，负数为扣减）
     * @param type        积分类型 1签到 2发帖 3评论 4点赞被赞 5管理员调整
     * @param description 变更描述
     * @return true 表示用户等级已提升，false 表示未提升
     */
    boolean addPointsWithLevelCheck(Long userId, int value, int type, String description);

}
