package com.colacode.subject.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.subject.domain.bo.SubjectCategoryBO;
import com.colacode.subject.domain.bo.SubjectInfoBO;
import com.colacode.subject.domain.converter.SubjectCategoryBOConverter;
import com.colacode.subject.domain.converter.SubjectInfoBOConverter;
import com.colacode.subject.domain.strategy.SubjectTypeHandler;
import com.colacode.subject.domain.strategy.SubjectTypeHandlerFactory;
import com.colacode.subject.infra.entity.EsSyncStatus;
import com.colacode.subject.infra.mapper.SubjectCategoryMapper;
import com.colacode.subject.infra.mapper.SubjectInfoMapper;
import com.colacode.subject.infra.mapper.SubjectMappingMapper;
import com.colacode.subject.infra.mapper.EsSyncStatusMapper;
import com.colacode.subject.domain.bo.ContributeStat;
import com.colacode.subject.application.mq.SubjectEsSyncProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SubjectDomainService {

    private final SubjectInfoMapper subjectInfoMapper;
    private final SubjectMappingMapper subjectMappingMapper;
    private final SubjectCategoryMapper subjectCategoryMapper;
    private final SubjectTypeHandlerFactory subjectTypeHandlerFactory;
    private final SubjectEsService subjectEsService;
    private final EsSyncStatusMapper esSyncStatusMapper;
    private final SubjectEsSyncProducer subjectEsSyncProducer;

    public SubjectDomainService(SubjectInfoMapper subjectInfoMapper,
                                SubjectMappingMapper subjectMappingMapper,
                                SubjectCategoryMapper subjectCategoryMapper,
                                SubjectTypeHandlerFactory subjectTypeHandlerFactory,
                                SubjectEsService subjectEsService,
                                EsSyncStatusMapper esSyncStatusMapper,
                                SubjectEsSyncProducer subjectEsSyncProducer) {
        this.subjectInfoMapper = subjectInfoMapper;
        this.subjectMappingMapper = subjectMappingMapper;
        this.subjectCategoryMapper = subjectCategoryMapper;
        this.subjectTypeHandlerFactory = subjectTypeHandlerFactory;
        this.subjectEsService = subjectEsService;
        this.esSyncStatusMapper = esSyncStatusMapper;
        this.subjectEsSyncProducer = subjectEsSyncProducer;
    }

    public void addSubject(SubjectInfoBO subjectInfoBO) {
        SubjectInfo entity = SubjectInfoBOConverter.INSTANCE.convertToEntity(subjectInfoBO);
        subjectInfoMapper.insert(entity);
        subjectInfoBO.setId(entity.getId());

        saveCategoryMapping(subjectInfoBO);

        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfoBO.getSubjectType());
        if (handler != null) {
            handler.add(subjectInfoBO);
        }

        syncToEs(subjectInfoBO);
    }

    public void updateSubject(SubjectInfoBO subjectInfoBO) {
        SubjectInfo entity = SubjectInfoBOConverter.INSTANCE.convertToEntity(subjectInfoBO);
        subjectInfoMapper.updateById(entity);

        updateCategoryMapping(subjectInfoBO);

        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfoBO.getSubjectType());
        if (handler != null) {
            handler.update(subjectInfoBO);
        }

        syncToEs(subjectInfoBO);
    }

    public void deleteSubject(Long subjectId) {
        subjectInfoMapper.deleteById(subjectId);
        
        createSyncTask(subjectId, EsSyncStatus.OPERATION_DELETE, null, EsSyncStatus.STATUS_PENDING);
        subjectEsSyncProducer.sendDeleteMessage(subjectId);
        
        log.info("删除题目成功, subjectId: {}", subjectId);
    }

    public SubjectInfoBO querySubject(Long subjectId) {
        SubjectInfo subjectInfo = subjectInfoMapper.selectById(subjectId);
        if (subjectInfo == null) return null;

        SubjectInfoBO bo = SubjectInfoBOConverter.INSTANCE.convertToBO(subjectInfo);
        bo.setCategoryIds(getCategoryIdsBySubjectId(subjectId));

        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        if (handler != null) {
            SubjectInfoBO detailBO = handler.query(subjectId);
            bo.setRadioList(detailBO.getRadioList());
            bo.setMultipleList(detailBO.getMultipleList());
            bo.setJudgeBO(detailBO.getJudgeBO());
            bo.setBriefBO(detailBO.getBriefBO());
        }
        return bo;
    }

    private void saveCategoryMapping(SubjectInfoBO subjectInfoBO) {
        List<Long> categoryIds = subjectInfoBO.getCategoryIds();
        if (categoryIds == null) return;
        for (Long categoryId : categoryIds) {
            SubjectMapping mapping = new SubjectMapping();
            mapping.setSubjectId(subjectInfoBO.getId());
            mapping.setCategoryId(categoryId);
            subjectMappingMapper.insert(mapping);
        }
    }

    private void updateCategoryMapping(SubjectInfoBO subjectInfoBO) {
        LambdaQueryWrapper<SubjectMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectMapping::getSubjectId, subjectInfoBO.getId());
        subjectMappingMapper.delete(wrapper);
        saveCategoryMapping(subjectInfoBO);
    }

    private List<Long> getCategoryIdsBySubjectId(Long subjectId) {
        LambdaQueryWrapper<SubjectMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectMapping::getSubjectId, subjectId);
        List<SubjectMapping> mappings = subjectMappingMapper.selectList(wrapper);
        List<Long> categoryIds = new ArrayList<>();
        for (SubjectMapping mapping : mappings) {
            categoryIds.add(mapping.getCategoryId());
        }
        return categoryIds;
    }

    private void syncToEs(SubjectInfoBO subjectInfoBO) {
        Long subjectId = subjectInfoBO.getId();
        
        createSyncTask(subjectId, EsSyncStatus.OPERATION_ADD_UPDATE, null, EsSyncStatus.STATUS_PENDING);
        subjectEsSyncProducer.sendAddUpdateMessage(subjectId);
        
        log.info("ES同步任务已创建, subjectId: {}", subjectId);
    }

    private SubjectEsDTO buildEsDto(SubjectInfoBO subjectInfoBO) {
        SubjectEsDTO esDTO = new SubjectEsDTO();
        esDTO.setId(subjectInfoBO.getId());
        esDTO.setSubjectName(subjectInfoBO.getSubjectName());
        esDTO.setSubjectParse(subjectInfoBO.getSubjectParse());
        esDTO.setSubjectComment(subjectInfoBO.getSubjectComment());
        esDTO.setSubjectDiff(subjectInfoBO.getSubjectDiff());
        esDTO.setSubjectType(subjectInfoBO.getSubjectType());

        List<Long> categoryIds = subjectInfoBO.getCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            SubjectCategory category = subjectCategoryMapper.selectById(categoryIds.get(0));
            if (category != null) {
                esDTO.setCategoryName(category.getCategoryName());
            }
        }
        return esDTO;
    }

    private void createSyncTask(Long bizId, Integer operation, String errorMsg, Integer status) {
        EsSyncStatus syncTask = new EsSyncStatus();
        syncTask.setBizId(bizId);
        syncTask.setBizType("subject");
        syncTask.setOperation(operation);
        syncTask.setStatus(status);
        syncTask.setErrorMsg(errorMsg);
        syncTask.setRetryCount(0);
        syncTask.setMaxRetryCount(3);
        
        if (status == EsSyncStatus.STATUS_SUCCESS) {
            syncTask.setLastSyncTime(new Date());
        } else {
            syncTask.setNextRetryTime(new Date());
        }
        
        esSyncStatusMapper.insert(syncTask);
    }

    public Page<SubjectInfoBO> getSubjectPage(Long categoryId, Long labelId, Integer subjectType, int pageNo, int pageSize) {
        Page<SubjectInfo> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SubjectInfo> wrapper = new LambdaQueryWrapper<>();
        if (subjectType != null) {
            wrapper.eq(SubjectInfo::getSubjectType, subjectType);
        }
        if (categoryId != null) {
            List<Long> subjectIds = getSubjectIdsByCategoryId(categoryId);
            if (!subjectIds.isEmpty()) {
                wrapper.in(SubjectInfo::getId, subjectIds);
            } else {
                wrapper.eq(SubjectInfo::getId, -1);
            }
        }
        wrapper.orderByDesc(SubjectInfo::getCreatedTime);
        Page<SubjectInfo> result = subjectInfoMapper.selectPage(page, wrapper);
        Page<SubjectInfoBO> boPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SubjectInfoBO> boList = new ArrayList<>();
        for (SubjectInfo info : result.getRecords()) {
            SubjectInfoBO bo = SubjectInfoBOConverter.INSTANCE.convertToBO(info);
            bo.setCategoryIds(getCategoryIdsBySubjectId(info.getId()));
            boList.add(bo);
        }
        boPage.setRecords(boList);
        return boPage;
    }

    public List<SubjectInfoBO> getContributeList(int limit) {
        List<ContributeStat> stats = subjectInfoMapper.selectContributeStats(limit);
        List<SubjectInfoBO> result = new ArrayList<>();
        for (ContributeStat stat : stats) {
            SubjectInfoBO bo = new SubjectInfoBO();
            bo.setCreatedBy(stat.getCreatedBy());
            bo.setContributeCount(stat.getContributeCount());
            result.add(bo);
        }
        return result;
    }

    private List<Long> getSubjectIdsByCategoryId(Long categoryId) {
        LambdaQueryWrapper<SubjectMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectMapping::getCategoryId, categoryId);
        List<SubjectMapping> mappings = subjectMappingMapper.selectList(wrapper);
        List<Long> ids = new ArrayList<>();
        for (SubjectMapping m : mappings) {
            ids.add(m.getSubjectId());
        }
        return ids;
    }

    public String getCategoryName(Long categoryId) {
        if (categoryId == null) return null;
        SubjectCategory category = subjectCategoryMapper.selectById(categoryId);
        return category != null ? category.getCategoryName() : null;
    }
}
