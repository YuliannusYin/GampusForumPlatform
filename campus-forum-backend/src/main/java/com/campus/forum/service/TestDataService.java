package com.campus.forum.service;

import com.campus.forum.dto.resp.TestDataImportResult;
import com.campus.forum.dto.resp.TestDataRemoveResult;
import com.campus.forum.dto.resp.TestDataStatus;

/**
 * 测试数据管理服务接口
 * 提供测试数据的一键导入、移除与状态查询能力，仅供超级管理员调用
 *
 * @author campus
 */
public interface TestDataService {

    /**
     * 一键导入全量测试数据
     * 生成用户、社团、帖子、评论、点赞、收藏、关注、私信、通知等测试数据
     * 若已存在测试数据则抛出业务异常
     *
     * @return 导入结果（各类数据实际生成数量）
     */
    TestDataImportResult importTestData();

    /**
     * 一键移除所有测试数据
     * 根据 test_ 前缀与【测试】前缀识别并清理全部测试数据
     *
     * @return 移除结果（各类数据实际删除数量）
     */
    TestDataRemoveResult removeTestData();

    /**
     * 查询当前系统中各类测试数据的数量
     *
     * @return 测试数据状态
     */
    TestDataStatus status();

}
