package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.resp.FileVO;
import com.campus.forum.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author campus
 */
public interface FileService extends IService<FileRecord> {

    /**
     * 上传文件
     * 校验文件类型与大小，保存到本地磁盘，并记录到 file_record 表
     *
     * @param file 上传的文件
     * @return 文件视图对象（含访问URL、原始文件名、大小、类型）
     */
    FileVO upload(MultipartFile file);

}
