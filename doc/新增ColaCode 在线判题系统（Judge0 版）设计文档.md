---

# ColaCode 在线判题系统（Judge0 版）设计文档

## 一、项目定位

**产品名称**：ColaCode Online Judge（OJ）  
**定位**：在现有 ColaCode 微服务体系内，新增类似 LeetCode 的在线编程、自动判题与 AI 讲解能力。

**核心闭环**：  
`选择编程题 -> 编写代码 -> 提交判题 -> 查看结果 -> AI 讲解`

**当前阶段边界**：
- 仅接入 **Judge0**
- 仅支持 **标准输入输出型编程题**
- 暂不支持 Hadoop / Spark / Hive 等大数据题
- 目标是交付一个**可运行、可判题、可查看提交记录**的 OJ MVP

---

## 二、与现有系统的关系

### 2.1 现有服务基础

当前仓库已具备：
- `colacode-gateway`：统一网关
- `colacode-auth`：认证与权限
- `colacode-subject`：题库服务
- `colacode-practice`：练习与提交记录
- `colacode-ai`：AI 能力服务
- `colacode-common`：统一返回体、异常、TraceId、Feign 透传

### 2.2 OJ 的服务边界

本期**不新建独立微服务**，采用“两端扩展”的方式：

- **Subject 服务**：扩展“编程题”的题目元数据与测试用例
- **Practice 服务**：扩展 Judge 模块，负责代码提交、调用 Judge0、保存判题记录
- **AI 服务**：复用已有能力，对判题结果做异步分析
- **Gateway**：复用现有统一入口

**请求链路**：

```text
前端 -> Gateway -> Practice(Judge模块) -> Judge0
                    |
                    -> Subject(题目与测试用例)
                    |
                    -> AI(异步分析，可选)
```

### 2.3 设计原则

- 优先复用现有微服务，不额外拆服务
- 编程题属于“题库能力”，放在 Subject
- 判题属于“练习/提交行为”，放在 Practice
- AI 分析不阻塞判题主链路
- MVP 先保证“判题可用”，再逐步提升体验

---

## 三、核心功能范围

### 3.1 本期必须交付

- 支持新增编程题
- 支持题目详情查询
- 支持选择语言与编辑代码
- 支持提交代码
- 支持异步查询判题结果
- 支持返回统一状态：
  `PENDING / RUNNING / AC / WA / TLE / RE / CE / SYSTEM_ERROR`
- 支持查看个人提交记录与单次详情

### 3.2 本期不做

- 自定义沙箱
- 交互式题目
- 特判器（Special Judge）
- 多文件工程型题目
- 并发评测队列优化
- 代码查重
- 比赛模式

---

## 四、题目模型设计

## 4.1 为什么不能直接扩展 `problem` 表

当前 ColaCode 题库并不是单一 `problem` 表模型，而是：

- 题目主表：`subject_info`
- 分类映射：`subject_mapping`
- 题型明细表：
  `subject_radio / subject_multiple / subject_judge / subject_brief`
- 题型处理走 `SubjectTypeHandler` 策略分发

因此，编程题必须按现有架构接入，而不是单独发明一套脱离 Subject 的单表模型。

## 4.2 编程题接入方案

在 Subject 服务新增一种题型：`CODE`

建议约定：
- `subject_type = 5` 表示编程题

新增两张表：

### 表一：`subject_code`

存放编程题元信息。

```sql
CREATE TABLE `subject_code` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL COMMENT '关联 subject_info.id',
  `judge_mode` VARCHAR(20) NOT NULL DEFAULT 'STANDARD_IO',
  `time_limit_ms` INT NOT NULL DEFAULT 1000,
  `memory_limit_kb` INT NOT NULL DEFAULT 131072,
  `supported_languages_json` JSON NOT NULL,
  `template_code_json` JSON NULL,
  `input_example` TEXT NULL,
  `output_example` TEXT NULL,
  `created_by` VARCHAR(32) DEFAULT NULL,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(32) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_subject_id` (`subject_id`)
);
```

### 表二：`subject_code_case`

存放测试用例。

```sql
CREATE TABLE `subject_code_case` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL,
  `case_no` INT NOT NULL,
  `stdin_text` TEXT NULL,
  `expected_stdout` TEXT NULL,
  `is_sample` TINYINT(1) NOT NULL DEFAULT 0,
  `score` INT NOT NULL DEFAULT 1,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_subject_case_no` (`subject_id`, `case_no`)
);
```

