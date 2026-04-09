package com.colacode.subject.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.entity.SubjectInfo;
import com.colacode.subject.infra.es.SubjectEsDTO;
import com.colacode.subject.infra.es.SubjectEsService;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import com.colacode.subject.infra.mapper.SubjectInfoMapper;
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
public class EsInspectJob {

    @Resource
    private SubjectInfoMapper subjectInfoMapper;

    @Resource
    private SubjectEsService subjectEsService;

    @Resource
    private EsSyncStatusMapper esSyncStatusMapper;

    @XxlJob("esInspectJob")
    public ReturnT<String> execute(String param) {
        log.info("开始执行ES巡检任务");
        
        int fixedHours = 24;
        Date startTime = getStartTime(fixedHours);
        
        LambdaQueryWrapper<SubjectInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SubjectInfo::getUpdatedTime, startTime);
        List<SubjectInfo> recentSubjects = subjectInfoMapper.selectList(wrapper);
        
        log.info("最近{}小时有{}条题目更新", fixedHours, recentSubjects.size());
        
        int inconsistencyCount = 0;
        
        for (SubjectInfo subject : recentSubjects) {
            try {
                SubjectEsDTO esDoc = subjectEsService.getById(subject.getId());
                
                if (esDoc == null) {
                    log.warn("巡检发现: MySQL有数据但ES没有, subjectId: {}", subject.getId());
                    createRepairTask(subject.getId(), "ES缺少文档");
                    inconsistencyCount++;
                } else if (!isDataConsistent(subject, esDoc)) {
                    log.warn("巡检发现: 数据不一致, subjectId: {}", subject.getId());
                    createRepairTask(subject.getId(), "ES数据与MySQL不一致");
                    inconsistencyCount++;
                }
            } catch (Exception e) {
                log.error("巡检异常, subjectId: {}", subject.getId(), e);
            }
        }
        
        log.info("ES巡检任务执行完成, 发现不一致: {} 条", inconsistencyCount);
        return ReturnT.SUCCESS;
    }

    private boolean isDataConsistent(SubjectInfo mysql, SubjectEsDTO es) {
        if (es == null) return false;
        
        boolean nameConsistent = (mysql.getSubjectName() == null && es.getSubjectName() == null)
                || (mysql.getSubjectName() != null && mysql.getSubjectName().equals(es.getSubjectName()));
        
        return nameConsistent;
    }

    private Date getStartTime(int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -hours);
        return calendar.getTime();
    }

    private void createRepairTask(Long subjectId, String errorMsg) {
        EsSyncStatus task = new EsSyncStatus();
        task.setBizId(subjectId);
        task.setBizType("subject");
        task.setOperation(EsSyncStatus.OPERATION_ADD_UPDATE);
        task.setStatus(EsSyncStatus.STATUS_PENDING);
        task.setErrorMsg("巡检发现: " + errorMsg);
        task.setRetryCount(0);
        task.setMaxRetryCount(3);
        task.setNextRetryTime(new Date());
        esSyncStatusMapper.insert(task);
    }
}