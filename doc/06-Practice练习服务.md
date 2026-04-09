# 鸡翅Club (ColaCode) 学习笔记 - Practice 练习服务

## 第四章：Practice 练习服务

### 4.1 模块概览

Practice 练习服务是 ColaCode 的练习模块，用户可以创建套题、进行答题、查看成绩和历史记录。

**核心功能：**
- 📝 套题管理 (PracticeSet)
- 📋 套题题目关联 (PracticeSetDetail)
- ✅ 答题提交与自动评分
- 📊 正确率统计与历史记录
- 🔗 Feign 跨服务调用 Subject 获取题目

### 4.2 数据库设计

**4 张核心表：**

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| practice_set | 套题 | set_name, description, status |
| practice_set_detail | 套题-题目关联 | set_id, subject_id, sort |
| practice_info | 答题记录 | set_id, user_id, total_score, correct_count, wrong_count |
| practice_detail | 答题详情 | practice_id, subject_id, user_answer, correct_answer, is_correct |

**数据关系：**
```
practice_set (套题)
    └── practice_set_detail (套题包含哪些题目，按 sort 排序)
            └── subject_id (关联 Subject 服务的题目)

practice_info (一次答题记录)
    └── practice_detail (每道题的答题详情)
            └── user_answer (用户答案)
            └── correct_answer (正确答案)
            └── is_correct (是否正确)
```

### 4.3 Feign 跨服务调用

#### 4.3.1 为什么用 Feign？

微服务架构中，服务之间需要相互调用。Practice 需要获取题目信息，但题目数据在 Subject 服务中。

**调用方式对比：**

| 方式 | 优点 | 缺点 |
|------|------|------|
| **HTTP Client** | 灵活 | 需要手动序列化/反序列化 |
| **RestTemplate** | Spring 原生 | 代码冗长 |
| **Feign (推荐)** | 声明式、简洁 | 需要额外依赖 |

#### 4.3.2 Feign 使用步骤

**1. 添加依赖 (pom.xml)：**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

**2. 启用 Feign (启动类)：**
```java
@SpringBootApplication
@EnableFeignClients
public class PracticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PracticeApplication.class, args);
    }
}
```

**3. 定义 Feign 接口：**
```java
@FeignClient(
    name = "colacode-subject",                    // 服务名 (从 Nacos 获取)
    url = "${feign.subject.url:http://127.0.0.1:3010}"  // 本地开发直连地址
)
public interface SubjectFeignClient {

    @GetMapping("/subject/info/query")
    Result<SubjectInfoDTO> querySubject(@RequestParam("id") Long id);
}
```

**4. 注入并使用：**
```java
@Service
public class PracticeDomainService {
    @Resource
    private SubjectFeignClient subjectFeignClient;

    private SubjectInfoDTO getSubjectById(Long subjectId) {
        Result<SubjectInfoDTO> result = subjectFeignClient.querySubject(subjectId);
        if (result != null && result.isSuccess()) {
            return result.getData();
        }
        return null;
    }
}
```

#### 4.3.3 Feign 调用链路

```
PracticeController
    ↓
PracticeDomainService.submitPractice()
    ↓ 遍历用户答案
    ↓
SubjectFeignClient.querySubject(subjectId)
    ↓ HTTP GET http://127.0.0.1:3010/subject/info/query?id=xxx
Subject 服务
    ↓ 查询数据库
    ↓ 返回题目信息
Practice 服务
    ↓ 比对答案
    ↓ 计算得分
    ↓ 保存记录
```

#### 4.3.4 本地开发 vs 生产环境

**本地开发 (直连)：**
```yaml
feign:
  subject:
    url: http://127.0.0.1:3010
```

**生产环境 (服务发现)：**
```yaml
feign:
  subject:
    url:  # 不配置 url，通过 Nacos 发现服务
```

当配置了 `url` 时，Feign 会直连指定地址，不经过 Nacos。
当不配置 `url` 时，Feign 通过 `name` 从 Nacos 获取服务实例。

