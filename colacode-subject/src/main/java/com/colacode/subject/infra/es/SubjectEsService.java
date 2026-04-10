package com.colacode.subject.infra.es;

import com.colacode.common.PageResult;
import com.colacode.subject.infra.entity.SubjectInfo;
import com.colacode.subject.infra.mapper.SubjectInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SubjectEsService {

    private static final String INDEX_NAME = "subject";

    private final RestHighLevelClient restHighLevelClient;
    private final SubjectInfoMapper subjectInfoMapper;

    public SubjectEsService(RestHighLevelClient restHighLevelClient, SubjectInfoMapper subjectInfoMapper) {
        this.restHighLevelClient = restHighLevelClient;
        this.subjectInfoMapper = subjectInfoMapper;
    }

    public void syncAllSubjects() {
        List<SubjectInfo> allSubjects = subjectInfoMapper.selectList(null);
        for (SubjectInfo subject : allSubjects) {
            SubjectEsDTO dto = new SubjectEsDTO();
            dto.setId(subject.getId());
            dto.setSubjectName(subject.getSubjectName());
            dto.setSubjectParse(subject.getSubjectParse());
            dto.setSubjectComment(subject.getSubjectComment());
            dto.setSubjectType(subject.getSubjectType());
            dto.setSubjectDiff(subject.getSubjectDiff());
            save(dto);
        }
        log.info("全量同步题目到ES完成, 共{}条", allSubjects.size());
    }

    public void save(SubjectEsDTO subjectEsDTO) {
        try {
            com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSONObject.toJSON(subjectEsDTO);
            org.elasticsearch.action.index.IndexRequest request = new org.elasticsearch.action.index.IndexRequest(INDEX_NAME);
            request.id(String.valueOf(subjectEsDTO.getId()));
            request.source(json);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            log.info("ES保存题目成功, id: {}", subjectEsDTO.getId());
        } catch (IOException e) {
            log.error("ES保存题目失败, id: {}", subjectEsDTO.getId(), e);
            throw new RuntimeException("ES保存题目失败", e);
        }
    }

    public void delete(Long subjectId) {
        try {
            org.elasticsearch.action.delete.DeleteRequest request = new org.elasticsearch.action.delete.DeleteRequest(INDEX_NAME, String.valueOf(subjectId));
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            log.info("ES删除题目成功, id: {}", subjectId);
        } catch (IOException e) {
            log.error("ES删除题目失败, id: {}", subjectId, e);
            throw new RuntimeException("ES删除题目失败", e);
        }
    }

    public SubjectEsDTO getById(Long subjectId) {
        try {
            org.elasticsearch.action.get.GetRequest request = new org.elasticsearch.action.get.GetRequest(INDEX_NAME, String.valueOf(subjectId));
            org.elasticsearch.action.get.GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            if (response.isExists()) {
                String source = response.getSourceAsString();
                return com.alibaba.fastjson.JSON.parseObject(source, SubjectEsDTO.class);
            }
            return null;
        } catch (IOException e) {
            log.error("ES查询题目失败, id: {}", subjectId, e);
            return null;
        }
    }

    public PageResult<SubjectEsDTO> search(String keyword, int pageNo, int pageSize) {
        try {
            SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (StringUtils.isNotBlank(keyword)) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "subjectName", "subjectParse", "subjectComment"));
            }
            sourceBuilder.query(boolQuery);
            sourceBuilder.from((pageNo - 1) * pageSize);
            sourceBuilder.size(pageSize);
            sourceBuilder.sort("id", SortOrder.DESC);
            sourceBuilder.trackTotalHits(true);

            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            List<SubjectEsDTO> results = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String source = hit.getSourceAsString();
                SubjectEsDTO dto = com.alibaba.fastjson.JSON.parseObject(source, SubjectEsDTO.class);
                results.add(dto);
            }

            long total = searchResponse.getHits().getTotalHits().value;
            PageResult<SubjectEsDTO> pageResult = new PageResult<>(pageNo, pageSize, total, results);
            log.info("ES搜索完成, keyword: {}, total: {}", keyword, total);
            return pageResult;
        } catch (IOException e) {
            log.error("ES搜索失败", e);
            return new PageResult<>();
        }
    }
}
