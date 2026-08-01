package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.CreateClubRequest;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.ClubMemberVO;
import com.campus.forum.dto.resp.ClubVO;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.ClubMember;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社团接口
 * 提供社团列表、详情、创建、加入/退出、社团发帖、成员管理等功能
 * GET 接口对游客可见，写操作需登录
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Validated
@Tag(name = "社团接口", description = "社团列表、详情、创建、加入/退出、发帖、成员管理")
public class ClubController {

    /** 管理员角色编码 */
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ClubService clubService;
    private final UserMapper userMapper;

    /**
     * 社团分页列表（对游客可见）
     *
     * @param page    当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 社团名称关键词（可选）
     * @param status  社团状态（可选，为空时仅查正常社团）
     * @return 社团分页结果
     */
    @Operation(summary = "社团分页列表", description = "对游客可见，status 为空时仅返回正常社团，按创建时间倒序")
    @GetMapping("")
    public Result<PageResult<ClubVO>> listClubs(@Parameter(description = "当前页码，默认 1")
                                                 @RequestParam(value = "page", defaultValue = "1") long page,
                                                 @Parameter(description = "每页条数，默认 10")
                                                 @RequestParam(value = "size", defaultValue = "10") long size,
                                                 @Parameter(description = "社团名称关键词（可选）")
                                                 @RequestParam(value = "keyword", required = false) String keyword,
                                                 @Parameter(description = "社团状态（可选，0待审核 1正常 2禁用）")
                                                 @RequestParam(value = "status", required = false) Integer status) {
        PageResult<ClubVO> result = clubService.listClubs(page, size, keyword, status);
        return Result.success(result);
    }

    /**
     * 社团详情（对游客可见）
     *
     * @param clubId 社团ID
     * @return 社团详情
     */
    @Operation(summary = "社团详情", description = "对游客可见")
    @GetMapping("/{clubId}")
    public Result<ClubVO> getClubDetail(@Parameter(description = "社团ID", required = true)
                                        @PathVariable("clubId") Long clubId) {
        ClubVO vo = clubService.getClubDetail(clubId);
        return Result.success(vo);
    }

    /**
     * 申请创建社团（status=0 待审核）
     *
     * @param request 创建社团请求
     * @return 创建后的社团详情
     */
    @Operation(summary = "申请创建社团", description = "登录用户可申请，状态为待审核，创建者自动成为社长")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public Result<ClubVO> createClub(@Parameter(description = "创建社团请求体", required = true)
                                     @Valid @RequestBody CreateClubRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ClubVO vo = clubService.createClub(request, userId);
        return Result.success(vo);
    }

    /**
     * 加入社团（幂等，已加入不报错）
     *
     * @param clubId 社团ID
     * @return 操作结果
     */
    @Operation(summary = "加入社团", description = "幂等，已加入不报错；社团需为正常状态")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{clubId}/join")
    public Result<String> joinClub(@Parameter(description = "社团ID", required = true)
                                   @PathVariable("clubId") Long clubId) {
        Long userId = SecurityUtils.getCurrentUserId();
        clubService.joinClub(clubId, userId);
        return Result.success("加入成功");
    }

    /**
     * 退出社团（社长不能退出）
     *
     * @param clubId 社团ID
     * @return 操作结果
     */
    @Operation(summary = "退出社团", description = "社长不能退出")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{clubId}/join")
    public Result<String> leaveClub(@Parameter(description = "社团ID", required = true)
                                    @PathVariable("clubId") Long clubId) {
        Long userId = SecurityUtils.getCurrentUserId();
        clubService.leaveClub(clubId, userId);
        return Result.success("退出成功");
    }

    /**
     * 社团帖子列表（对游客可见）
     *
     * @param clubId 社团ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 帖子分页结果
     */
    @Operation(summary = "社团帖子列表", description = "对游客可见，按创建时间倒序")
    @GetMapping("/{clubId}/posts")
    public Result<PageResult<PostListVO>> listClubPosts(@Parameter(description = "社团ID", required = true)
                                                        @PathVariable("clubId") Long clubId,
                                                        @Parameter(description = "当前页码，默认 1")
                                                        @RequestParam(value = "page", defaultValue = "1") long page,
                                                        @Parameter(description = "每页条数，默认 10")
                                                        @RequestParam(value = "size", defaultValue = "10") long size) {
        PageResult<PostListVO> result = clubService.listClubPosts(clubId, page, size);
        return Result.success(result);
    }

