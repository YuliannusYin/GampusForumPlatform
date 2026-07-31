package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Post;

/**
 * 帖子服务接口
 * 封装发帖、编辑、删除、分页列表、详情查询等业务逻辑
 *
 * @author campus
 */
public interface PostService extends IService<Post> {

    /**
     * 发帖
     *
     * @param request 发帖请求
     * @param userId  当前用户ID
     * @return 创建后的帖子详情
     */
    PostDetailVO createPost(CreatePostRequest request, Long userId);

    /**
     * 编辑帖子
     * 校验权限：当前用户是作者或管理员
     *
     * @param id        帖子ID
     * @param request   编辑请求
     * @param userId    当前用户ID
     * @param isAdmin   当前用户是否为管理员
     * @return 更新后的帖子详情
     */
    PostDetailVO updatePost(Long id, CreatePostRequest request, Long userId, boolean isAdmin);

    /**
     * 删除帖子（逻辑删除）
     * 校验权限：当前用户是作者或管理员
     *
     * @param id      帖子ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    void deletePost(Long id, Long userId, boolean isAdmin);

    /**
     * 分页查询帖子列表
     *
     * @param page      当前页码
     * @param size      每页条数
     * @param sectionId 板块ID（可选）
     * @param sort      排序方式 latest / hot
     * @return 分页结果
     */
    PageResult<PostListVO> listPosts(long page, long size, Long sectionId, String sort);

    /**
     * 查询帖子详情，并将浏览数 +1
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    PostDetailVO getPostDetail(Long id);

    /**
     * 关键词搜索帖子
     * 匹配标题、正文、作者用户名；仅查询已发布且未删除的帖子
     * 可选筛选：板块ID、起止时间；按创建时间倒序
     *
     * @param keyword   关键词（匹配标题/正文/作者用户名）
     * @param sectionId 板块ID（可选，为 null 时不限制）
     * @param startTime 起始时间字符串 yyyy-MM-dd（可选，为 null 时不限制）
     * @param endTime   截止时间字符串 yyyy-MM-dd（可选，为 null 时不限制）
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<PostListVO> search(String keyword, Long sectionId, String startTime, String endTime, int page, int size);

}
