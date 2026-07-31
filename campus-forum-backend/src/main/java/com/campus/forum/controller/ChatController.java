package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.SendMessageRequest;
import com.campus.forum.dto.resp.ChatMessageVO;
import com.campus.forum.dto.resp.ChatSessionVO;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 私信接口
 * 提供会话列表、历史消息、未读数量、标记已读、发送私信等功能
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
@Tag(name = "私信接口", description = "会话列表、历史消息、未读数量、标记已读、发送私信")
public class ChatController {

    private final ChatService chatService;

    /**
     * 查询当前用户的会话列表
     *
     * @return 会话视图列表
     */
    @Operation(summary = "会话列表", description = "查询当前用户的会话列表，含另一用户信息、最后消息内容与未读数")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> listSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatSessionVO> list = chatService.listSessions(userId);
        return Result.success(list);
    }

    /**
     * 分页查询会话历史消息（按 create_time 正序，先旧后新）
     *
     * @param sessionId 会话ID
     * @param page      当前页码，默认 1
     * @param size      每页条数，默认 20
     * @return 分页结果
     */
    @Operation(summary = "历史消息分页", description = "按 create_time 正序查询会话历史消息，联查发送者用户名与头像")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/messages/{sessionId}")
    public Result<PageResult<ChatMessageVO>> listMessages(@Parameter(description = "会话ID", required = true)
                                                          @PathVariable("sessionId") Long sessionId,
                                                          @Parameter(description = "当前页码，默认 1")
                                                          @RequestParam(value = "page", defaultValue = "1") long page,
                                                          @Parameter(description = "每页条数，默认 20")
                                                          @RequestParam(value = "size", defaultValue = "20") long size) {
        PageResult<ChatMessageVO> result = chatService.listMessages(sessionId, page, size);
        return Result.success(result);
    }

    /**
     * 查询当前用户未读私信总数
     *
     * @return 未读私信总数
     */
    @Operation(summary = "未读私信数量", description = "查询当前用户的未读私信总数")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/messages/unread/count")
    public Result<Integer> countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();
        int count = chatService.countUnread(userId);
        return Result.success(count);
    }

    /**
     * 标记某会话所有消息为已读
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "标记会话已读", description = "标记某会话中发送给当前用户的所有未读消息为已读")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/messages/{sessionId}/read")
    public Result<Void> markRead(@Parameter(description = "会话ID", required = true)
                                 @PathVariable("sessionId") Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        chatService.markRead(sessionId, userId);
        return Result.success();
    }

    /**
     * 发送私信
     *
     * @param receiverId 接收者ID
     * @param request    请求体（content）
     * @return 消息视图
     */
    @Operation(summary = "发送私信", description = "登录用户发送私信，若接收者在线则通过 WebSocket 实时推送")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/messages/{receiverId}")
    public Result<ChatMessageVO> sendMessage(@Parameter(description = "接收者ID", required = true)
                                             @PathVariable("receiverId") Long receiverId,
                                             @Parameter(description = "发送私信请求体", required = true)
                                             @Valid @RequestBody SendMessageRequest request) {
        ChatMessageVO vo = chatService.sendMessage(receiverId, request.getContent());
        return Result.success(vo);
    }

}
