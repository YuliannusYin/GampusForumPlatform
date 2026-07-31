package com.campus.forum.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果包装类
 * 用于包装分页查询的返回数据，统一前后端分页交互格式
 *
 * @param <T> 业务数据类型
 * @author campus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据列表 */
    @Schema(description = "当前页数据列表")
    private List<T> records;

    /** 总记录数 */
    @Schema(description = "总记录数", example = "100")
    private long total;

    /** 当前页码 */
    @Schema(description = "当前页码", example = "1")
    private long page;

    /** 每页条数 */
    @Schema(description = "每页条数", example = "10")
    private long size;

    /**
     * 从 MyBatis-Plus 的 IPage 转换为 PageResult
     *
     * @param page MyBatis-Plus 分页结果
     * @param <T>  业务数据类型
     * @return PageResult 实例
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return new PageResult<>(List.of(), 0L, 1L, 10L);
        }
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

}
