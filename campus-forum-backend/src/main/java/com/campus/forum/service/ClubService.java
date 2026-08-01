package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.req.CreateClubRequest;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.ClubVO;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Club;
import com.campus.forum.entity.ClubMember;

import java.util.List;

/**
 * 社团服务接口
 * 封装社团创建、审核、加入/退出、成员管理、社团发帖等业务逻辑
 *
 * @author campus
 */
public interface ClubService extends IService<Club> {

    /**
     * 社团分页列表
     * status 为 null 时只查 status=1（正常）的社团，对游客可见
     *
     * @param page    当前页码
     * @param size    每页条数
     * @param keyword 社团名称关键词（可选）
     * @param status  社团状态（可选，为 null 时仅查正常社团）
     * @return 社团分页结果
     */
    PageResult<ClubVO> listClubs(long page, long size, String keyword, Integer status);

    /**
     * 社团详情
     *
     * @param clubId 社团ID
     * @return 社团视图
     */
    ClubVO getClubDetail(Long clubId);

    /**
     * 申请创建社团（status=0 待审核）
     * 创建者自动写入 club_member（role=1 社长）
     *
     * @param req    创建社团请求
     * @param userId 当前用户ID
     * @return 创建后的社团视图
     */
    ClubVO createClub(CreateClubRequest req, Long userId);

    /**
     * 加入社团（幂等，已加入不报错；club.status 必须=1 正常）
     *
     * @param clubId 社团ID
     * @param userId 当前用户ID
     */
    void joinClub(Long clubId, Long userId);

    /**
     * 退出社团（社长不能退出）
     *
     * @param clubId 社团ID
     * @param userId 当前用户ID
     */
    void leaveClub(Long clubId, Long userId);

    /**
     * 社团帖子列表
     *
     * @param clubId 社团ID
     * @param page   当前页码
     * @param size   每页条数
     * @return 帖子分页结果
     */
    PageResult<PostListVO> listClubPosts(Long clubId, long page, long size);

    /**
     * 在社团发帖（需为社团成员）
     * 调用 PostService.createPost 后写入 club_post 关联，并更新 club.postCount+1
     *
     * @param clubId 社团ID
     * @param req    发帖请求
     * @param userId 当前用户ID
     * @return 创建后的帖子详情
     */
    PostDetailVO createClubPost(Long clubId, CreatePostRequest req, Long userId);

    /**
     * 删除社团帖子（社长或管理员可操作）
     * 删除 club_post 关联，club.postCount-1
     *
     * @param clubId  社团ID
     * @param postId  帖子ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    void removeClubPost(Long clubId, Long postId, Long userId, boolean isAdmin);

    /**
     * 编辑社团信息（仅社长）
     *
     * @param clubId 社团ID
     * @param req    编辑请求
     * @param userId 当前用户ID
     */
    void updateClub(Long clubId, CreateClubRequest req, Long userId);

    /**
     * 社长移除成员
     *
     * @param clubId    社团ID
     * @param memberId  成员记录ID
     * @param userId    当前用户ID（操作者）
     */
    void removeMember(Long clubId, Long memberId, Long userId);

    /**
     * 成员列表
     *
     * @param clubId 社团ID
     * @return 成员记录列表
     */
    List<ClubMember> listMembers(Long clubId);

    /**
     * 审核通过（status=0→1，已通过的不重复处理）
     *
     * @param clubId 社团ID
     */
    void approveClub(Long clubId);

    /**
     * 审核拒绝（status=0→2 禁用）
     *
     * @param clubId 社团ID
     */
    void rejectClub(Long clubId);

}
