package com.colacode.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.colacode.subject.infra.entity.SubjectLiked;
import com.colacode.subject.infra.mapper.SubjectLikedMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(
        topic = "subject-liked-topic",
        consumerGroup = "subject-liked-consumer-group"
)
public class SubjectLikedConsumer implements RocketMQListener<String> {

    private final SubjectLikedMapper subjectLikedMapper;

    public SubjectLikedConsumer(SubjectLikedMapper subjectLikedMapper) {
        this.subjectLikedMapper = subjectLikedMapper;
    }

    @Override
    public void onMessage(String message) {
        try {
            log.info("收到点赞消息: {}", message);
            SubjectLikedMessage likedMessage = JSON.parseObject(message, SubjectLikedMessage.class);

            SubjectLiked liked = new SubjectLiked();
            liked.setSubjectId(likedMessage.getSubjectId());
            liked.setLikedUserId(likedMessage.getLikedUserId());
            liked.setLikedStatus(likedMessage.getLikedStatus());
            subjectLikedMapper.insert(liked);

            log.info("点赞消息处理成功, subjectId: {}", likedMessage.getSubjectId());
        } catch (Exception e) {
            log.error("点赞消息处理失败", e);
            throw new RuntimeException("点赞消息处理失败", e);
        }
    }
}
