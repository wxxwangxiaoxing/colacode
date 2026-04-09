# Elasticsearch 在 ColaCode 项目中的使用

## 1. 为什么使用 Elasticsearch？

题库服务中，题目搜索是核心功能。如果用 MySQL 做全文搜索：
```sql
SELECT * FROM subject_info WHERE subject_name LIKE '%Java%';
```
- **全表扫描**: LIKE '%xx%' 无法使用索引
- **性能差**: 数据量大时查询极慢
- **功能弱**: 不支持分词、相关性排序、高亮等

Elasticsearch 专为全文搜索设计：
- **倒排索引**: 毫秒级全文检索
- **分词器**: 中文分词 (IK Analyzer)
- **相关性排序**: 按匹配度排序
- **高亮显示**: 匹配关键词高亮

## 2. Elasticsearch 核心概念

| ES 概念 | 关系型数据库类比 | 说明 |
|---------|-----------------|------|
| **Index (索引)** | 数据库 (Database) | 文档的集合，如 `subject` |
| **Document (文档)** | 行 (Row) | 一条数据记录 |
| **Field (字段)** | 列 (Column) | 文档中的属性 |
| **Mapping (映射)** | 表结构 (Schema) | 定义字段类型 |
| **Query DSL** | SQL | 查询语言 |

## 3. 项目中的架构

```
用户搜索关键词 "Java"
    ↓
SubjectSearchController
    ↓
SubjectEsService.search()
    ↓
RestHighLevelClient
    ↓
Elasticsearch (倒排索引检索)
    ↓
返回题目列表 (按相关性排序)
```

**数据同步方案**: 题目增删改时同步更新 ES
```
新增题目 → MySQL 写入 → ES 同步写入
更新题目 → MySQL 更新 → ES 同步更新
删除题目 → MySQL 逻辑删除 → ES 同步删除
```

## 4. 代码实现

### 4.1 依赖配置

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.14.0</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>7.14.0</version>
</dependency>
```

> ⚠️ 客户端版本必须与 ES 服务端版本一致

### 4.2 配置文件

```yaml
# application.yml
elasticsearch:
  host: 127.0.0.1
  port: 9200
```

### 4.3 ES 客户端配置

```java
@Configuration
public class EsConfig {
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private int port;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
            RestClient.builder(new HttpHost(host, port, "http"))
        );
    }
}
```

### 4.4 ES 文档模型

```java
@Data
public class SubjectEsDTO implements Serializable {
    private Long id;
    private String subjectName;     // 题目名称 (全文检索)
    private String subjectParse;    // 题目解析 (全文检索)
    private String subjectComment;  // 题目备注 (全文检索)
    private Integer subjectDiff;    // 难度
    private Integer subjectType;    // 题型
    private String categoryName;    // 分类名称
    private String labelName;       // 标签名称
}
```

### 4.5 保存文档

```java
public void save(SubjectEsDTO subjectEsDTO) {
    // 1. 转为 JSON
    JSONObject json = (JSONObject) JSONObject.toJSON(subjectEsDTO);
    
    // 2. 创建索引请求
    IndexRequest request = new IndexRequest("subject");
    request.id(String.valueOf(subjectEsDTO.getId()));
    request.source(json);
    
    // 3. 执行请求
    restHighLevelClient.index(request, RequestOptions.DEFAULT);
}
```

### 4.6 删除文档

```java
public void delete(Long subjectId) {
    DeleteRequest request = new DeleteRequest("subject", String.valueOf(subjectId));
    restHighLevelClient.delete(request, RequestOptions.DEFAULT);
}
```

### 4.7 全文搜索

```java
public PageResult<SubjectEsDTO> search(String keyword, int pageNo, int pageSize) {
    SearchRequest searchRequest = new SearchRequest("subject");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    // 构建布尔查询
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(keyword)) {
        // 多字段匹配查询
        boolQuery.must(QueryBuilders.multiMatchQuery(
            keyword, 
            "subjectName", "subjectParse", "subjectComment"
        ));
    }
    sourceBuilder.query(boolQuery);
    
    // 分页 + 排序
    sourceBuilder.from((pageNo - 1) * pageSize);
    sourceBuilder.size(pageSize);
    sourceBuilder.sort("id", SortOrder.DESC);
    sourceBuilder.trackTotalHits(true);

    searchRequest.source(sourceBuilder);
    SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

    // 解析结果
    List<SubjectEsDTO> results = new ArrayList<>();
    for (SearchHit hit : response.getHits().getHits()) {
        SubjectEsDTO dto = JSON.parseObject(hit.getSourceAsString(), SubjectEsDTO.class);
        results.add(dto);
    }
    return new PageResult<>(pageNo, pageSize, total, results);
}
```

## 5. ES 查询语法详解

### 5.1 常用查询类型

| 查询类型 | 说明 | 示例 |
|---------|------|------|
| **match** | 分词匹配 | `matchQuery("subjectName", "Java")` |
| **multiMatch** | 多字段匹配 | `multiMatchQuery("Java", "name", "parse")` |
| **term** | 精确匹配 (不分词) | `termQuery("subjectType", 1)` |
| **terms** | 多值精确匹配 | `termsQuery("categoryId", 1, 2, 3)` |
| **range** | 范围查询 | `rangeQuery("subjectDiff").gte(1).lte(3)` |
| **bool** | 组合查询 | `must + should + mustNot` |

### 5.2 Bool 查询详解

```java
BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

