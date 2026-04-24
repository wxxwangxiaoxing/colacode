# ColaCode 学习笔记 - Circle 社区服务

## 第三章：Circle 社区服务

### 3.1 模块概览

Circle 社区服务是 ColaCode 的社交核心，提供动态发布、评论回复、私信聊天等功能。

**核心功能：**
- 📝 圈子发布 (ShareCircle)
- 📸 动态发布 (ShareMoment)
- 💬 评论回复 (ShareCommentReply)
- 📨 私信消息 (ShareMessage)
- 🔌 WebSocket 实时通信 (ChickenSocket)
- 🛡️ DFA 敏感词过滤

### 3.2 数据库设计

**4 张核心表：**

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| share_circle | 圈子 | title, content, userId, likedCount, commentCount |
| share_moment | 动态 | content, images, userId, likedCount |
| share_comment_reply | 评论回复 | momentId, circleId, userId, content, replyUserId, type |
| share_message | 私信消息 | fromUserId, toUserId, content, messageType, status |

**评论类型 (type)：**
- `1` = 动态评论
- `2` = 圈子评论

### 3.3 DFA 敏感词过滤算法

#### 3.3.1 什么是 DFA？

DFA (Deterministic Finite Automaton) 确定有限状态自动机，是一种高效的字符串匹配算法。

**为什么不用 `String.contains()`？**
```java
// 传统方式: O(n*m) 时间复杂度，每个敏感词都要遍历一次文本
for (String word : sensitiveWords) {
    if (text.contains(word)) { ... }
}

// DFA 方式: O(n) 时间复杂度，只需遍历一次文本
dfaFilter.filter(text);
```

#### 3.3.2 Trie 树结构

```
添加敏感词: ["敏感词", "违规", "敏感"]

构建 Trie 树:
        root
       /    \
     敏      违
    /  \      \
  感    词*   规*
   \
    词*

* 表示结束标记 (isEnd=true)
```

#### 3.3.3 过滤过程

```
输入文本: "这是一段敏感词内容，请勿违规"

遍历过程:
"这" → 不在树中 → 保留
"是" → 不在树中 → 保留
"一" → 不在树中 → 保留
"段" → 不在树中 → 保留
"敏" → 在树中 → 继续匹配
  "感" → 在树中 → 继续匹配
    "词" → 在树中且 isEnd=true → 匹配到"敏感词" → 替换为 "***"
"内" → 不在树中 → 保留
"容" → 不在树中 → 保留
"，" → 不在树中 → 保留
"请" → 不在树中 → 保留
"勿" → 不在树中 → 保留
"违" → 在树中 → 继续匹配
  "规" → 在树中且 isEnd=true → 匹配到"违规" → 替换为 "**"

输出: "这是一段***内容，请勿**"
```

#### 3.3.4 代码实现

```java
public class DFAFilter {
    // Trie 树: Map<字符, 子Map>
    private final Map<Object, Object> sensitiveWordMap = new HashMap<>();
    private static final String END_FLAG = "isEnd";

    // 添加敏感词
    public void addWord(String word) {
        Map<Object, Object> currentMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object subMap = currentMap.get(c);
            if (subMap == null) {
                Map<Object, Object> newMap = new HashMap<>();
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Object, Object>) subMap;
            }
        }
        currentMap.put(END_FLAG, true); // 标记结束
    }

    // 过滤文本
    public String filter(String text) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            Map<Object, Object> currentMap = sensitiveWordMap;
            int matchLength = 0;
            int j = i;
            boolean found = false;

            while (j < text.length()) {
                char c = text.charAt(j);
                Object subMap = currentMap.get(c);
                if (subMap == null) break;

                currentMap = (Map<Object, Object>) subMap;
                matchLength++;
                j++;

                if (currentMap.containsKey(END_FLAG)) {
                    found = true; // 找到完整匹配，但继续找最长匹配
                }
            }

            if (found) {
                for (int k = 0; k < matchLength; k++) {
                    result.append("*");
                }
                i += matchLength;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
}
```

#### 3.3.5 在 Circle 服务中的使用

