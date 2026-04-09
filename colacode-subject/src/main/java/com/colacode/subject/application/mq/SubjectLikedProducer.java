package com.colacode.subject.application.mq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubjectLikedProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public SubjectLikedProducer(@Nullable RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendLikedMessage(SubjectLikedMessage message) {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate unavailable, skip liked message: {}", message);
            return;
        }
        String destination = "subject-liked-topic";
        rocketMQTemplate.syncSend(destination, JSON.toJSONString(message));
    }
}
