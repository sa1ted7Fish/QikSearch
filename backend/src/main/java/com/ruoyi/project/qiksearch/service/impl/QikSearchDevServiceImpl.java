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
            // 获取得分
            Float score = hit.getScore();

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
                resultMap.put("title", score + category + ": [" + date + "] " + documentId);
            } else {
                resultMap.put("title", category != null ? score + category + ": " + title : score + title);
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
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1. 核心：严格短语匹配（完整句截取，无挖空/无修改）
        // 权重拉到最高（50分），确保完全匹配的原文绝对优先，不受词频/时间干扰
        boolQuery.should(QueryBuilders.constantScoreQuery(
                QueryBuilders.matchPhraseQuery("full_text", keyword)
                        .slop(0)  // 词项严格连续，对应“原文直接截取的完整句题目”
        ).boost(50.0f));

        // 2. 挖空句专项匹配（中间插入2-4个词，你的核心场景）
        // 权重次高（35分），仅低于完整句，确保挖空题能精准命中原文
        boolQuery.should(QueryBuilders.constantScoreQuery(
                QueryBuilders.matchPhraseQuery("full_text", keyword)
                        .slop(6)  // 适配2-4个挖空词，预留标点/助词冗余
        ).boost(35.0f));

        // 3. 宽松短语匹配（轻微改写：少标点/多助词，非挖空类修改）
        // 权重中等（15分），低于挖空句，避免轻微改写的结果干扰核心场景
        boolQuery.should(QueryBuilders.constantScoreQuery(
                QueryBuilders.matchPhraseQuery("full_text", keyword)
                        .slop(2)  // 仅允许1-2个词的差异，控制宽松度
        ).boost(15.0f));

        // 4. 精确分词匹配（所有词必现，无顺序要求，仅作次兜底）
        // 权重压到极低（3分），避免“词频高但句子不匹配”的文章反超
        boolQuery.should(QueryBuilders.constantScoreQuery(
                QueryBuilders.matchQuery("full_text", keyword)
                        .analyzer("ik_max_word")
                        .operator(Operator.AND)
                        .minimumShouldMatch("100%")
        ).boost(3.0f));

        // 5. 兜底匹配（80%词匹配，边缘召回，权重最低）
        // 仅用于“无短语匹配”的极端情况，避免无结果返回
        boolQuery.should(QueryBuilders.constantScoreQuery(
                QueryBuilders.matchQuery("full_text", keyword)
                        .analyzer("ik_max_word")
                        .minimumShouldMatch("80%")
        ).boost(1.0f));

        // 日期衰减：优化新旧文章平衡（新文章有优势，旧文章不被过度压制）
        FunctionScoreQueryBuilder.FilterFunctionBuilder gaussFunction =
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        ScoreFunctionBuilders.gaussDecayFunction(
                                "date",       // 日期字段
                                "now",        // 原点：当前时间
                                "730d",       // 缩放因子：2年（覆盖你的“最早两年前文章”需求）
                                "30d",        // 偏移量：近30天文章不衰减（匹配“部分题目来自上一周/近几月”）
                                0.6f          // 衰减系数：减缓衰减（2年前文章仍有50%左右得分）
                        )
                );

        // 组合查询：短语匹配固定分 × 时间衰减分（彻底排除词频影响）
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{gaussFunction}
        ).boostMode(CombineFunction.MULTIPLY);

        // 最小得分阈值：过滤低于5分的无效结果（保留核心匹配+部分次优结果）
//        sourceBuilder.minScore(5.0f);
        sourceBuilder.query(functionScoreQuery);

        return sourceBuilder;
    }
}
