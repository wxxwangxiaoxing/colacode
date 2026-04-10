package com.colacode.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.colacode.subject.domain.service.SubjectLikedDomainService;
import com.colacode.subject.infra.entity.SubjectLiked;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rocketmq.name-server")
@RocketMQMessageListener(
        topic = SubjectMqConstants.SUBJECT_LIKED_TOPIC,
        consumerGroup = SubjectMqConstants.SUBJECT_LIKED_CONSUMER_GROUP
)
public class SubjectLikedConsumer implements RocketMQListener<String> {

    private final SubjectLikedDomainService subjectLikedDomainService;

    public SubjectLikedConsumer(SubjectLikedDomainService subjectLikedDomainService) {
        this.subjectLikedDomainService = subjectLikedDomainService;
    }

    @Override
    public void onMessage(String message) {
        try {
            SubjectLikedMessage likedMessage = JSON.parseObject(message, SubjectLikedMessage.class);
            log.info("收到点赞消息, subjectId: {}, likedUserId: {}, likedStatus: {}, traceId: {}",
                    likedMessage.getSubjectId(),
                    likedMessage.getLikedUserId(),
                    likedMessage.getLikedStatus(),
                    likedMessage.getTraceId());

            SubjectLiked liked = new SubjectLiked();
            liked.setSubjectId(likedMessage.getSubjectId());
            liked.setLikedUserId(likedMessage.getLikedUserId());
            liked.setLikedStatus(likedMessage.getLikedStatus());
            subjectLikedDomainService.addOrUpdate(liked);

            log.info("点赞消息处理成功, subjectId: {}, likedUserId: {}",
                    likedMessage.getSubjectId(), likedMessage.getLikedUserId());
        } catch (Exception e) {
            log.error("点赞消息处理失败, payload: {}", message, e);
            throw new RuntimeException("点赞消息处理失败", e);
        }
    }
}
