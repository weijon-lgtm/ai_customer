// history-service/src/main/java/com/example/history/mapper/QaHistoryMapper.java
package com.example.history.mapper;

import com.example.history.entity.QaHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QaHistoryMapper {

    @Insert("INSERT INTO qa_history(user_id, question, answer, create_time) " +
            "VALUES(#{userId}, #{question}, #{answer}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(QaHistory qaHistory);

    @Select("SELECT * FROM qa_history WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<QaHistory> findByUserId(Long userId);

    @Select("SELECT * FROM qa_history WHERE id = #{id}")
    QaHistory findById(Long id);

    // 新增分页查询方法
    @Select("SELECT * FROM qa_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<QaHistory> findByUserIdWithPagination(@Param("userId") Long userId,
                                               @Param("offset") int offset,
                                               @Param("size") int size);

    // 新增查询总数方法
    @Select("SELECT COUNT(*) FROM qa_history WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}