    /**
     * 在社团发帖（需为社团成员）
     *
     * @param clubId  社团ID
     * @param request 发帖请求
     * @return 创建后的帖子详情
     */
    @Operation(summary = "在社团发帖", description = "仅社团成员可在社团发帖")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{clubId}/posts")
    public Result<PostDetailVO> createClubPost(@Parameter(description = "社团ID", required = true)
                                               @PathVariable("clubId") Long clubId,
                                               @Parameter(description = "发帖请求体", required = true)
                                               @Valid @RequestBody CreatePostRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PostDetailVO vo = clubService.createClubPost(clubId, request, userId);
        return Result.success(vo);
    }

    /**
     * 删除社团帖子（社长或管理员可操作）
     *
     * @param clubId  社团ID
     * @param postId  帖子ID
     * @return 操作结果
     */
    @Operation(summary = "删除社团帖子", description = "仅社长或管理员可操作，解除社团帖子关联")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{clubId}/posts/{postId}")
    public Result<String> removeClubPost(@Parameter(description = "社团ID", required = true)
                                         @PathVariable("clubId") Long clubId,
                                         @Parameter(description = "帖子ID", required = true)
                                         @PathVariable("postId") Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        clubService.removeClubPost(clubId, postId, userId, isAdmin());
        return Result.success("删除成功");
    }

    /**
     * 编辑社团信息（仅社长）
     *
     * @param clubId  社团ID
     * @param request 编辑请求
     * @return 操作结果
     */
    @Operation(summary = "编辑社团信息", description = "仅社长可编辑名称与简介")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{clubId}")
    public Result<String> updateClub(@Parameter(description = "社团ID", required = true)
                                     @PathVariable("clubId") Long clubId,
                                     @Parameter(description = "编辑请求体", required = true)
                                     @Valid @RequestBody CreateClubRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        clubService.updateClub(clubId, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 成员列表（对游客可见）
     *
     * @param clubId 社团ID
     * @return 成员列表
     */
    @Operation(summary = "社团成员列表", description = "对游客可见，社长在前，按加入时间升序")
    @GetMapping("/{clubId}/members")
    public Result<List<ClubMemberVO>> listMembers(@Parameter(description = "社团ID", required = true)
                                                  @PathVariable("clubId") Long clubId) {
        List<ClubMember> members = clubService.listMembers(clubId);
        return Result.success(toMemberVOList(members));
    }

    /**
     * 社长移除成员
     *
     * @param clubId    社团ID
     * @param memberId  成员记录ID
     * @return 操作结果
     */
    @Operation(summary = "移除社团成员", description = "仅社长可移除成员，不能移除自己")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{clubId}/members/{memberId}")
    public Result<String> removeMember(@Parameter(description = "社团ID", required = true)
                                       @PathVariable("clubId") Long clubId,
                                       @Parameter(description = "成员记录ID", required = true)
                                       @PathVariable("memberId") Long memberId) {
        Long userId = SecurityUtils.getCurrentUserId();
        clubService.removeMember(clubId, memberId, userId);
        return Result.success("移除成功");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将成员记录列表转换为成员视图列表，填充用户名/昵称/头像
     *
     * @param members 成员记录列表
     * @return 成员视图列表
     */
    private List<ClubMemberVO> toMemberVOList(List<ClubMember> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = members.stream()
                .map(ClubMember::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ClubMemberVO> voList = new ArrayList<>(members.size());
        for (ClubMember member : members) {
            ClubMemberVO vo = new ClubMemberVO();
            vo.setId(member.getId());
            vo.setUserId(member.getUserId());
            User user = member.getUserId() == null ? null : userMap.get(member.getUserId());
            vo.setUsername(user == null ? null : user.getUsername());
            vo.setNickname(user == null ? null : user.getNickname());
            vo.setAvatar(user == null ? null : user.getAvatar());
            vo.setRole(member.getRole());
            vo.setJoinedTime(member.getJoinedTime());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 判断当前登录用户是否具有 ROLE_ADMIN 角色
     *
     * @return true 表示是管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (ROLE_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
