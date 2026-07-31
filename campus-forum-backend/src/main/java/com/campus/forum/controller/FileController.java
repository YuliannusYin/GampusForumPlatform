package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.FileVO;
import com.campus.forum.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 * 提供文件上传等接口
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件接口", description = "文件上传等")
public class FileController {

    private final FileService fileService;

    /**
     * 文件上传
     * 校验文件类型与大小后保存到本地磁盘，并记录到 file_record 表
     *
     * @param file 上传的文件
     * @return 文件视图对象（含访问URL、原始文件名、大小、类型）
     */
    @Operation(summary = "文件上传", description = "上传图片（image/jpeg、image/png、image/gif），单文件不超过5MB")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public Result<FileVO> upload(@Parameter(description = "上传的文件", required = true)
                                 @RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(file));
    }

}
