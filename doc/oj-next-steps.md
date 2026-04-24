# ColaCode OJ 后续计划与使用说明

## 1. 当前实现状态

本轮已经完成的内容：

- `gateway` 本地路由结构已调整为代码实际读取的 `colacode.gateway.routes.definitions`
- `practice` 已支持通过 `feign.subject.url` 本地直连 `subject`
- `subject` 已新增编程题模型支撑：
  - `subject_code`
  - `subject_code_case`
  - `CODE` 题型处理器
  - 内部判题详情接口 `/subject/code/judgeDetail`
- `practice` 已新增在线判题主链路：
  - 提交接口 `/practice/judge/submit`
  - 提交详情接口 `/practice/judge/submission/detail`
  - 提交列表接口 `/practice/judge/submission/list`
  - 异步判题执行服务
  - Judge0 客户端
  - 提交频率与代码长度限制
- 初始化 SQL 已补充：
  - `practice_submission`
  - `practice_submission_case`
  - `subject_code`
  - `subject_code_case`

当前还**没有完成或没有验证**的内容：

- 你本地尚未完成编译验证
- 数据库表尚未实际执行建表
- 尚未初始化一条编程题测试数据
- 尚未完成前端 OJ 页面
- 尚未做 AI 异步分析接入
- 尚未补测试用例

---

## 2. 你接下来建议按这个顺序推进

### 第一步：先编译并修掉首轮错误

建议先只看这两个模块：

- `colacode-subject`
- `colacode-practice`

目标：

- 确认新增 DTO / BO / Mapper / Entity / Controller 没有编译问题
- 确认 MapStruct、MyBatis Plus、Feign 相关类引用都正常

建议你本地优先执行：

```bash
mvn -pl colacode-subject,colacode-practice -am -DskipTests compile
```

如果这里报错，优先修：

1. MapStruct 转换方法缺失
2. MyBatis Plus 字段映射问题
3. DTO 包路径或导入错误
4. `ResultCodeEnum` / `BusinessException` 的枚举引用问题

---

## 3. 第二步：执行数据库脚本

需要把新增表同步到你的本地数据库。

关键脚本位置：

- [doc/infrastructure/sql/colacode-init.sql](D:/project/backend/colacode/doc/infrastructure/sql/colacode-init.sql)
- [doc/infrastructure/sql/seed-oj-minimal-problem.sql](D:/project/backend/colacode/doc/infrastructure/sql/seed-oj-minimal-problem.sql)

本次新增的表：

- `subject_code`
- `subject_code_case`
- `practice_submission`
- `practice_submission_case`

建议做法：

- 如果是全新初始化库，直接用完整脚本
- 如果你已有开发库，建议把这四张表单独拆成增量 SQL 执行，避免覆盖现有数据

执行后请确认：

- 表创建成功
- JSON 字段可正常保存
- 生成列和唯一索引没有报错

---

## 4. 第三步：初始化一条编程题数据

要先准备一条最小可联调的数据，不然后端接口通了也没法验。

推荐直接执行：

- [doc/infrastructure/sql/seed-oj-minimal-problem.sql](D:/project/backend/colacode/doc/infrastructure/sql/seed-oj-minimal-problem.sql)

建议先造一题：

- 题目名：两数之和
- 类型：`subject_type = 5`
- 判题模式：`STANDARD_IO`
- 支持语言：`java/python/cpp`
- 2 个样例用例
- 3 个隐藏用例

最少需要插入的数据包括：

1. `subject_info`
2. `subject_mapping`
3. `subject_code`
4. `subject_code_case`

注意：

- `subject_info.subject_type` 要设为 `5`
- `subject_code_case` 至少要有一个 `is_sample = 1`

---

## 5. 第四步：启动最小联调链路

推荐启动顺序：

1. MySQL
2. Redis
3. Judge0
4. `subject`
5. `practice`
6. `gateway`

Judge0 本地验证建议：

```bash
curl -X POST "http://localhost:2358/submissions?base64_encoded=false&wait=false" \
  -H "Content-Type: application/json" \
  -d "{\"source_code\":\"print(42)\",\"language_id\":71}"
```

如果 Judge0 不通，先不要继续查业务代码，先把 Judge0 自身联通性解决。

---

## 6. 第五步：按接口逐个验证

建议按这个顺序测。

### 6.1 验证题目详情接口

验证：

- `/subject/info/query?id=xxx`

你要重点看返回里是否包含：

- `codeConfig`
- `testCases`

如果没有，大概率是 `SubjectDomainService` 的题型详情回填还有问题。

### 6.2 验证内部判题详情接口

验证：

- `/subject/code/judgeDetail?id=xxx`

你要重点看：

- 是否返回全部测试用例
- 是否包含隐藏用例
- `timeLimitMs / memoryLimitKb` 是否正确

### 6.3 验证提交接口

验证：

- `POST /practice/judge/submit`

请求示例：

```json
{
  "subjectId": 1001,
  "language": "python",
  "code": "print(sum(map(int, input().split())))"
}
```

预期：

- 返回 `submissionId`
- 初始状态为 `PENDING`

### 6.4 验证提交详情接口

验证：

- `/practice/judge/submission/detail?id=xxx`

预期状态流转：

- `PENDING`
- `RUNNING`
- `AC / WA / TLE / RE / CE / SYSTEM_ERROR`

### 6.5 验证提交列表接口

验证：

