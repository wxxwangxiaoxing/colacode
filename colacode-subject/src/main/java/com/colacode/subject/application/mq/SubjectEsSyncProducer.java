package com.colacode.subject.application.mq;

import com.colacode.common.constants.WebConstants;
import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Component
public class SubjectEsSyncProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final EsSyncStatusMapper esSyncStatusMapper;

    public SubjectEsSyncProducer(@org.springframework.beans.factory.annotation.Autowired(required = false) RocketMQTemplate rocketMQTemplate, EsSyncStatusMapper esSyncStatusMapper) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.esSyncStatusMapper = esSyncStatusMapper;
    }

    public void sendSyncMessage(SubjectEsSyncMessage message) {
        if (message.getTraceId() == null || message.getTraceId().isBlank()) {
            message.setTraceId(org.slf4j.MDC.get(WebConstants.TRACE_ID_MDC_KEY));
        }
        if ((message.getPayloadJson() == null || message.getPayloadJson().isBlank()) && message.getTaskId() != null) {
            EsSyncStatus task = esSyncStatusMapper.selectById(message.getTaskId());
            if (task != null) {
                message.setPayloadJson(task.getPayloadJson());
                if (message.getTraceId() == null || message.getTraceId().isBlank()) {
                    message.setTraceId(task.getTraceId());
                }
            }
        }

        if (rocketMQTemplate == null) {
            log.warn("RocketMQ not configured, skipping MQ message send for ES sync");
            markTaskSendFailed(message.getTaskId(), new RuntimeException("RocketMQ not configured"));
            return;
        }

        try {
            Message<SubjectEsSyncMessage> mqMessage = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.asyncSend(SubjectMqConstants.SUBJECT_ES_SYNC_TOPIC, mqMessage,
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("ES同步MQ消息发送成功, taskId: {}, subjectId: {}, operation: {}, traceId: {}",
                                    message.getTaskId(), message.getSubjectId(), message.getOperation(), message.getTraceId());
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("ES同步MQ消息发送失败, taskId: {}, subjectId: {}",
                                    message.getTaskId(), message.getSubjectId(), e);
                            markTaskSendFailed(message.getTaskId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("发送ES同步MQ消息异常, taskId: {}, subjectId: {}",
                    message.getTaskId(), message.getSubjectId(), e);
            markTaskSendFailed(message.getTaskId(), e);
            throw new RuntimeException("发送ES同步MQ消息异常", e);
        }
    }

    public void resendByTask(EsSyncStatus task) {
        SubjectEsSyncMessage message = new SubjectEsSyncMessage();
        message.setTaskId(task.getId());
        message.setSubjectId(task.getBizId());
        message.setOperation(task.getOperation());
        message.setRetryCount(task.getRetryCount());
        message.setTraceId(task.getTraceId());
        message.setPayloadJson(task.getPayloadJson());
        sendSyncMessage(message);
    }

    private void markTaskSendFailed(Long taskId, Throwable throwable) {
        if (taskId == null) {
            return;
        }
        EsSyncStatus task = esSyncStatusMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(EsSyncStatus.STATUS_FAILED);
        task.setErrorMsg(throwable == null ? null : throwable.getMessage());
        task.setNextRetryTime(calculateNextRetryTime(task.getRetryCount()));
        esSyncStatusMapper.updateById(task);
    }

    private Date calculateNextRetryTime(Integer retryCount) {
        int currentRetryCount = retryCount == null ? 0 : retryCount;
        Calendar calendar = Calendar.getInstance();
        int[] delays = {1, 5, 30};
        int index = Math.min(currentRetryCount, delays.length - 1);
        calendar.add(Calendar.MINUTE, delays[index]);
        return calendar.getTime();
    }
}
