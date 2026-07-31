package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.FileVO;
import com.campus.forum.entity.FileRecord;
import com.campus.forum.mapper.FileRecordMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件服务实现类
 * 负责文件上传的校验、存储与记录
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    /** 允许上传的文件MIME类型：image/jpeg、image/png、image/gif */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    /** 最大文件大小：5MB */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** 日期目录格式：yyyy/MM/dd */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 文件上传路径，对应配置项 campus.upload-path */
    @Value("${campus.upload-path}")
    private String uploadPath;

    @Override
    public FileVO upload(MultipartFile file) {
        // 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件不能为空");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的文件类型");
        }
        // 校验文件大小
        long size = file.getSize();
        if (size > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小不能超过5MB");
        }

        // 原始文件名
        String originalName = file.getOriginalFilename();

        // 生成存储文件名：UUID（去除连字符）+ 原文件扩展名
        String extension = extractExtension(originalName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 按日期分目录：yyyy/MM/dd
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);

        // 完整存储目录（转绝对路径，避免 transferTo 相对路径解析到临时目录的问题）
        Path dirPath = Paths.get(uploadPath, datePath).toAbsolutePath();
        try {
            // 创建目录（如不存在）
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("创建上传目录失败：{}", dirPath, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        }

        // 保存文件到本地磁盘
        Path filePath = dirPath.resolve(storedName);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("保存上传文件失败：{}", filePath, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败");
        }

        // 生成访问 URL：/uploads/yyyy/MM/dd/uuid.ext（与 Nginx 静态映射对齐）
        String url = "/uploads/" + datePath + "/" + storedName;

        // 保存 FileRecord 到数据库
        FileRecord record = new FileRecord();
        record.setUserId(SecurityUtils.getCurrentUserId());
        record.setOriginalName(originalName);
        record.setStoredName(storedName);
        record.setPath(filePath.toString());
        record.setUrl(url);
        record.setSize(size);
        record.setType(contentType);
        save(record);

        // 构造并返回 FileVO
        FileVO vo = new FileVO();
        vo.setUrl(url);
        vo.setOriginalName(originalName);
        vo.setSize(size);
        vo.setType(contentType);
        return vo;
    }

    /**
     * 从原始文件名中提取扩展名（含点号，小写），如 .jpg
     * 若无扩展名则返回空字符串
     *
     * @param originalName 原始文件名
     * @return 扩展名（含点号，小写）
     */
    private String extractExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dotIndex).toLowerCase();
    }

}