## 4.3 编程题 DTO 示例

```json
{
  "id": 1001,
  "subjectName": "两数之和",
  "subjectType": 5,
  "subjectDifficulty": 1,
  "subjectComment": "给定两个整数，输出它们之和",
  "codeConfig": {
    "judgeMode": "STANDARD_IO",
    "timeLimitMs": 1000,
    "memoryLimitKb": 131072,
    "supportedLanguages": ["java", "python", "cpp"],
    "templateCode": {
      "java": "public class Main { public static void main(String[] args) { } }",
      "python": "def solve():\n    pass\n\nif __name__ == '__main__':\n    solve()"
    },
    "inputExample": "2 7",
    "outputExample": "9"
  }
}
```

---

## 五、提交与判题模型设计

## 5.1 为什么不能做同步阻塞判题

如果接口收到提交后一直阻塞等待 Judge0 返回，存在几个问题：

- Gateway 与 Practice 请求线程被长时间占用
- Judge0 超时会直接拖慢用户请求
- 前端体验不稳定
- 后续扩展到多测试用例时会更难维护

因此本期采用**异步提交 + 前端轮询查询**模式。

## 5.2 判题状态机

系统定义统一状态：

- `PENDING`：已创建提交，尚未开始判题
- `RUNNING`：判题中
- `AC`：全部通过
- `WA`：答案错误
- `TLE`：超时
- `RE`：运行错误
- `CE`：编译错误
- `SYSTEM_ERROR`：系统异常或 Judge0 调用失败

## 5.3 提交流程

```text
1. 用户点击“提交”
2. Practice 创建 submission 记录，状态为 PENDING
3. Practice 拉取该题所有测试用例
4. Practice 逐个调用 Judge0 创建提交
5. Practice 轮询 Judge0 token 结果
6. 每个测试用例得到执行结果后进行结果映射
7. 汇总所有 case，生成最终 submission 状态
8. 写回数据库
9. 如开启 AI 分析，则异步触发 AI 服务
10. 前端通过 submissionId 查询最终结果
```

## 5.4 Judge0 调用方式

推荐使用：

```http
POST /submissions?base64_encoded=false&wait=false
```

请求示例：

```json
{
  "source_code": "print(sum(map(int, input().split())))",
  "language_id": 71,
  "stdin": "2 7"
}
```

返回：

```json
{ "token": "xxx" }
```

查询：

```http
GET /submissions/{token}?base64_encoded=false
```

## 5.5 判题核心逻辑

Judge0 负责：
- 编译
- 执行
- 资源限制
- 返回 `stdout / stderr / compile_output / time / memory / status`

业务系统负责：
- 测试用例组织
- 多 case 汇总
- 状态映射
- 提交记录持久化
- AI 分析触发

伪代码：

```java
SubmissionResult finalResult = new SubmissionResult();
for (TestCase tc : testCases) {
    Judge0Result result = judge0Client.run(code, languageId, tc.stdin);
    CaseResult caseResult = mapJudge0Result(result, tc.expectedOutput);
    saveCaseResult(caseResult);

    if (caseResult.status != AC) {
        finalResult.status = caseResult.status;
        finalResult.failedCaseNo = tc.caseNo;
        finalResult.stdout = caseResult.stdout;
        finalResult.stderr = caseResult.stderr;
        break;
    }
}
if (allCasesPassed) {
    finalResult.status = AC;
}
updateSubmission(finalResult);
```

## 5.6 输出比对规则

MVP 阶段采用最简单稳定的规则：

- 默认 `trim()` 后进行字符串全等比较
- 忽略末尾空白差异
- 不做浮点误差兼容
- 不做 special judge

建议封装为独立比较器，后续便于扩展。

