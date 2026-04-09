package com.colacode.subject.job;

import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.es.SubjectEsDTO;
import com.colacode.subject.infra.es.SubjectEsService;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SubjectSyncJob {

    private final SubjectEsService subjectEsService;
    private final EsSyncStatusMapper esSyncStatusMapper;

    public SubjectSyncJob(SubjectEsService subjectEsService, EsSyncStatusMapper esSyncStatusMapper) {
        this.subjectEsService = subjectEsService;
        this.esSyncStatusMapper = esSyncStatusMapper;
    }

    @XxlJob("syncSubjectToEs")
    public ReturnT<String> syncSubjectToEs(String param) {
        log.info("开始执行同步题目到ES定时任务");
        try {
            subjectEsService.syncAllSubjects();
            log.info("同步题目到ES定时任务执行成功");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("同步题目到ES定时任务执行失败", e);
            return ReturnT.FAIL;
        }
    }

    @XxlJob("esSyncRetryJob")
    public ReturnT<String> esSyncRetryJob(String param) {
        log.info("开始执行ES同步补偿任务");
        int limit = 100;
        
        List<EsSyncStatus> retryTasks = esSyncStatusMapper.selectRetryableTasks(new Date(), limit);
        log.info("扫描到 {} 条待补偿任务", retryTasks.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (EsSyncStatus task : retryTasks) {
            try {
                task.setStatus(EsSyncStatus.STATUS_PROCESSING);
                esSyncStatusMapper.updateById(task);
                
                if (task.getOperation() == EsSyncStatus.OPERATION_DELETE) {
                    subjectEsService.delete(task.getBizId());
                } else {
                    log.warn("当前仅支持删除操作的直接补偿，新增/修改需走MQ链路");
                }
                
                task.setStatus(EsSyncStatus.STATUS_SUCCESS);
                task.setLastSyncTime(new Date());
                task.setErrorMsg(null);
                esSyncStatusMapper.updateById(task);
                successCount++;
                log.info("补偿成功, bizId: {}", task.getBizId());
                
            } catch (Exception e) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setErrorMsg(e.getMessage());
                task.setNextRetryTime(calculateNextRetryTime(task.getRetryCount()));
                
                if (task.getRetryCount() >= task.getMaxRetryCount()) {
                    task.setStatus(EsSyncStatus.STATUS_DEAD);
                    log.error("任务超过最大重试次数，进入死信, bizId: {}", task.getBizId());
                } else {
                    task.setStatus(EsSyncStatus.STATUS_FAILED);
                    log.warn("补偿失败, bizId: {}, retryCount: {}", task.getBizId(), task.getRetryCount());
                }
                esSyncStatusMapper.updateById(task);
                failCount++;
            }
        }
        
        log.info("ES同步补偿任务执行完成, 成功: {}, 失败: {}", successCount, failCount);
        return ReturnT.SUCCESS;
    }

    private Date calculateNextRetryTime(int retryCount) {
        Calendar calendar = Calendar.getInstance();
        int[] delays = {1, 5, 30};
        int minutes = retryCount <= delays.length ? delays[retryCount - 1] : 30;
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}
