package com.colacode.practice.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.practice.infra.entity.PracticeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeInfoMapper extends BaseMapper<PracticeInfo> {

    @Select("SELECT DATE(created_time) as date, COUNT(*) as count " +
            "FROM practice_info " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "GROUP BY DATE(created_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectPracticeCountByDate(@Param("userId") Long userId);
}