### 4.4 答题评分逻辑

#### 4.4.1 提交流程

```
用户提交答案
    ↓
PracticeSubmitBO { setId, userId, answers[] }
    ↓ 创建答题记录 (practice_info)
    ↓ 遍历每道题
    ↓
    ├─ Feign 调用 Subject 获取题目信息
    ├─ 比对用户答案和正确答案
    ├─ 记录答题详情 (practice_detail)
    └─ 统计正确/错误数量
    ↓
计算总分: totalScore = correctCount * 100 / totalCount
    ↓
更新答题记录
    ↓
返回成绩: PracticeInfoBO { id, totalScore, correctCount, wrongCount }
```

#### 4.4.2 核心代码

```java
public PracticeInfoBO submitPractice(PracticeSubmitBO submitBO) {
    int correctCount = 0;
    int totalCount = submitBO.getAnswers().size();

    // 1. 创建答题记录
    PracticeInfo practiceInfo = new PracticeInfo();
    practiceInfo.setSetId(submitBO.getSetId());
    practiceInfo.setUserId(submitBO.getUserId());
    practiceInfo.setSubmitTime(new Date());
    practiceInfoMapper.insert(practiceInfo);

    // 2. 遍历每道题，判分
    for (PracticeSubmitBO.AnswerItemBO answer : submitBO.getAnswers()) {
        SubjectInfoDTO subjectInfo = getSubjectById(answer.getSubjectId());
        if (subjectInfo == null) continue;

        boolean isCorrect = checkAnswer(subjectInfo, answer.getUserAnswer());
        if (isCorrect) correctCount++;

        // 保存答题详情
        PracticeDetail detail = new PracticeDetail();
        detail.setPracticeId(practiceInfo.getId());
        detail.setSubjectId(answer.getSubjectId());
        detail.setUserAnswer(answer.getUserAnswer());
        detail.setCorrectAnswer(getCorrectAnswer(subjectInfo));
        detail.setIsCorrect(isCorrect ? 1 : 0);
        practiceDetailMapper.insert(detail);
    }

    // 3. 计算总分并更新记录
    int wrongCount = totalCount - correctCount;
    int totalScore = totalCount > 0 ? (correctCount * 100 / totalCount) : 0;

    practiceInfo.setTotalScore(totalScore);
    practiceInfo.setCorrectCount(correctCount);
    practiceInfo.setWrongCount(wrongCount);
    practiceInfoMapper.updateById(practiceInfo);

    // 4. 返回成绩
    PracticeInfoBO resultBO = new PracticeInfoBO();
    resultBO.setId(practiceInfo.getId());
    resultBO.setTotalScore(totalScore);
    resultBO.setCorrectCount(correctCount);
    resultBO.setWrongCount(wrongCount);
    return resultBO;
}
```

#### 4.4.3 答案比对

```java
private boolean checkAnswer(SubjectInfoDTO subjectInfo, String userAnswer) {
    if (subjectInfo == null || userAnswer == null) return false;
    return subjectInfo.getSubjectParse() != null &&
            subjectInfo.getSubjectParse().trim().equalsIgnoreCase(userAnswer.trim());
}
```

> ⚠️ 当前实现是简化版，直接比对 `subjectParse` (题目解析) 字段。
> 实际项目中应该根据题型调用 Subject 服务的对应接口获取正确答案。

### 4.5 Practice 服务 API 清单

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| POST | /practice/set/add | 创建套题 | `{setName, description}` |
| POST | /practice/set/detail/add | 添加套题题目 | `[subjectId1, subjectId2, ...]` |
| GET | /practice/set/subjects | 获取套题题目列表 | `?setId=xxx` |
| POST | /practice/submit | 提交答题 | `{setId, userId, answers[]}` |
| GET | /practice/history | 查看历史记录 | `?userId=xxx` |

### 4.6 关键设计

