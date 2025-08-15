package com.ruoyi.project.qiksearch.service;

import com.ruoyi.framework.web.page.TableDataInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch搜索服务接口
 * 符合RuoYi风格：定义接口，返回TableDataInfo用于分页
 */
public interface IQikSearchService {


    TableDataInfo search(String keyword, Integer pageNum, Integer pageSize) throws IOException;

    boolean addQuestion(String questionContent) throws IOException;
}
