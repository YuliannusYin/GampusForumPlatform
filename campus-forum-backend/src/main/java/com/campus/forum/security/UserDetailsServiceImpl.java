package com.campus.forum.security;

import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsService 实现类
 * 通过 UserService 按账号（用户名或邮箱）查询用户，并装配角色权限构造 LoginUserDetails。
 * 该 Bean 会覆盖 SecurityConfig 中 @ConditionalOnMissingBean 的默认占位实现
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /** 用户服务，用于查询用户与角色 */
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // getByAccount 支持用户名或邮箱查询
        User user = userService.getByAccount(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 查询用户角色列表，转为 GrantedAuthority（角色 code 前加 "ROLE_"）
        List<Role> roles = userService.getUserRoles(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .toList();

        // enabled = status == 0（0正常 1封禁）
        boolean enabled = user.getStatus() == null || user.getStatus() == 0;

        return new LoginUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                enabled
        );
    }

}