// must: 必须匹配 (AND)
boolQuery.must(QueryBuilders.matchQuery("subjectName", "Java"));

// should: 可选匹配 (OR)，匹配越多相关性越高
boolQuery.should(QueryBuilders.matchQuery("subjectParse", "Spring"));

// mustNot: 必须不匹配 (NOT)
boolQuery.mustNot(QueryBuilders.termQuery("isDeleted", 1));

// filter: 过滤条件 (不参与相关性评分)
boolQuery.filter(QueryBuilders.rangeQuery("subjectDiff").gte(1).lte(3));
```

## 6. 安装 Elasticsearch

### 6.1 Docker 安装 (推荐)

```bash
# 启动 ES
docker run -d \
  --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:7.14.0

# 安装 IK 中文分词器
docker exec -it elasticsearch \
  elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.14.0/elasticsearch-analysis-ik-7.14.0.zip

# 重启
docker restart elasticsearch
```

### 6.2 验证安装

```bash
curl http://localhost:9200
# 返回:
{
  "name" : "xxx",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "7.14.0"
  }
}
```

### 6.3 创建索引

```bash
curl -X PUT http://localhost:9200/subject -H 'Content-Type: application/json' -d '
{
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "subjectName": { 
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "subjectParse": { 
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "subjectComment": { 
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "subjectDiff": { "type": "integer" },
      "subjectType": { "type": "integer" },
      "categoryName": { "type": "keyword" },
      "labelName": { "type": "keyword" }
    }
  }
}'
```

## 7. 常见问题

### Q1: RestHighLevelClient 已废弃，还能用吗？
ES 8.x 推荐使用 `ElasticsearchJavaClient`，但 7.x 版本 `RestHighLevelClient` 仍是官方推荐。ColaCode 使用 ES 7.14.0，完全兼容。

### Q2: 如何保证 MySQL 和 ES 数据一致？
- **方案一 (当前)**: 应用层双写，MySQL 和 ES 同时写入
- **方案二 (进阶)**: Canal 监听 MySQL binlog，自动同步到 ES
- **方案三 (定时)**: XXL-Job 定时全量/增量同步

### Q3: 分词器 ik_max_word 和 ik_smart 的区别？
- **ik_max_word**: 最细粒度拆分，用于**索引**时
  - "Java开发" → "Java", "开发", "Java开发"
- **ik_smart**: 最粗粒度拆分，用于**搜索**时
  - "Java开发" → "Java", "开发"

### Q4: 如何高亮搜索结果？
```java
sourceBuilder.highlighter(
    new HighlightBuilder()
        .field("subjectName")
        .preTags("<em>")
        .postTags("</em>")
);
```
