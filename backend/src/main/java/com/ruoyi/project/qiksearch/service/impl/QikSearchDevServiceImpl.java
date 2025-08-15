package com.ruoyi.project.qiksearch.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.qiksearch.service.IQikSearchDevService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Elasticsearch搜索服务实现
 */
@Service
public class QikSearchDevServiceImpl implements IQikSearchDevService {

    @Autowired
    private RestHighLevelClient esClient;

    private static final String INDEX_NAME = "qs";

    @Override
    public TableDataInfo search(String keyword, Integer pageNum, Integer pageSize) throws IOException {
        // 校验参数
        if (StringUtils.isEmpty(keyword) || pageNum == null || pageSize == null) {
            return null;
        }

        // 1. 创建搜索请求（指定索引）
        SearchRequest searchRequest = new SearchRequest("qs");
        SearchSourceBuilder sourceBuilder = buildUniversalQuery(keyword);

        // 3. 配置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightField = new HighlightBuilder.Field("full_text");
        highlightField.preTags("<em>");
        highlightField.postTags("</em>");
        highlightField.fragmentSize(300);
        highlightField.numOfFragments(1);
        highlightField.noMatchSize(0);
        highlightBuilder.field(highlightField);
        sourceBuilder.highlighter(highlightBuilder);

        // 4. 配置分页
        sourceBuilder.from((pageNum - 1) * pageSize); // RuoYi页码从1开始
        sourceBuilder.size(pageSize);

        // 5. 返回结果
        searchRequest.source(sourceBuilder);

        // 6. 执行查询
        SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);

        // 7. 处理结果
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> resultMap = new HashMap<>(4); // 容量调整为4，适配4个字段

            // 获取文档原始数据
            Map<String, Object> sourceMap = hit.getSourceAsMap();
            // 获取文档ID
            String documentId = hit.getId();
            resultMap.put("id", documentId != null ? documentId : "");

            // 提取title、category字段，作为结果的标题
            String title = (String) sourceMap.get("title");
            String category = (String) sourceMap.get("category");
            String date = (String) sourceMap.get("date");
            resultMap.put("category", category != null ? category : "");

            if ("题库".equals(category)) {
                // 如果类别是"题库"，则格式化为"题库:ID值"
                resultMap.put("title", category + ": [" + date + "] " + documentId);
            } else {
                resultMap.put("title", category != null ? category + ": " + title : title);
            }

            // 提取html_path字段
            String htmlPath = (String) sourceMap.get("html_path");
            resultMap.put("htmlPath", htmlPath != null ? htmlPath : "");

            // 获取获取full_text字段的高亮内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField fullTextHighlight = highlightFields.get("full_text");
            if (fullTextHighlight != null && fullTextHighlight.getFragments().length > 0) {
                Text fragment = fullTextHighlight.getFragments()[0];
                resultMap.put("highlightContent", fragment.toString());
            } else {
                resultMap.put("highlightContent", "");
            }

            resultList.add(resultMap);
        }

        long total = response.getHits().getTotalHits().value;

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200); // 成功状态码
        rspData.setMsg("查询成功");
        rspData.setRows(resultList); // 当前页数据列表
        rspData.setTotal(total); // 总条数

        return rspData;
    }

    public static SearchSourceBuilder buildUniversalQuery(String keyword) {
        // 创建搜索源构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建bool查询（对应ES中的bool.should）
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1. term查询（full_text.keyword精确匹配，boost=10.0）
        boolQuery.should(QueryBuilders.termQuery("full_text.keyword", keyword)
                .boost(10.0f));

        // 2. 严格短语匹配（slop=1，boost=9.0）
        boolQuery.should(QueryBuilders.matchPhraseQuery("full_text", keyword)
                .slop(1)
                .boost(9.0f));

        // 3. 宽松短语匹配（slop=3，boost=7.0）
        boolQuery.should(QueryBuilders.matchPhraseQuery("full_text", keyword)
                .slop(3)
                .boost(7.0f));

        // 4. 基础匹配（AND逻辑，100%匹配，ik_max_word分词，boost=3.0）
        boolQuery.should(QueryBuilders.matchQuery("full_text", keyword)
                .analyzer("ik_max_word")
                .operator(Operator.AND)
                .minimumShouldMatch("100%")
                .boost(3.0f));

        // 5. 兜底匹配（80%匹配，boost=1.0）
        boolQuery.should(QueryBuilders.matchQuery("full_text", keyword)
                .minimumShouldMatch("80%")
                .boost(1.0f));

        // 构建日期衰减函数（gauss函数，参数与ES一致）
        FunctionScoreQueryBuilder.FilterFunctionBuilder gaussFunction =
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        ScoreFunctionBuilders.gaussDecayFunction(
                                "date",       // 日期字段
                                "now",        // 原点（当前时间）
                                "60d",        // 缩放因子（60天）
                                "7d",         // 偏移量（7天）
                                0.8f          // 衰减系数
                        )
                );

        // 构建function_score查询（组合bool查询和衰减函数）
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery,  // 基础查询
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{gaussFunction}  // 函数数组
        ).boostMode(CombineFunction.MULTIPLY);  // 得分组合方式（相乘）

        // 设置查询和最小得分阈值
        sourceBuilder.query(functionScoreQuery);
        sourceBuilder.minScore(20.0f);

        return sourceBuilder;
    }
}