#### 4.6.1 套题题目排序

```java
public void addPracticeSetDetail(Long setId, List<Long> subjectIds) {
    int sort = 1;
    for (Long subjectId : subjectIds) {
        PracticeSetDetail detail = new PracticeSetDetail();
        detail.setSetId(setId);
        detail.setSubjectId(subjectId);
        detail.setSort(sort++);  // 按添加顺序排序
        practiceSetDetailMapper.insert(detail);
    }
}
```

查询时按 `sort` 排序：
```java
wrapper.eq(PracticeSetDetail::getSetId, setId);
wrapper.orderByAsc(PracticeSetDetail::getSort);
```

#### 4.6.2 答题记录设计

```
一次答题 (practice_info)
├── practiceId: 1
├── setId: 10
├── userId: 100
├── totalScore: 80
├── correctCount: 4
├── wrongCount: 1
└── submitTime: 2026-04-05 18:00:00
    │
    └── 答题详情 (practice_detail)
        ├── detailId: 1, subjectId: 101, userAnswer: "A", isCorrect: 1
        ├── detailId: 2, subjectId: 102, userAnswer: "B", isCorrect: 1
        ├── detailId: 3, subjectId: 103, userAnswer: "C", isCorrect: 0
        ├── detailId: 4, subjectId: 104, userAnswer: "D", isCorrect: 1
        └── detailId: 5, subjectId: 105, userAnswer: "A", isCorrect: 1
```

#### 4.6.3 服务降级 (Fallback)

生产环境中，Subject 服务可能不可用。可以添加 Fallback：

```java
@FeignClient(
    name = "colacode-subject",
    url = "${feign.subject.url:http://127.0.0.1:3010}",
    fallback = SubjectFeignClientFallback.class
)
public interface SubjectFeignClient {
    @GetMapping("/subject/info/query")
    Result<SubjectInfoDTO> querySubject(@RequestParam("id") Long id);
}

@Component
public class SubjectFeignClientFallback implements SubjectFeignClient {
    @Override
    public Result<SubjectInfoDTO> querySubject(Long id) {
        return Result.fail("Subject 服务暂时不可用");
    }
}
```

### 4.7 微服务调用注意事项

#### 4.7.1 超时配置

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000    # 连接超时 5s
        readTimeout: 10000      # 读取超时 10s
```

#### 4.7.2 日志配置

```yaml
feign:
  client:
    config:
      default:
        loggerLevel: FULL  # NONE, BASIC, HEADERS, FULL
logging:
  level:
    com.colacode.practice.application.feign: DEBUG
```

#### 4.7.3 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| `No Feign Client for loadBalancing` | 缺少 LoadBalancer 依赖 | 添加 `spring-cloud-starter-loadbalancer` |
| `Connection refused` | 目标服务未启动 | 启动 Subject 服务 |
| `404 Not Found` | 路径不匹配 | 检查 `@GetMapping` 路径 |
| 序列化失败 | DTO 字段不匹配 | 确保 Feign DTO 与 Subject DTO 一致 |

---

## 踩坑记录

### Practice 服务踩坑

### 10. Feign 本地开发直连
本地开发时 Nacos 可能未启动，Feign 无法通过服务名发现实例。
**解决**: 配置 `url` 属性直连: `url = "${feign.subject.url:http://127.0.0.1:3010}"`

### 11. Feign 缺少 LoadBalancer 依赖
```
No Feign Client for loadBalancing
```
**解决**: 添加依赖:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

---

## 学习总结

### Practice 服务学到的核心知识点
1. ✅ Feign 声明式 HTTP 客户端
2. ✅ 微服务跨服务调用 (Practice → Subject)
3. ✅ Feign 本地直连 vs Nacos 服务发现
4. ✅ 答题评分逻辑实现
5. ✅ 套题-题目-答题记录 数据模型设计
6. ✅ 服务降级 (Fallback) 概念
7. ✅ Feign 超时配置与日志
