package com.colacode.subject.application.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.apache.rocketmq.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SubjectEsSyncProducer {

    private static final String TOPIC = "subject-es-sync-topic";

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendSyncMessage(SubjectEsSyncMessage message) {
        try {
            Message<SubjectEsSyncMessage> msg = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.asyncSend(TOPIC, msg, new org.apache.rocketmq.spring.core.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.spring.core.SendResult sendResult) {
                    log.info("MQ消息发送成功, subjectId: {}, operation: {}", 
                        message.getSubjectId(), message.getOperation());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("MQ消息发送失败, subjectId: {}", message.getSubjectId(), e);
                }
            });
        } catch (Exception e) {
            log.error("发送MQ消息异常, subjectId: {}", message.getSubjectId(), e);
        }
    }

    public void sendAddUpdateMessage(Long subjectId) {
        SubjectEsSyncMessage message = new SubjectEsSyncMessage();
        message.setSubjectId(subjectId);
        message.setOperation(1);
        message.setRetryCount(0);
        sendSyncMessage(message);
    }

    public void sendDeleteMessage(Long subjectId) {
        SubjectEsSyncMessage message = new SubjectEsSyncMessage();
        message.setSubjectId(subjectId);
        message.setOperation(2);
        message.setRetryCount(0);
        sendSyncMessage(message);
    }
}