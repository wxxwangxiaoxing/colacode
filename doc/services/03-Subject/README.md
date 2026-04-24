# ColaCode 学习笔记 - Subject 题库服务

## 第二章：Subject 题库服务

### 2.1 模块概览

Subject 题库服务是 ColaCode 的核心业务模块，负责题目的增删改查、分类管理、全文搜索和点赞功能。

**核心功能：**
- 📚 题目 CRUD (单选/多选/判断/简答)
- 🏷️ 分类树管理
- 🔍 Elasticsearch 全文搜索
- ❤️ RocketMQ 异步点赞
- 🏗️ 策略模式处理不同题型

### 2.2 数据库设计

**9 张核心表：**

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| subject_info | 题目主表 | subject_name, subject_diff, subject_type, subject_parse |
| subject_category | 题目分类 | category_name, parent_id, category_type |
| subject_label | 题目标签 | label_name |
| subject_mapping | 题目-分类-标签关联 | subject_id, category_id, label_id |
| subject_radio | 单选题选项 | subject_id, option_type, option_content, is_correct |
| subject_multiple | 多选题选项 | subject_id, option_type, option_content, is_correct |
| subject_judge | 判断题答案 | subject_id, is_correct |
| subject_brief | 简答题答案 | subject_id, brief_content |
| subject_liked | 点赞记录 | subject_id, liked_user_id, liked_status |

**题型标识 (subject_type)：**
- `1` = 单选题
- `2` = 多选题
- `3` = 判断题
- `4` = 简答题

### 2.3 策略模式详解

#### 2.3.1 为什么用策略模式？

4 种题型的数据结构完全不同：
- 单选题: 4 个选项 (A/B/C/D) + 1 个正确答案
- 多选题: 4 个选项 (A/B/C/D) + 多个正确答案
- 判断题: 只有 is_correct 字段
- 简答题: 只有 brief_content 字段

**不用策略模式的问题：**
```java
// 一堆 if-else，难以维护
if (type == 1) {
    // 处理单选...
} else if (type == 2) {
    // 处理多选...
} else if (type == 3) {
    // 处理判断...
} else if (type == 4) {
    // 处理简答...
}
```

**策略模式的优势：**
- 开闭原则：新增题型只需添加新 Handler，不修改现有代码
- 单一职责：每个 Handler 只处理一种题型
- 易于测试：每个 Handler 可以独立测试

#### 2.3.2 策略模式实现

**策略接口：**
```java
public interface SubjectTypeHandler {
    Integer getHandlerType();  // 返回处理的题型
    void add(SubjectInfoBO bo);     // 新增
    void update(SubjectInfoBO bo);  // 更新
    SubjectInfoBO query(Long id);   // 查询
}
```

**具体策略 (以单选题为例)：**
```java
@Component
public class RadioTypeHandler implements SubjectTypeHandler {
    @Resource
    private SubjectRadioMapper subjectRadioMapper;

    @Override
    public Integer getHandlerType() {
        return 1; // 单选题
    }

    @Override
    public void add(SubjectInfoBO bo) {
        for (SubjectRadioBO radioBO : bo.getRadioList()) {
            SubjectRadio entity = converter.convertToEntity(radioBO);
            entity.setSubjectId(bo.getId());
            subjectRadioMapper.insert(entity);
        }
    }

    @Override
    public void update(SubjectInfoBO bo) {
        // 先删除旧的，再插入新的
        LambdaQueryWrapper<SubjectRadio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectRadio::getSubjectId, bo.getId());
        subjectRadioMapper.delete(wrapper);
        add(bo);
    }

    @Override
    public SubjectInfoBO query(Long subjectId) {
        LambdaQueryWrapper<SubjectRadio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectRadio::getSubjectId, subjectId);
        List<SubjectRadio> list = subjectRadioMapper.selectList(wrapper);
        SubjectInfoBO bo = new SubjectInfoBO();
        bo.setRadioList(converter.convertToBOList(list));
        return bo;
    }
}
```

**工厂类：**
```java
@Component
public class SubjectTypeHandlerFactory {
    @Resource
    private List<SubjectTypeHandler> handlerList; // Spring 自动注入所有 Handler

    private final Map<Integer, SubjectTypeHandler> handlerMap = new HashMap<>();

    @PostConstruct
    public void register() {
        for (SubjectTypeHandler handler : handlerList) {
            handlerMap.put(handler.getHandlerType(), handler);
        }
    }

    public SubjectTypeHandler getHandler(Integer subjectType) {
        return handlerMap.get(subjectType);
    }
}
```

**使用方式：**
```java
@Service
public class SubjectDomainService {
    @Resource
    private SubjectTypeHandlerFactory factory;

    public void addSubject(SubjectInfoBO bo) {
        // 保存题目主表
        subjectInfoMapper.insert(entity);
        
        // 根据题型获取对应处理器
        SubjectTypeHandler handler = factory.getHandler(bo.getSubjectType());
        if (handler != null) {
            handler.add(bo);
        }
    }
}
```

#### 2.3.3 策略模式类图

```
SubjectTypeHandler (接口)
    │
    ├── RadioTypeHandler (type=1)  ← Spring 自动注册
    ├── MultipleTypeHandler (type=2) ← Spring 自动注册
    ├── JudgeTypeHandler (type=3)    ← Spring 自动注册
    └── BriefTypeHandler (type=4)    ← Spring 自动注册
              ↑
    SubjectTypeHandlerFactory
              ↑
    SubjectDomainService (调用方)
```

