package com.campus.forum.config;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 字段自动填充处理器
 * 在插入和更新时自动填充 create_time 与 update_time 字段
 *
 * 注意：逻辑删除字段 deleted 已在 application.yml 中通过
 * mybatis-plus.global-config.db-config.logic-delete-field 配置，此处不再处理
 *
 * @author campus
 */
@Component
public class MetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {

    /** 创建时间字段名 */
    private static final String CREATE_TIME = "createTime";

    /** 更新时间字段名 */
    private static final String UPDATE_TIME = "updateTime";

    /**
     * 插入时自动填充：create_time 与 update_time
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        // 创建时间填充
        if (metaObject.hasSetter(CREATE_TIME)) {
            this.strictInsertFill(metaObject, CREATE_TIME, Date.class, now);
        }
        // 更新时间填充
        if (metaObject.hasSetter(UPDATE_TIME)) {
            this.strictInsertFill(metaObject, UPDATE_TIME, Date.class, now);
        }
    }

    /**
     * 更新时自动填充：update_time
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter(UPDATE_TIME)) {
            this.strictUpdateFill(metaObject, UPDATE_TIME, Date.class, new Date());
        }
    }

}
