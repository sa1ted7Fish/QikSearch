package com.ruoyi.project.qiksearch.service.impl;

import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.qiksearch.service.IQikSearchService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
import com.ruoyi.common.utils.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Elasticsearch搜索服务实现
 */
@Service
public class QikSearchServiceImpl implements IQikSearchService {

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

            // 1. 提取title字段，当title为"题库"时添加冒号和记录id
            String title = (String) sourceMap.get("title");
            String category = (String) sourceMap.get("category");
            if ("题库".equals(title)) {
                // 如果标题是"题库"，则格式化为"题库:ID值"
                resultMap.put("title", title + ": " + documentId);
            } else {
                resultMap.put("title", title != null ? category + ": " + title : "");
            }

            // 2. 提取category字段（新增）
            resultMap.put("category", category != null ? category : "");

            // 3. 提取html_path字段（新增）
            String htmlPath = (String) sourceMap.get("html_path");
            resultMap.put("htmlPath", htmlPath != null ? htmlPath : "");

            // 4. 获取获取full_text字段的高亮内容（保持不变）
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

    @Override
    public boolean addQuestion(String questionContent) throws IOException {
        if (StringUtils.isEmpty(questionContent)) {
            return false;
        }

        // 构建文档数据
        Map<String, Object> document = new HashMap<>();
        document.put("title", "题库"); // 固定标题为"题库"
        document.put("full_text", questionContent); // 题目内容
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(new Date());
        document.put("date", formattedDate);

        // 创建索引请求
        IndexRequest request = new IndexRequest(INDEX_NAME);

        request.source(document);

        // 执行索引操作
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);

        // 判断是否创建成功
        return "CREATED".equals(response.getResult().name()) ||
                "UPDATED".equals(response.getResult().name());
    }

    public static SearchSourceBuilder buildUniversalQuery(String keyword) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1. 最高优先级：完整查询句精确匹配（完全一致的文档）
        boolQuery.should(QueryBuilders.termQuery("full_text.keyword", keyword))
                .boost(10.0f);  // 权重最高，确保完全匹配的文档排在最前

        // 2. 高优先级：严格短语匹配（词项连续，允许极少量间隔如标点）
        boolQuery.should(QueryBuilders.matchPhraseQuery("full_text", keyword)
                .slop(1)  // 允许1个间隔（适应标点、空格等）
                .boost(9.0f));  // 次高权重，完整句子优先

        // 3. 中高优先级：宽松短语匹配（词项顺序大致一致，允许有限打乱）
        boolQuery.should(QueryBuilders.matchPhraseQuery("full_text", keyword)
                .slop(3)  // 允许3个间隔（适应语序微调，如“发展西部”和“西部发展”）
                .boost(7.0f));  // 权重高于单纯词频匹配

        // 4. 基础匹配：必须包含所有词项（排除部分匹配，但弱化词频影响）
        boolQuery.should(QueryBuilders.matchQuery("full_text", keyword)
                .analyzer("ik_max_word")
                .operator(Operator.AND)  // 强制包含所有词项（完整句子必满足）
                .minimumShouldMatch("100%")  // 与AND配合，确保不遗漏词项
                .boost(3.0f));  // 权重较低，避免词频主导

        // 5. 兜底匹配：仅保留高相关性（过滤低质文档）
        boolQuery.should(QueryBuilders.matchQuery("full_text", keyword)
                .minimumShouldMatch("80%")  // 需匹配大部分词项
                .boost(1.0f));  // 最低权重

        // 6. 保留日期衰减（不影响核心排序逻辑）
        FunctionScoreQueryBuilder.FilterFunctionBuilder gaussFunction =
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        ScoreFunctionBuilders.gaussDecayFunction(
                                "date", "now", "60d", "7d", 0.8
                        )
                );

        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{gaussFunction}
        ).boostMode(CombineFunction.MULTIPLY);

        sourceBuilder.query(functionScoreQuery);
        return sourceBuilder;
    }
}
