package com.campus.forum.utils;

import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Section;

import java.util.List;

/**
 * 表白墙匿名脱敏工具
 * 公开接口出口统一走此方法，避免仅前端藏名字导致抓包泄露作者
 *
 * @author campus
 */
public final class PostAnonymityHelper {

    /** 表白墙板块编码 */
    public static final String CONFESSION_SECTION_CODE = "confession";

    /** 匿名标记 */
    public static final int ANONYMOUS_YES = 1;

    private PostAnonymityHelper() {
    }

    /**
     * 判断板块是否为表白墙
     *
     * @param section 板块
     * @return true 表示为表白墙
     */
    public static boolean isConfessionSection(Section section) {
        return section != null && CONFESSION_SECTION_CODE.equals(section.getCode());
    }

    /**
     * 计算发帖/改帖后的匿名标记
     * 已匿名的帖不可改回实名；发到或改到表白墙则强制匿名
     *
     * @param targetSection     目标板块
     * @param existingAnonymous 原匿名标记（新建帖传 null）
     * @return 1 匿名 / 0 实名
     */
    public static int resolveAnonymousFlag(Section targetSection, Integer existingAnonymous) {
        if (existingAnonymous != null && existingAnonymous == ANONYMOUS_YES) {
            return ANONYMOUS_YES;
        }
        return isConfessionSection(targetSection) ? ANONYMOUS_YES : 0;
    }

    /**
     * 对公开帖子 VO 脱敏：匿名帖清空 userId/username/userAvatar，并标记 isAuthor
     *
     * @param vo            帖子视图
     * @param currentUserId 当前登录用户ID，未登录为 null
     */
    public static void applyPublicView(PostListVO vo, Long currentUserId) {
        if (vo == null) {
            return;
        }
        boolean isAuthor = currentUserId != null && currentUserId.equals(vo.getUserId());
        vo.setIsAuthor(isAuthor);
        if (vo.getIsAnonymous() != null && vo.getIsAnonymous() == ANONYMOUS_YES) {
            vo.setUserId(null);
            vo.setUsername(null);
            vo.setUserAvatar(null);
        }
    }

    /**
     * 批量对公开帖子列表脱敏
     *
     * @param records       帖子列表
     * @param currentUserId 当前登录用户ID，未登录为 null
     */
    public static void applyPublicView(List<? extends PostListVO> records, Long currentUserId) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (PostListVO vo : records) {
            applyPublicView(vo, currentUserId);
        }
    }

}
