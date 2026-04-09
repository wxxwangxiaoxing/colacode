package com.colacode.circle.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.colacode.circle.common.DFAFilter;
import com.colacode.circle.domain.bo.SensitiveWordBO;
import com.colacode.circle.domain.bo.ShareCircleBO;
import com.colacode.circle.domain.bo.ShareCommentReplyBO;
import com.colacode.circle.domain.bo.ShareMessageBO;
import com.colacode.circle.domain.bo.ShareMomentBO;
import com.colacode.circle.domain.converter.ShareCircleBOConverter;
import com.colacode.circle.domain.converter.ShareMomentBOConverter;
import com.colacode.circle.infra.entity.SensitiveWords;
import com.colacode.circle.infra.entity.ShareCircle;
import com.colacode.circle.infra.entity.ShareCommentReply;
import com.colacode.circle.infra.entity.ShareMessage;
import com.colacode.circle.infra.entity.ShareMoment;
import com.colacode.circle.infra.mapper.SensitiveWordsMapper;
import com.colacode.circle.infra.mapper.ShareCircleMapper;
import com.colacode.circle.infra.mapper.ShareCommentReplyMapper;
import com.colacode.circle.infra.mapper.ShareMessageMapper;
import com.colacode.circle.infra.mapper.ShareMomentMapper;
import com.colacode.common.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CircleDomainService {

    private final ShareCircleMapper shareCircleMapper;
    private final ShareMomentMapper shareMomentMapper;
    private final ShareCommentReplyMapper shareCommentReplyMapper;
    private final ShareMessageMapper shareMessageMapper;
    private final SensitiveWordsMapper sensitiveWordsMapper;

    private final DFAFilter dfaFilter;

    public CircleDomainService(ShareCircleMapper shareCircleMapper,
                               ShareMomentMapper shareMomentMapper,
                               ShareCommentReplyMapper shareCommentReplyMapper,
                               ShareMessageMapper shareMessageMapper,
                               SensitiveWordsMapper sensitiveWordsMapper) {
        this.shareCircleMapper = shareCircleMapper;
        this.shareMomentMapper = shareMomentMapper;
        this.shareCommentReplyMapper = shareCommentReplyMapper;
        this.shareMessageMapper = shareMessageMapper;
        this.sensitiveWordsMapper = sensitiveWordsMapper;
        this.dfaFilter = new DFAFilter();
        loadSensitiveWords();
        log.info("DFA敏感词过滤器初始化完成");
    }

    private void loadSensitiveWords() {
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWords::getIsDeleted, 0);
        List<SensitiveWords> wordsList = sensitiveWordsMapper.selectList(wrapper);
        for (SensitiveWords sw : wordsList) {
            dfaFilter.addWord(sw.getWords());
        }
    }

    public void addSensitiveWord(SensitiveWordBO wordBO) {
        SensitiveWords entity = new SensitiveWords();
        entity.setWords(wordBO.getWords());
        entity.setType(wordBO.getType());
        sensitiveWordsMapper.insert(entity);
        dfaFilter.addWord(wordBO.getWords());
    }

    public void deleteSensitiveWord(Long id) {
        sensitiveWordsMapper.deleteById(id);
    }

    public List<SensitiveWordBO> listSensitiveWords() {
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWords::getIsDeleted, 0);
        return sensitiveWordsMapper.selectList(wrapper).stream().map(this::toSensitiveWordBO).toList();
    }

    public void addCircle(ShareCircleBO circleBO) {
        circleBO.setContent(dfaFilter.filter(circleBO.getContent()));
        circleBO.setTitle(dfaFilter.filter(circleBO.getTitle()));
        ShareCircle entity = ShareCircleBOConverter.INSTANCE.convertToEntity(circleBO);
        shareCircleMapper.insert(entity);
        log.info("发布圈子成功, id: {}", entity.getId());
    }

    public PageResult<ShareCircleBO> listCircles(int pageNo, int pageSize) {
        Page<ShareCircle> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ShareCircle> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ShareCircle::getCreatedTime);
        shareCircleMapper.selectPage(page, wrapper);
        return new PageResult<>(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                ShareCircleBOConverter.INSTANCE.convertToBOList(page.getRecords()));
    }

    public void addMoment(ShareMomentBO momentBO) {
        momentBO.setContent(dfaFilter.filter(momentBO.getContent()));
        ShareMoment entity = ShareMomentBOConverter.INSTANCE.convertToEntity(momentBO);
        if (entity.getCircleId() == null) {
            entity.setCircleId(1L);
        }
        shareMomentMapper.insert(entity);
        log.info("发布动态成功, id: {}", entity.getId());
    }

    public PageResult<ShareMomentBO> listMoments(int pageNo, int pageSize) {
        Page<ShareMoment> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ShareMoment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ShareMoment::getCreatedTime);
        shareMomentMapper.selectPage(page, wrapper);
        return new PageResult<>(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                ShareMomentBOConverter.INSTANCE.convertToBOList(page.getRecords()));
    }

    public void addComment(ShareCommentReplyBO commentBO) {
        commentBO.setContent(dfaFilter.filter(commentBO.getContent()));
        ShareCommentReply entity = new ShareCommentReply();
        entity.setMomentId(commentBO.getMomentId());
        entity.setCircleId(commentBO.getCircleId());
        entity.setContent(commentBO.getContent());
        entity.setReplyUserId(commentBO.getReplyUserId());
        entity.setType(commentBO.getType());
        entity.setCreatedBy(commentBO.getUserId() == null ? null : String.valueOf(commentBO.getUserId()));
        shareCommentReplyMapper.insert(entity);

        sendCommentNotification(commentBO);

        log.info("评论成功, id: {}", entity.getId());
    }

    private void sendCommentNotification(ShareCommentReplyBO commentBO) {
        ShareMessage message = new ShareMessage();
        message.setFromUserId(commentBO.getUserId());
        message.setContent(commentBO.getContent());
        message.setMomentId(commentBO.getMomentId());
        message.setStatus(0);

        if (commentBO.getType() == 1) {
            LambdaQueryWrapper<ShareMoment> momentWrapper = new LambdaQueryWrapper<>();
            momentWrapper.eq(ShareMoment::getId, commentBO.getMomentId());
            ShareMoment moment = shareMomentMapper.selectOne(momentWrapper);
            if (moment != null && !moment.getUserId().equals(commentBO.getUserId())) {
                message.setToUserId(moment.getUserId());
                message.setMessageType(1);
                shareMessageMapper.insert(message);
            }
        } else if (commentBO.getReplyUserId() != null && !commentBO.getReplyUserId().equals(commentBO.getUserId())) {
            message.setToUserId(commentBO.getReplyUserId());
            message.setMessageType(2);
            shareMessageMapper.insert(message);
        }
    }

    public PageResult<ShareCommentReplyBO> getComments(Long targetId, Integer type, int pageNo, int pageSize) {
        Page<ShareCommentReply> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ShareCommentReply> wrapper = new LambdaQueryWrapper<>();
        if (type == 1) {
            wrapper.eq(ShareCommentReply::getMomentId, targetId);
        } else {
            wrapper.eq(ShareCommentReply::getCircleId, targetId);
        }
        wrapper.orderByAsc(ShareCommentReply::getCreatedTime);
        shareCommentReplyMapper.selectPage(page, wrapper);
        return new PageResult<>(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                page.getRecords().stream().map(r -> {
                    ShareCommentReplyBO bo = new ShareCommentReplyBO();
                    bo.setId(r.getId());
                    bo.setMomentId(r.getMomentId());
                    bo.setCircleId(r.getCircleId());
                    if (r.getCreatedBy() != null) {
                        try {
                            bo.setUserId(Long.parseLong(r.getCreatedBy()));
                        } catch (NumberFormatException ignored) {
                            bo.setUserId(null);
                        }
                    }
                    bo.setContent(r.getContent());
                    bo.setReplyUserId(r.getReplyUserId());
                    bo.setType(r.getType());
                    return bo;
                }).toList());
    }

    public void updateCircle(ShareCircleBO circleBO) {
        if (circleBO == null || circleBO.getId() == null) {
            return;
        }
        ShareCircle updateEntity = new ShareCircle();
        updateEntity.setId(circleBO.getId());
        updateEntity.setTitle(circleBO.getTitle());
        shareCircleMapper.updateById(updateEntity);
    }

    public void deleteCircle(Long circleId) {
        if (circleId == null) {
            return;
        }
        shareCircleMapper.deleteById(circleId);
    }

    public void deleteMoment(Long momentId) {
        if (momentId == null) {
            return;
        }
        shareMomentMapper.deleteById(momentId);
    }

    public boolean hasUnreadMessage(Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<ShareMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareMessage::getToUserId, userId);
        wrapper.eq(ShareMessage::getStatus, 0);
        return shareMessageMapper.selectCount(wrapper) > 0;
    }

    public PageResult<ShareMessageBO> getMessages(Long userId, int pageNo, int pageSize) {
        Page<ShareMessage> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ShareMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareMessage::getToUserId, userId);
        wrapper.orderByDesc(ShareMessage::getCreatedTime);
        shareMessageMapper.selectPage(page, wrapper);
        return new PageResult<>(
                (int) page.getCurrent(),
                (int) page.getSize(),
                page.getTotal(),
                page.getRecords().stream().map(this::toShareMessageBO).toList());
    }

    public void markMessageAsRead(Long messageId) {
        if (messageId == null) {
            return;
        }
        ShareMessage message = new ShareMessage();
        message.setId(messageId);
        message.setStatus(1);
        shareMessageMapper.updateById(message);
    }

    public void markAllMessagesAsRead(Long userId) {
        if (userId == null) {
            return;
        }
        LambdaUpdateWrapper<ShareMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShareMessage::getToUserId, userId);
        wrapper.eq(ShareMessage::getStatus, 0);
        ShareMessage message = new ShareMessage();
        message.setStatus(1);
        shareMessageMapper.update(message, wrapper);
    }

    private SensitiveWordBO toSensitiveWordBO(SensitiveWords entity) {
        SensitiveWordBO bo = new SensitiveWordBO();
        bo.setId(entity.getId());
        bo.setWords(entity.getWords());
        bo.setType(entity.getType());
        return bo;
    }

    private ShareMessageBO toShareMessageBO(ShareMessage entity) {
        ShareMessageBO bo = new ShareMessageBO();
        bo.setId(entity.getId());
        bo.setFromUserId(entity.getFromUserId());
        bo.setToUserId(entity.getToUserId());
        bo.setContent(entity.getContent());
        bo.setMessageType(entity.getMessageType());
        bo.setMomentId(entity.getMomentId());
        bo.setStatus(entity.getStatus());
        bo.setCreatedTime(entity.getCreatedTime());
        return bo;
    }
}
