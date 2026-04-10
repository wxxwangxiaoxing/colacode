package com.colacode.subject.application.mq;

import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.service.SubjectDomainService;
import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.es.SubjectEsDTO;
import com.colacode.subject.infra.es.SubjectEsService;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = SubjectMqConstants.SUBJECT_ES_SYNC_TOPIC,
        consumerGroup = SubjectMqConstants.SUBJECT_ES_SYNC_CONSUMER_GROUP
)
public class SubjectEsSyncConsumer implements RocketMQListener<SubjectEsSyncMessage> {

    private final SubjectDomainService subjectDomainService;
    private final SubjectEsService subjectEsService;
    private final EsSyncStatusMapper esSyncStatusMapper;

    public SubjectEsSyncConsumer(SubjectDomainService subjectDomainService,
                                 SubjectEsService subjectEsService,
                                 EsSyncStatusMapper esSyncStatusMapper) {
        this.subjectDomainService = subjectDomainService;
        this.subjectEsService = subjectEsService;
        this.esSyncStatusMapper = esSyncStatusMapper;
    }

    @Override
    @Transactional
    public void onMessage(SubjectEsSyncMessage message) {
        log.info("收到ES同步消息, taskId: {}, subjectId: {}, operation: {}, traceId: {}",
                message.getTaskId(), message.getSubjectId(), message.getOperation(), message.getTraceId());

        EsSyncStatus task = getTask(message.getTaskId());
        if (task != null) {
            task.setStatus(EsSyncStatus.STATUS_PROCESSING);
            task.setErrorMsg(null);
            esSyncStatusMapper.updateById(task);
        }

        try {
            if (Objects.equals(message.getOperation(), EsSyncStatus.OPERATION_DELETE)) {
                subjectEsService.delete(message.getSubjectId());
                log.info("ES删除成功, subjectId: {}", message.getSubjectId());
            } else {
                SubjectInfoBO bo = subjectDomainService.querySubjectWithoutBrowseIncrement(message.getSubjectId());
                if (bo != null) {
                    SubjectEsDTO dto = buildEsDoc(bo);
                    subjectEsService.save(dto);
                    log.info("ES新增/更新成功, subjectId: {}", message.getSubjectId());
                } else {
                    log.warn("题目不存在，跳过新增/更新同步, subjectId: {}", message.getSubjectId());
                }
            }

            markSuccess(task);
        } catch (Exception e) {
            log.error("ES同步消费失败, taskId: {}, subjectId: {}", message.getTaskId(), message.getSubjectId(), e);
            markConsumeFailure(task, e);
            throw new RuntimeException("ES同步消费失败", e);
        }
    }

    private SubjectEsDTO buildEsDoc(SubjectInfoBO bo) {
        SubjectEsDTO dto = new SubjectEsDTO();
        dto.setId(bo.getId());
        dto.setSubjectName(bo.getSubjectName());
        dto.setSubjectParse(bo.getSubjectParse());
        dto.setSubjectComment(bo.getSubjectComment());
        dto.setSubjectDiff(bo.getSubjectDiff());
        dto.setSubjectType(bo.getSubjectType());

        if (bo.getCategoryIds() != null && !bo.getCategoryIds().isEmpty()) {
            dto.setCategoryName(subjectDomainService.getCategoryName(bo.getCategoryIds().get(0)));
        }
        return dto;
    }

    private EsSyncStatus getTask(Long taskId) {
        if (taskId == null) {
            return null;
        }
        return esSyncStatusMapper.selectById(taskId);
    }

    private void markSuccess(EsSyncStatus task) {
        if (task == null) {
            return;
        }
        task.setStatus(EsSyncStatus.STATUS_SUCCESS);
        task.setLastSyncTime(new Date());
        task.setErrorMsg(null);
        task.setNextRetryTime(null);
        esSyncStatusMapper.updateById(task);
    }

    private void markConsumeFailure(EsSyncStatus task, Exception e) {
        if (task == null) {
            return;
        }
        task.setErrorMsg(e.getMessage());
        task.setNextRetryTime(calculateNextRetryTime(task.getRetryCount()));
        if (task.getMaxRetryCount() != null && task.getRetryCount() != null
                && task.getRetryCount() >= task.getMaxRetryCount()) {
            task.setStatus(EsSyncStatus.STATUS_DEAD);
        } else {
            task.setStatus(EsSyncStatus.STATUS_FAILED);
        }
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
