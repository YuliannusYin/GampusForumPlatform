package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.CreateReportRequest;
import com.campus.forum.dto.req.HandleReportRequest;
import com.campus.forum.dto.resp.ReportVO;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Report;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.ReportMapper;
import com.campus.forum.service.CommentService;
import com.campus.forum.service.PostService;
import com.campus.forum.service.ReportService;
import com.campus.forum.utils.PostAnonymityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 举报服务实现
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    /** 目标类型：帖子 */
    private static final int TARGET_TYPE_POST = 1;

    /** 目标类型：评论 */
    private static final int TARGET_TYPE_COMMENT = 2;

    /** 状态：待处理 */
    private static final int STATUS_PENDING = 0;

    /** 状态：属实已处理 */
    private static final int STATUS_APPROVED = 1;

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 提交举报：仅允许举报表白墙匿名帖及其评论，不能举报自己，待处理不可重复
     *
     * @param request    举报请求
     * @param reporterId 举报人ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReport(CreateReportRequest request, Long reporterId) {
        Post post;
        Long ownerId;
        if (TARGET_TYPE_POST == request.getTargetType()) {
            post = postMapper.selectById(request.getTargetId());
            if (post == null) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND);
            }
            ownerId = post.getUserId();
        } else {
            Comment comment = commentMapper.selectById(request.getTargetId());
            if (comment == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
            post = postMapper.selectById(comment.getPostId());
            if (post == null) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND);
            }
            ownerId = comment.getUserId();
        }

        if (post.getIsAnonymous() == null || post.getIsAnonymous() != PostAnonymityHelper.ANONYMOUS_YES) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅可举报表白墙内容");
        }
        if (reporterId.equals(ownerId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能举报自己的内容");
        }

        Long pending = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, reporterId)
                .eq(Report::getTargetType, request.getTargetType())
                .eq(Report::getTargetId, request.getTargetId())
                .eq(Report::getStatus, STATUS_PENDING));
        if (pending != null && pending > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已提交过该内容的举报，请等待处理");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus(STATUS_PENDING);
        reportMapper.insert(report);
    }

    /**
     * 管理端分页查询举报
     *
     * @param page       页码
     * @param size       每页条数
     * @param status     状态（可选）
     * @param targetType 目标类型（可选）
     * @return 分页结果
     */
    @Override
    public PageResult<ReportVO> listReports(long page, long size, Integer status, Integer targetType) {
        Page<ReportVO> p = new Page<>(page, size);
        IPage<ReportVO> result = reportMapper.selectAdminReportList(p, status, targetType);
        return PageResult.of(result);
    }

    /**
     * 处理举报：属实则逻辑删除对应帖/评，驳回仅改状态
     *
     * @param id        举报ID
     * @param request   处理请求
     * @param handlerId 处理人ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long id, HandleReportRequest request, Long handlerId) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报记录不存在");
        }
        if (report.getStatus() == null || report.getStatus() != STATUS_PENDING) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该举报已处理");
        }

        if (STATUS_APPROVED == request.getStatus()) {
            if (TARGET_TYPE_POST == report.getTargetType()) {
                Post post = postMapper.selectById(report.getTargetId());
                if (post != null) {
                    postService.deletePost(report.getTargetId(), handlerId, true);
                }
            } else if (TARGET_TYPE_COMMENT == report.getTargetType()) {
                Comment comment = commentMapper.selectById(report.getTargetId());
                if (comment != null) {
                    commentService.deleteComment(report.getTargetId(), handlerId, true);
                }
            }
        }

        Report update = new Report();
        update.setId(id);
        update.setStatus(request.getStatus());
        update.setHandlerId(handlerId);
        update.setHandleRemark(request.getHandleRemark());
        reportMapper.updateById(update);
    }

}
