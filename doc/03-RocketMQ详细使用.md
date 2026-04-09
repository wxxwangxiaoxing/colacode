# RocketMQ 在 ColaCode 项目中的使用

## 1. 为什么使用 RocketMQ？

在题库服务中，点赞功能是一个高频操作。如果每次点赞都直接写数据库：
- **同步阻塞**: 用户需要等待数据库写入完成才能看到结果
- **性能瓶颈**: 高并发时数据库压力大
- **用户体验差**: 响应时间长

使用 RocketMQ 异步解耦后：
```
用户点赞 → 发送消息到 MQ → 立即返回成功 (10ms)
                           ↓
                      消费者异步写入数据库 (后台处理)
```

## 2. RocketMQ 核心概念

| 概念 | 说明 | 类比 |
|------|------|------|
| **Producer (生产者)** | 发送消息的应用 | 寄信人 |
| **Consumer (消费者)** | 接收并处理消息的应用 | 收信人 |
| **Topic (主题)** | 消息的分类，类似文件夹 | 邮箱分类 |
| **Message (消息)** | 传递的数据载体 | 信件内容 |
| **Tag (标签)** | 消息的子分类，类似标签 | 信封上的标记 |
| **Broker** | 消息存储服务器 | 邮局 |
| **NameServer** | 路由注册中心 | 邮局地址簿 |

## 3. 项目中的架构

```
SubjectLikedController
    ↓ 用户点赞请求
SubjectLikedProducer (生产者)
    ↓ rocketMQTemplate.syncSend()
RocketMQ Broker (消息中间件)
    ↓ 消息持久化
SubjectLikedConsumer (消费者)
    ↓ @RocketMQMessageListener 监听
SubjectLikedMapper
    ↓ 写入数据库
subject_liked 表
```

## 4. 代码实现

### 4.1 依赖配置

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.2.2</version>
</dependency>
```

### 4.2 配置文件

```yaml
# application.yml
spring:
  rocketmq:
    name-server: 127.0.0.1:9876    # NameServer 地址
    producer:
      group: colacode-subject-producer  # 生产者组名
```

### 4.3 消息定义

```java
@Data
public class SubjectLikedMessage implements Serializable {
    private Long subjectId;      // 题目ID
    private Long likedUserId;    // 点赞用户ID
    private Integer likedStatus; // 点赞状态 (1=点赞, 0=取消)
}
```

### 4.4 生产者

```java
@Component
public class SubjectLikedProducer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendLikedMessage(SubjectLikedMessage message) {
        // 同步发送消息
        rocketMQTemplate.syncSend("subject-liked-topic", JSON.toJSONString(message));
    }
}
```

### 4.5 消费者

```java
@Component
@RocketMQMessageListener(
    topic = "subject-liked-topic",           // 监听的主题
    consumerGroup = "subject-liked-consumer-group"  // 消费者组
)
public class SubjectLikedConsumer implements RocketMQListener<String> {
    
    @Resource
    private SubjectLikedMapper subjectLikedMapper;

    @Override
    public void onMessage(String message) {
        SubjectLikedMessage likedMessage = JSON.parseObject(message, SubjectLikedMessage.class);
        // 写入数据库
        SubjectLiked liked = new SubjectLiked();
        liked.setSubjectId(likedMessage.getSubjectId());
        // ...
        subjectLikedMapper.insert(liked);
    }
}
```

### 4.6 Controller 调用

```java
@PostMapping("/doLike")
public Result<Void> doLike(
        @RequestParam Long subjectId,
        @RequestParam Long userId,
        @RequestParam Integer likedStatus) {
    SubjectLikedMessage message = new SubjectLikedMessage();
    message.setSubjectId(subjectId);
    message.setLikedUserId(userId);
    message.setLikedStatus(likedStatus);
    // 异步发送，立即返回
    subjectLikedProducer.sendLikedMessage(message);
    return Result.success();
}
```

## 5. RocketMQ 发送方式对比

| 方式 | API | 特点 | 使用场景 |
|------|-----|------|----------|
| **同步发送** | `syncSend()` | 等待 Broker 确认 | 重要消息，如订单 |
| **异步发送** | `asyncSend()` + 回调 | 不阻塞，有回调通知 | 对响应时间敏感 |
| **单向发送** | `sendOneWay()` | 不等待确认 | 日志收集，不关心结果 |

## 6. 消息可靠性保证

### 6.1 消息不丢失的三个阶段

```
生产者 → Broker → 消费者
  ↓        ↓        ↓
同步确认  持久化存储  消费确认
```

1. **生产阶段**: 使用 `syncSend()` 同步发送，确保 Broker 收到
2. **存储阶段**: Broker 将消息持久化到磁盘
3. **消费阶段**: 消费者处理成功后自动 ACK

### 6.2 消费重试

消费者抛出异常时，RocketMQ 会自动重试：
- 最大重试次数: 16 次
- 重试间隔: 指数退避 (10s, 30s, 1min, 2min, ...)
- 超过最大次数: 进入死信队列 (DLQ)

## 7. 常见问题

### Q1: 本地开发没有 RocketMQ 怎么办？
本地开发时 RocketMQ 连接失败不影响服务启动（连接异常会被捕获）。但消息发送和消费功能无法使用。

### Q2: 如何安装 RocketMQ？
```bash
# Docker 方式
docker run -d \
  -p 9876:9876 \
  -p 10911:10911 \
  apache/rocketmq:4.9.3 \
  sh mqbroker -n localhost:9876

# 访问控制台 (可选)
docker run -d \
  -p 8080:8080 \
  -e "JAVA_OPTS=-Drocketmq.namesrv.addr=127.0.0.1:9876" \
  apacherocketmq/rocketmq-dashboard
```

### Q3: 消息顺序性如何保证？
RocketMQ 保证**同一 MessageQueue 内**的消息有序。点赞场景不需要顺序保证。

### Q4: 如何避免重复消费？
- **幂等性设计**: 每次消费操作都是幂等的
- **唯一标识**: 使用业务唯一键 (如 subjectId + userId) 做去重
