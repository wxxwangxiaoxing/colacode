package com.colacode.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.colacode.common.constants.WebConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubjectLikedProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public SubjectLikedProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendLikedMessage(SubjectLikedMessage message) {
        if (message.getTraceId() == null || message.getTraceId().isBlank()) {
            message.setTraceId(org.slf4j.MDC.get(WebConstants.TRACE_ID_MDC_KEY));
        }
        rocketMQTemplate.syncSend(SubjectMqConstants.SUBJECT_LIKED_TOPIC, JSON.toJSONString(message));
        log.info("点赞消息发送成功, subjectId: {}, likedUserId: {}, traceId: {}",
                message.getSubjectId(), message.getLikedUserId(), message.getTraceId());
    }
}