### 2.4 数据流转图

```
新增题目请求
    ↓
SubjectInfoController
    ↓ SubjectInfoDTOConverter.INSTANCE.convertToBO(dto)
SubjectDomainService
    ↓ 1. 保存题目主表 (subject_info)
    ↓ 2. 保存分类关联 (subject_mapping)
    ↓ 3. 获取策略处理器
    ↓ factory.getHandler(subjectType)
    ↓ 4. 保存题型数据 (subject_radio/multiple/judge/brief)
    ↓ 5. 同步到 ES
    ↓ syncToEs()
Elasticsearch
```

### 2.5 RocketMQ 异步点赞

#### 2.5.1 架构

```
用户点赞
    ↓
SubjectLikedController
    ↓ 构建消息
SubjectLikedProducer
    ↓ rocketMQTemplate.syncSend()
RocketMQ Broker
    ↓ 消息持久化
SubjectLikedConsumer (@RocketMQMessageListener)
    ↓ 解析消息
SubjectLikedMapper
    ↓ 写入数据库
subject_liked 表
```

#### 2.5.2 核心代码

**生产者：**
```java
@Component
public class SubjectLikedProducer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendLikedMessage(SubjectLikedMessage message) {
        rocketMQTemplate.syncSend("subject-liked-topic", JSON.toJSONString(message));
    }
}
```

**消费者：**
```java
@Component
@RocketMQMessageListener(
    topic = "subject-liked-topic",
    consumerGroup = "subject-liked-consumer-group"
)
public class SubjectLikedConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        SubjectLikedMessage msg = JSON.parseObject(message, SubjectLikedMessage.class);
        SubjectLiked liked = new SubjectLiked();
        liked.setSubjectId(msg.getSubjectId());
        liked.setLikedUserId(msg.getLikedUserId());
        liked.setLikedStatus(msg.getLikedStatus());
        subjectLikedMapper.insert(liked);
    }
}
```

### 2.6 Elasticsearch 全文搜索

#### 2.6.1 搜索架构

```
用户搜索 "Java"
    ↓
SubjectSearchController
    ↓
SubjectEsService.search()
    ↓
RestHighLevelClient
    ↓ multiMatchQuery("Java", "subjectName", "subjectParse", "subjectComment")
Elasticsearch (倒排索引)
    ↓ 按相关性排序
返回题目列表
```

#### 2.6.2 数据同步

题目增删改时同步更新 ES：

```java
public void addSubject(SubjectInfoBO bo) {
    // 1. MySQL 写入
    subjectInfoMapper.insert(entity);
    // 2. 题型数据写入
    handler.add(bo);
    // 3. ES 同步
    syncToEs(bo);
}

public void deleteSubject(Long subjectId) {
    // 1. MySQL 逻辑删除
    subjectInfoMapper.deleteById(subjectId);
    // 2. ES 删除
    subjectEsService.delete(subjectId);
}
```

#### 2.6.3 搜索查询

```java
public PageResult<SubjectEsDTO> search(String keyword, int pageNo, int pageSize) {
    SearchRequest request = new SearchRequest("subject");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(keyword)) {
        boolQuery.must(QueryBuilders.multiMatchQuery(
            keyword, "subjectName", "subjectParse", "subjectComment"
        ));
    }
    sourceBuilder.query(boolQuery);
    sourceBuilder.from((pageNo - 1) * pageSize);
    sourceBuilder.size(pageSize);
    sourceBuilder.sort("id", SortOrder.DESC);

    SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
    // 解析结果...
}
```

### 2.7 Subject 服务 API 清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /subject/info/add | 新增题目 |
| POST | /subject/info/update | 更新题目 |
| GET | /subject/info/query | 查询题目详情 |
| POST | /subject/info/delete | 删除题目 |
| GET | /subject/category/queryTree | 分类树 |
| GET | /subject/search/query | ES 全文搜索 |
| POST | /subject/liked/doLike | 点赞 (异步) |

### 2.8 关键设计模式总结

| 模式 | 应用场景 | 说明 |
|------|---------|------|
| **策略模式** | 4 种题型处理 | SubjectTypeHandler + Factory |
| **工厂模式** | Handler 注册 | @PostConstruct 自动注册 |
| **模板方法** | MyBatis-Plus CRUD | BaseMapper 提供基础方法 |
| **单例模式** | MapStruct Converter | INSTANCE 静态常量 |
| **观察者模式** | RocketMQ 消息 | Producer 发送 → Consumer 处理 |

---

## 学习总结

### Subject 服务学到的核心知识点
1. ✅ 策略模式 + 工厂模式组合应用
2. ✅ Spring 自动注入 List<Interface> 实现策略注册
3. ✅ RocketMQ 异步消息处理 (Producer + Consumer)
4. ✅ Elasticsearch 全文搜索 (RestHighLevelClient)
5. ✅ ES Bool 查询 + MultiMatch 多字段搜索
6. ✅ MySQL + ES 双写数据同步
7. ✅ 9 张表的复杂关联查询
8. ✅ DDD 分层在复杂业务中的应用
