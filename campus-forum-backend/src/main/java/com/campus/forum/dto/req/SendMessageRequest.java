package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送私信请求体
 *
 * @author campus
 */
@Data
@Schema(description = "发送私信请求")
public class SendMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息内容 */
    @Schema(description = "消息内容", example = "你好")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000字")
    private String content;

}
