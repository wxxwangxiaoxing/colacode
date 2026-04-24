# ColaCode 学习笔记 - Interview 面试服务

## 第五章：Interview 面试服务

### 5.1 模块概览

Interview 面试服务是 ColaCode 的 AI 模拟面试模块，支持两种面试引擎：本地题库引擎和 AI 大模型引擎。

**核心功能：**
- 📄 简历分析 (提取技术关键词)
- 🎯 智能出题 (根据关键词生成面试题)
- 📝 在线答题 (用户作答)
- 📊 AI 评分 (自动评分 + 评价建议)
- 📚 历史记录 (查看过往面试)

### 5.2 数据库设计

**2 张核心表：**

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| interview_history | 面试记录 | avg_score, key_words, tip, interview_url |
| interview_question_history | 答题详情 | interview_id, score, key_words, question, answer, user_answer |

**关系：** 一对多 (一次面试 → 多道题目)

### 5.3 策略模式 - 面试引擎

#### 5.3.1 架构设计

```
InterviewEngine (策略接口)
    ├── engineType()     → 返回引擎类型
    ├── analyse()        → 简历分析
    ├── start()          → 开始面试 (出题)
    └── submit()         → 提交答案 (评分)

具体实现:
    ├── DatabaseInterviewEngine (DATABASE)  ← 本地题库
    └── AiInterviewEngine (AI)              ← AI大模型
```

#### 5.3.2 引擎注册机制

```java
@Service
public class InterviewDomainService {
    @Resource
    private List<InterviewEngine> engineList; // Spring 自动注入所有实现

    private Map<String, InterviewEngine> engineMap = new HashMap<>();

    @PostConstruct
    public void registerEngines() {
        for (InterviewEngine engine : engineList) {
            engineMap.put(engine.engineType(), engine);
        }
    }
}
```

**优势：** 新增引擎只需实现 `InterviewEngine` 接口并添加 `@Component`，无需修改现有代码 (开闭原则)。

#### 5.3.3 两种引擎对比

| 特性 | Database 引擎 | AI 引擎 |
|------|--------------|---------|
| 出题方式 | 从本地题库随机抽取 | 调用大模型 API 生成 |
| 评分方式 | 使用用户自评分 | 调用大模型 API 评分 |
| 依赖 | 无外部依赖 | 需要 AI API Key |
| 适用场景 | 离线/本地环境 | 在线/智能环境 |

### 5.4 面试流程

```
用户提交简历 (PDF URL)
    ↓
1. 简历分析 (/interview/analyse)
    ↓ 提取技术关键词
    ↓ 返回: ["Java", "MySQL", "Redis", "Spring"]
    ↓
2. 选择关键词开始面试 (/interview/start)
    ↓ 用户选择感兴趣的关键词
    ↓ 引擎根据关键词出题 (最多8道)
    ↓ 返回: [{题目, 参考答案, 关键词}]
    ↓
3. 用户作答并提交 (/interview/submit)
    ↓ 引擎评分
    ↓ 计算平均分
    ↓ 生成评价建议
    ↓ 保存面试记录
    ↓ 返回: {avgScore, tips, avgTips}
    ↓
4. 查看历史 (/interview/history)
5. 查看详情 (/interview/detail?id=X)
```

### 5.5 Interview 服务 API 清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /interview/analyse | 简历分析，提取技术关键词 |
| POST | /interview/start | 开始面试，根据关键词出题 |
| POST | /interview/submit | 提交答案，评分并保存记录 |
| POST | /interview/history | 查看面试历史列表 |
| GET | /interview/detail | 查看某次面试的答题详情 |

### 5.6 关键设计

#### 5.6.1 评分算法

```java
// 计算平均分
double avgScore = totalScore / questionCount;

// 生成评价
if (avgScore >= 4) return "整体表现优秀，基础扎实！";
if (avgScore >= 3) return "整体表现良好，部分知识需要巩固。";
if (avgScore >= 2) return "基础一般，建议系统复习。";
return "基础薄弱，建议从基础开始学习。";
```

#### 5.6.2 AI 引擎扩展

当前 AI 引擎是预留接口，实际接入大模型只需修改：

```java
private String callAiGenerateQuestion(String keyword) {
    // 调用阿里云百炼 / OpenAI / 通义千问 API
    String prompt = "根据关键字 " + keyword + " 生成1道面试题";
    return aiClient.chat(prompt);
}

private double callAiScoreAnswer(String question, String userAnswer) {
    String prompt = "请根据题目和用户答案评分(0-5分): " + question + "\n" + userAnswer;
    String response = aiClient.chat(prompt);
    return parseScore(response);
}
```

配置:
```yaml
interview:
  ai:
    enabled: true
    api-key: "your-api-key"
```

### 5.7 策略模式总结

ColaCode 项目中策略模式的应用：

| 模块 | 策略接口 | 具体策略 |
|------|---------|---------|
| Subject | SubjectTypeHandler | Radio/Multiple/Judge/Brief |
| Interview | InterviewEngine | Database/AI |
| OSS (待实现) | StorageAdapter | MinIO/AliyunOSS |
| Wx (待实现) | WxMessageHandler | Text/Image/Event |

---

## 学习总结

### Interview 服务学到的核心知识点
1. ✅ 策略模式在面试引擎中的应用
2. ✅ Spring 自动注入 List<Interface> 实现策略注册
3. ✅ 面试流程设计 (分析 → 出题 → 作答 → 评分)
4. ✅ AI 大模型接口预留设计
5. ✅ 面试历史记录与详情查询
6. ✅ 开闭原则的实际应用
