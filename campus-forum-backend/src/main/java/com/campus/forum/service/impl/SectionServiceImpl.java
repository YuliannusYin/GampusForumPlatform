package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.entity.Section;
import com.campus.forum.mapper.SectionMapper;
import com.campus.forum.service.SectionService;
import org.springframework.stereotype.Service;

/**
 * 板块服务实现类
 *
 * @author campus
 */
@Service
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService {
}