```java
@Service
public class CircleDomainService {
    private DFAFilter dfaFilter;

    @PostConstruct
    public void initDFAFilter() {
        dfaFilter = new DFAFilter();
        // 从数据库加载敏感词
        dfaFilter.addWord("敏感词");
        dfaFilter.addWord("违规");
    }

    public void addCircle(ShareCircleBO circleBO) {
        // 发布前自动过滤
        circleBO.setContent(dfaFilter.filter(circleBO.getContent()));
        circleBO.setTitle(dfaFilter.filter(circleBO.getTitle()));
        // ... 保存到数据库
    }
}
```

### 3.4 WebSocket 实时通信

#### 3.4.1 为什么用 WebSocket？

HTTP 是请求-响应模式，服务器不能主动推送消息给客户端。
WebSocket 建立持久连接，支持双向通信。

```
HTTP:     客户端 → 请求 → 服务器 → 响应 → 客户端 (单向)
WebSocket: 客户端 ←→ 双向通信 ←→ 服务器 (持久连接)
```

#### 3.4.2 项目中的 WebSocket 架构

```
用户A 浏览器
    ↓ WebSocket 连接 (ws://localhost:3014/ws?userId=1)
ChickenSocketHandler
    ↓ SESSION_MAP.put(1, session)
    ↓ 收到消息
    ↓ 遍历 SESSION_MAP 转发给其他用户
用户B 浏览器 ← 收到消息
```

#### 3.4.3 核心代码

**WebSocket 配置：**
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    private ChickenSocketHandler chickenSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chickenSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
```

**消息处理器：**
```java
@Component
public class ChickenSocketHandler extends TextWebSocketHandler {
    // 存储在线用户会话
    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        SESSION_MAP.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        // 广播给其他用户
        for (Map.Entry<Long, WebSocketSession> entry : SESSION_MAP.entrySet()) {
            if (!entry.getKey().equals(userId)) {
                entry.getValue().sendMessage(new TextMessage(message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        SESSION_MAP.remove(userId);
    }
}
```

#### 3.4.4 前端连接示例

```javascript
const ws = new WebSocket('ws://localhost:3014/ws');

ws.onopen = () => {
    console.log('WebSocket 连接成功');
};

ws.onmessage = (event) => {
    console.log('收到消息:', event.data);
};

ws.send(JSON.stringify({
    fromUserId: 1,
    toUserId: 2,
    content: '你好！'
}));
```

### 3.5 Circle 服务 API 清单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /circle/share/add | 发布圈子 |
| GET | /circle/share/list | 圈子列表 |
| POST | /circle/moment/add | 发布动态 |
| GET | /circle/moment/list | 动态列表 |
| POST | /circle/moment/comment | 发表评论 |
| GET | /circle/moment/comment/list | 评论列表 |
| WS | /ws | WebSocket 实时消息 |

### 3.6 关键设计

#### 3.6.1 评论树形结构

```
动态 (momentId=1)
├── 评论1 (userId=1, replyUserId=null)
│   ├── 回复1 (userId=2, replyUserId=1)  ← 回复评论1
│   └── 回复2 (userId=3, replyUserId=1)  ← 回复评论1
└── 评论2 (userId=4, replyUserId=null)
    └── 回复3 (userId=5, replyUserId=4)  ← 回复评论2
```

通过 `replyUserId` 字段实现评论回复关系：
- `replyUserId = null` → 一级评论
- `replyUserId != null` → 回复某条评论

#### 3.6.2 敏感词过滤时机

```
用户输入内容
    ↓
DFAFilter.filter() ← 在 Domain Service 层过滤
    ↓
过滤后的内容保存到数据库
```

**为什么在 Domain Service 层过滤而不是 Controller？**
- 过滤是业务规则，不是接口校验
- 多个入口 (Controller/定时任务/消息队列) 都需要过滤
- 业务层统一处理，避免遗漏

---

## 踩坑记录

### Circle 服务踩坑

### 9. String.repeat() 是 Java 11+ 方法
```
cannot find symbol: method repeat(int)
```
**解决**: Java 8 使用循环替代:
```java
// Java 11+: "*".repeat(3)
// Java 8:
for (int i = 0; i < 3; i++) result.append("*");
```

---

## 学习总结

### Circle 服务学到的核心知识点
1. ✅ DFA 敏感词过滤算法 (Trie 树 + 状态机)
2. ✅ WebSocket 实时双向通信
3. ✅ ConcurrentHashMap 管理在线用户会话
4. ✅ 评论回复的树形结构设计
5. ✅ 业务层统一敏感词过滤
6. ✅ Spring WebSocket 配置与使用