---

## 六、数据库设计

## 6.1 提交主表：`practice_submission`

放在 Practice 库中。

```sql
CREATE TABLE `practice_submission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `language` VARCHAR(20) NOT NULL,
  `language_id` INT NOT NULL,
  `code` MEDIUMTEXT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `pass_case_count` INT NOT NULL DEFAULT 0,
  `total_case_count` INT NOT NULL DEFAULT 0,
  `execute_time_ms` INT DEFAULT NULL,
  `memory_used_kb` INT DEFAULT NULL,
  `judge_message` VARCHAR(255) DEFAULT NULL,
  `stdout_preview` TEXT NULL,
  `stderr_preview` TEXT NULL,
  `ai_status` VARCHAR(20) DEFAULT NULL,
  `ai_feedback` TEXT NULL,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_subject` (`user_id`, `subject_id`),
  INDEX `idx_user_created_time` (`user_id`, `created_time`)
);
```

## 6.2 测试点结果表：`practice_submission_case`

```sql
CREATE TABLE `practice_submission_case` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `submission_id` BIGINT NOT NULL,
  `case_no` INT NOT NULL,
  `is_sample` TINYINT(1) NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL,
  `stdin_text` TEXT NULL,
  `expected_stdout` TEXT NULL,
  `actual_stdout` TEXT NULL,
  `stderr_text` TEXT NULL,
  `execute_time_ms` INT DEFAULT NULL,
  `memory_used_kb` INT DEFAULT NULL,
  `judge_token` VARCHAR(100) DEFAULT NULL,
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_submission_case_no` (`submission_id`, `case_no`)
);
```

## 6.3 是否复用 `practice_info / practice_detail`

不建议直接复用。

原因：
- 现有 `practice_info / practice_detail` 偏“练习答题记录”
- OJ 提交记录是独立生命周期
- 编程题会产生多次提交、逐点结果、编译输出、运行错误等信息
- 强行复用会让原有客观题/主观题练习模型变复杂

因此本期建议新增独立 `submission` 体系。

---

## 七、后端接口设计

## 7.1 Subject 服务

### 查询编程题详情
```http
GET /subject/code/query?id=1001
```

### 新增编程题
```http
POST /subject/code/add
```

### 更新编程题
```http
POST /subject/code/update
```

---

## 7.2 Practice 服务

### 创建提交
```http
POST /practice/judge/submit
```

请求：

```json
{
  "subjectId": 1001,
  "language": "python",
  "code": "print(sum(map(int, input().split())))"
}
```

响应：

```json
{
  "submissionId": 90001,
  "status": "PENDING"
}
```

### 查询提交详情
```http
GET /practice/judge/submission/detail?id=90001
```

### 查询我的提交列表
```http
GET /practice/judge/submission/list?subjectId=1001&pageNo=1&pageSize=20
```

### 仅运行样例
```http
POST /practice/judge/run
```

说明：
- `run` 只跑样例测试用例
- `submit` 跑全部测试用例
- `run` 的结果不进入正式排行榜与通过统计

---

## 八、前端页面设计

新增页面路由：

```text
/oj/problem/:id
```

页面布局：

- 左侧：题目描述、输入输出示例、限制说明
- 右侧顶部：语言选择
- 右侧中部：Monaco Editor
- 右侧底部：运行结果 / 提交结果 / AI 分析
- 侧边区域：历史提交记录

页面能力：

- 切换语言时自动加载模板代码
- 支持“运行样例”
- 支持“提交判题”
- 提交后展示轮询中的状态
- 支持查看失败测试点摘要

---

## 九、Judge0 与语言映射

## 9.1 平台内部语言标识

建议统一保存：

- `java`
- `python`
- `cpp`

## 9.2 与 Judge0 `language_id` 映射

建议配置化，不写死在业务逻辑中。

示例：

```yaml
judge:
  languages:
    java: 62
    python: 71
    cpp: 54
