package com.colacode.subject.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.colacode.subject.domain.bo.ContributeStat;
import com.colacode.subject.infra.entity.SubjectInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SubjectInfoMapper extends BaseMapper<SubjectInfo> {

    @Select("SELECT created_by, COUNT(*) as contribute_count " +
            "FROM subject_info " +
            "WHERE created_by IS NOT NULL AND created_by != '' AND is_deleted = 0 " +
            "GROUP BY created_by " +
            "ORDER BY contribute_count DESC " +
            "LIMIT #{limit}")
    List<ContributeStat> selectContributeStats(@Param("limit") int limit);

    @Update("UPDATE subject_info SET browse_count = browse_count + 1 WHERE id = #{subjectId} AND is_deleted = 0")
    int incrBrowseCount(@Param("subjectId") Long subjectId);
}
