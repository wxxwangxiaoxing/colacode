package com.colacode.subject.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.subject.infra.entity.EsSyncStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface EsSyncStatusMapper extends BaseMapper<EsSyncStatus> {

    @Select("SELECT * FROM es_sync_status " +
            "WHERE status IN (0, 2) " +
            "AND retry_count < max_retry_count " +
            "AND (next_retry_time IS NULL OR next_retry_time <= #{now}) " +
            "AND is_deleted = 0 " +
            "ORDER BY created_time ASC " +
            "LIMIT #{limit}")
    List<EsSyncStatus> selectRetryableTasks(@Param("now") Date now, @Param("limit") int limit);

    @Select("SELECT * FROM es_sync_status " +
            "WHERE status = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY updated_time DESC " +
            "LIMIT #{limit}")
    List<EsSyncStatus> selectRecentSuccessTasks(@Param("limit") int limit);
}