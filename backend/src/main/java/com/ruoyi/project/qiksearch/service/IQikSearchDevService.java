package com.ruoyi.project.qiksearch.service;

import com.ruoyi.framework.web.page.TableDataInfo;

import java.io.IOException;

/**
 * Elasticsearch搜索服务接口
 * 符合RuoYi风格：定义接口，返回TableDataInfo用于分页
 */
public interface IQikSearchDevService {


    TableDataInfo search(String keyword, Integer pageNum, Integer pageSize) throws IOException;
}
