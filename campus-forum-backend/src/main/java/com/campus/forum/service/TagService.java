package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.dto.resp.TagVO;
import com.campus.forum.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 * 提供标签列表查询、按标签检索帖子等业务逻辑
 *
 * @author campus
 */
public interface TagService extends IService<Tag> {

    /**
     * 查询所有标签，返回包含每个标签帖子数的视图列表
     *
     * @return 标签视图列表
     */
    List<TagVO> listAllTags();

    /**
     * 根据标签ID分页查询关联帖子
     *
     * @param tagId 标签ID
     * @param page  当前页码
     * @param size  每页条数
     * @return 帖子分页结果
     */
    PageResult<PostListVO> getPostsByTag(Long tagId, int page, int size);

}
