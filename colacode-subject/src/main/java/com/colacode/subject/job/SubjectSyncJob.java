package com.colacode.subject.job;

import com.colacode.subject.application.mq.SubjectEsSyncProducer;
import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.es.SubjectEsService;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SubjectSyncJob {

    private final SubjectEsService subjectEsService;
    private final EsSyncStatusMapper esSyncStatusMapper;
    private final SubjectEsSyncProducer subjectEsSyncProducer;

    public SubjectSyncJob(SubjectEsService subjectEsService,
                          EsSyncStatusMapper esSyncStatusMapper,
                          SubjectEsSyncProducer subjectEsSyncProducer) {
        this.subjectEsService = subjectEsService;
        this.esSyncStatusMapper = esSyncStatusMapper;
        this.subjectEsSyncProducer = subjectEsSyncProducer;
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
                int nextRetryCount = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
                task.setRetryCount(nextRetryCount);
                task.setStatus(EsSyncStatus.STATUS_PROCESSING);
                task.setErrorMsg(null);
                task.setNextRetryTime(null);
                esSyncStatusMapper.updateById(task);

                subjectEsSyncProducer.resendByTask(task);
                successCount++;
                log.info("补偿消息已重新投递, taskId: {}, bizId: {}, operation: {}, retryCount: {}",
                        task.getId(), task.getBizId(), task.getOperation(), task.getRetryCount());
            } catch (Exception e) {
                task.setErrorMsg(e.getMessage());
                task.setNextRetryTime(calculateNextRetryTime(task.getRetryCount()));

                if (task.getMaxRetryCount() != null && task.getRetryCount() != null
                        && task.getRetryCount() >= task.getMaxRetryCount()) {
                    task.setStatus(EsSyncStatus.STATUS_DEAD);
                    log.error("补偿投递超过最大重试次数，进入死信, taskId: {}, bizId: {}",
                            task.getId(), task.getBizId());
                } else {
                    task.setStatus(EsSyncStatus.STATUS_FAILED);
                    log.warn("补偿投递失败, taskId: {}, bizId: {}, retryCount: {}",
                            task.getId(), task.getBizId(), task.getRetryCount());
                }
                esSyncStatusMapper.updateById(task);
                failCount++;
            }
        }

        log.info("ES同步补偿任务执行完成, 重投成功: {}, 重投失败: {}", successCount, failCount);
        return ReturnT.SUCCESS;
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