- `/practice/judge/submission/list?subjectId=xxx&pageNo=1&pageSize=20`

预期：

- 能看到当前用户最近提交记录

---

## 7. 你编译或联调时最可能遇到的问题

### 7.1 MapStruct 没生成转换实现

现象：

- `...ConverterImpl` 找不到
- 编译时报 mapping 方法缺失

处理：

- 先确认 `mapstruct-processor` 已启用
- 再看新增字段是否需要显式映射

### 7.2 MyBatis Plus 字段映射失败

现象：

- 新增表字段查出来全是 `null`
- 插入成功但查询不对

处理：

- 重点看新实体上的 `@TableField`
- 先排查下划线字段和驼峰字段是否对应

### 7.3 Feign 本地调用失败

现象：

- `practice` 调 `subject` 报连接失败

处理：

- 检查 `feign.subject.url`
- 确认 `subject` 本地端口
- 确认 local profile 是否生效

### 7.4 Judge0 可访问但判题一直超时

现象：

- 一直停在轮询
- 最终 `SYSTEM_ERROR`

处理：

- 先查 Judge0 容器状态
- 再看语言 ID 是否正确
- 再看代码是否本身卡死

### 7.5 Redis 限流影响重复调试

现象：

- 明明代码没问题，但接口返回“提交过于频繁”

处理：

- 临时调大：
  - `judge.max-submit-per-minute`
  - `judge.submit-cooldown-seconds`

---

## 8. 下一阶段开发建议

当后端主链路确认跑通后，建议按下面顺序继续：

### A. 完成 `run` 能力

新增：

- `/practice/judge/run`

目标：

- 只跑样例
- 不进入正式提交记录
- 返回 stdout / stderr / time / memory

### B. 完成前端 OJ 页面

新增页面：

- `/oj/problem/:id`

建议最小功能：

- 题目描述
- 语言切换
- 代码编辑器
- 提交按钮
- 结果展示
- 提交历史列表

### C. 增加 AI 异步分析

建议先做成非阻塞：

- 提交完成后异步写 `ai_status`
- 后续单独刷新 `ai_feedback`

### D. 补测试

建议至少补：

- 输出比较器测试
- 语言映射测试
- 状态映射测试
- 提交接口 happy path 测试

---

## 9. 我建议你下一次优先发给我的内容

为了让我继续高效接力，下一次你可以直接给我下面任一种信息：

### 方式 1：贴编译错误

把第一轮 `mvn compile` 的报错贴给我，我直接帮你逐个修。

### 方式 2：贴接口返回

把下面几个接口的实际返回贴给我：

- `/subject/info/query`
- `/subject/code/judgeDetail`
- `/practice/judge/submit`
- `/practice/judge/submission/detail`

我可以直接判断是数据问题、配置问题还是代码问题。

### 方式 3：让我继续补功能

你可以直接让我继续做其中一个：

- 补 `/practice/judge/run`
- 补初始化测试数据 SQL
- 补前端 OJ 页面
- 补 AI 异步分析骨架

---

## 10. 这份文档对应的关键代码位置

### Subject

- [SubjectDomainService.java](D:/project/backend/colacode/colacode-subject/src/main/java/com/colacode/subject/domain/service/SubjectDomainService.java)
- [SubjectCodeDomainService.java](D:/project/backend/colacode/colacode-subject/src/main/java/com/colacode/subject/domain/service/SubjectCodeDomainService.java)
- [CodeTypeHandler.java](D:/project/backend/colacode/colacode-subject/src/main/java/com/colacode/subject/domain/strategy/CodeTypeHandler.java)
- [SubjectCodeController.java](D:/project/backend/colacode/colacode-subject/src/main/java/com/colacode/subject/application/controller/SubjectCodeController.java)

### Practice

- [JudgeController.java](D:/project/backend/colacode/colacode-practice/src/main/java/com/colacode/practice/application/controller/JudgeController.java)
- [JudgeSubmissionDomainService.java](D:/project/backend/colacode/colacode-practice/src/main/java/com/colacode/practice/domain/service/JudgeSubmissionDomainService.java)
- [JudgeSubmissionExecutionService.java](D:/project/backend/colacode/colacode-practice/src/main/java/com/colacode/practice/domain/service/JudgeSubmissionExecutionService.java)
- [Judge0Client.java](D:/project/backend/colacode/colacode-practice/src/main/java/com/colacode/practice/infra/judge/Judge0Client.java)
- [JudgeSecurityService.java](D:/project/backend/colacode/colacode-practice/src/main/java/com/colacode/practice/domain/service/JudgeSecurityService.java)

### 配置与 SQL

- [colacode-gateway/src/main/resources/application-local.yml](D:/project/backend/colacode/colacode-gateway/src/main/resources/application-local.yml)
- [colacode-practice/src/main/resources/application.yml](D:/project/backend/colacode/colacode-practice/src/main/resources/application.yml)
- [colacode-practice/src/main/resources/application-local.yml](D:/project/backend/colacode/colacode-practice/src/main/resources/application-local.yml)
- [doc/infrastructure/sql/colacode-init.sql](D:/project/backend/colacode/doc/infrastructure/sql/colacode-init.sql)

---

## 11. 一句话建议

你现在最值钱的动作不是继续加功能，而是：

**先编译，建表，造一题，跑通一次真实提交。**

这一步通了，后面的前端、AI、排行榜都会顺很多。
