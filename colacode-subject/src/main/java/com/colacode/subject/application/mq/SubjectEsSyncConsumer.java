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

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "subject-es-sync-topic", consumerGroup = "subject-es-sync-group")
public class SubjectEsSyncConsumer implements RocketMQListener<SubjectEsSyncMessage> {

    @Resource
    private SubjectDomainService subjectDomainService;

    @Resource
    private SubjectEsService subjectEsService;

    @Resource
    private EsSyncStatusMapper esSyncStatusMapper;

    private static final int MAX_RETRY = 3;

    @Override
    @Transactional
    public void onMessage(SubjectEsSyncMessage message) {
        log.info("收到ES同步消息, subjectId: {}, operation: {}", message.getSubjectId(), message.getOperation());
        
        try {
            if (message.getOperation() == EsSyncStatus.OPERATION_DELETE) {
                subjectEsService.delete(message.getSubjectId());
                log.info("ES删除成功, subjectId: {}", message.getSubjectId());
            } else {
                SubjectInfoBO bo = subjectDomainService.querySubject(message.getSubjectId());
                if (bo != null) {
                    SubjectEsDTO dto = buildEsDoc(bo);
                    subjectEsService.save(dto);
                    log.info("ES新增/更新成功, subjectId: {}", message.getSubjectId());
                } else {
                    log.warn("题目不存在, subjectId: {}", message.getSubjectId());
                }
            }
        } catch (Exception e) {
            log.error("ES同步消费失败, subjectId: {}", message.getSubjectId(), e);
            
            if (message.getTaskId() != null) {
                handleFailure(message.getTaskId(), e.getMessage(), message.getRetryCount());
            }
            
            throw e;
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

    private void handleFailure(Long taskId, String errorMsg, Integer retryCount) {
        if (taskId == null) return;
        
        EsSyncStatus task = esSyncStatusMapper.selectById(taskId);
        if (task != null) {
            task.setRetryCount(retryCount + 1);
            task.setErrorMsg(errorMsg);
            task.setNextRetryTime(calculateNextRetryTime(task.getRetryCount()));
            
            if (task.getRetryCount() >= MAX_RETRY) {
                task.setStatus(EsSyncStatus.STATUS_DEAD);
                log.error("任务超过最大重试次数，进入死信, taskId: {}", taskId);
            } else {
                task.setStatus(EsSyncStatus.STATUS_FAILED);
            }
            esSyncStatusMapper.updateById(task);
        }
    }

    private Date calculateNextRetryTime(int retryCount) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int[] delays = {1, 5, 30};
        int minutes = retryCount <= delays.length ? delays[retryCount - 1] : 30;
        calendar.add(java.util.Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}