```

由 Practice 服务读取配置并映射。

---

## 十、安全与稳定性设计

Judge0 只是执行沙箱，不代表应用层可以不做限制。

必须补充：

- 代码长度限制：默认 64KB
- 单用户提交限流：例如每分钟 20 次
- 单题短时重复提交限制：例如 5 秒内最多 1 次
- Practice 到 Judge0 走内网地址，不暴露公网
- 查询 Judge0 时设置最大轮询次数和总超时时间
- 提交失败要有兜底状态：`SYSTEM_ERROR`

建议后续补充：

- 恶意关键字检测
- IP 维度风控
- 提交队列化
- Judge0 高可用部署

---

## 十一、AI 能力接入

AI 不进入主判题链路，只做异步增强。

## 11.1 输入

- 题目标题与描述
- 用户代码
- 提交状态
- 编译错误 / 运行错误 / 错误输出
- 首个失败测试点摘要

## 11.2 输出

- 错误原因解释
- 修复建议
- 复杂度优化建议
- 边界条件提醒

## 11.3 接入方式

在 submission 状态落库后异步触发：
- 可先用线程池
- 后续可升级为 MQ

注意：
- 当前 AI 服务默认 Bean 选择需要梳理，不能直接假设一定走真实模型
- 配置中的敏感 key 必须改为环境变量注入，禁止默认值落在仓库

---

## 十二、落地前置修复项

在正式进入 OJ 开发前，建议先修三件基础问题。

### 12.1 修复 Gateway 本地路由配置
当前本地配置和代码读取前缀不一致，需要统一，否则本地网关可能不转发。

### 12.2 修复 Practice 本地 Feign 直连
当前 `practice` 本地配置里有 `feign.subject.url`，但 Feign 客户端未绑定 `url`，local 模式下跨服务调用有风险。

### 12.3 梳理 AI 服务 Bean 选择与密钥配置
明确开发环境到底使用 Mock 还是 Real，移除仓库中的默认密钥形态值。

这三项建议作为 **Phase 0**。

---

## 十三、实施路线图

## Phase 0：基础修复与环境打通
目标：
- 修复 Gateway 本地路由
- 修复 Practice 本地 Feign 直连
- 启动 Judge0 本地容器
- 完成 Practice 到 Judge0 的 HTTP 联通验证

验收标准：
- 本地能通过 Gateway 正常访问 Practice
- Practice 能调用 Subject
- Practice 能成功调用 Judge0 并拿到 token/result

## Phase 1：OJ MVP
目标：
- Subject 支持 `CODE` 题型
- 支持样例与隐藏测试用例
- Practice 支持创建提交与查询结果
- 前端支持编程题详情、代码编辑、提交、查看结果
- 完成 `AC / WA / TLE / RE / CE / SYSTEM_ERROR` 状态流转

验收标准：
- 用户可完成一次完整“写代码 -> 提交 -> 查看判题结果”
- 至少有一道编程题可完整跑通

## Phase 2：体验完善
目标：
- 增加“运行样例”
- 增加提交记录列表
- 增加失败测试点摘要
- 展示执行时间与内存
- 增加模板代码与语言切换

验收标准：
- 用户可快速调试代码
- 用户可查看历史提交差异

## Phase 3：AI 增强
目标：
- 异步 AI 错误讲解
- AI 优化建议
- AI 代码解释

验收标准：
- WA / CE / RE 场景下可返回可读分析文本

## Phase 4：远期扩展
目标：
- special judge
- 比赛模式
- 排行榜
- 社区讨论区联动
- 大数据题型扩展

---

## 十四、部署与验证

## 14.1 Judge0 启动

建议优先使用官方推荐的 docker compose 方案；如果只是本地联调，可先用单容器验证 API 可达性。

示例：

```bash
docker run -d -p 2358:2358 --name judge0 judge0/judge0:1.13.1
```

## 14.2 联调验证

```bash
curl -X POST http://localhost:2358/submissions?base64_encoded=false&wait=false \
  -H "Content-Type: application/json" \
  -d "{\"source_code\":\"print(42)\",\"language_id\":71}"
```

然后查询 token 对应结果，确认能返回：

- `status`
- `stdout`
- `time`
- `memory`

---

