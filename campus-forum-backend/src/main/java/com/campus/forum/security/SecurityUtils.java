package com.campus.forum.security;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 * 提供静态方法从 SecurityContext 获取当前登录用户信息，便于各模块复用
 *
 * @author campus
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户详情
     *
     * @return 当前登录的 LoginUserDetails，未登录抛出业务异常
     */
    public static LoginUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserDetails userDetails) {
            return userDetails;
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID，未登录抛出业务异常
     */
    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

}
