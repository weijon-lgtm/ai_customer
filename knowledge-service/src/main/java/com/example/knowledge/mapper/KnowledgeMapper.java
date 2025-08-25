// KnowledgeMapper.java - 修正版本，使用正确的数据库字段名
package com.example.knowledge.mapper;

import com.example.knowledge.entity.Knowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 使用数据库实际字段名 question, answer
    @Insert("INSERT INTO knowledge(question, answer, create_time, update_time) " +
            "VALUES(#{question}, #{answer}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Knowledge knowledge);

    @Update("UPDATE knowledge SET question = #{question}, answer = #{answer}, " +
            "update_time = NOW() WHERE id = #{id}")
    int update(Knowledge knowledge);

    @Delete("DELETE FROM knowledge WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT * FROM knowledge WHERE id = #{id}")
    Knowledge findById(Long id);

    @Select("SELECT * FROM knowledge ORDER BY update_time DESC")
    List<Knowledge> findAll();

    // 分页查询
    @Select("SELECT * FROM knowledge ORDER BY update_time DESC LIMIT #{offset}, #{size}")
    List<Knowledge> findByPage(@Param("offset") int offset, @Param("size") int size);

    // 获取总数
    @Select("SELECT COUNT(*) FROM knowledge")
    long countAll();

    // 搜索 - 在question和answer字段中搜索
    @Select("SELECT * FROM knowledge WHERE question LIKE CONCAT('%', #{query}, '%') " +
            "OR answer LIKE CONCAT('%', #{query}, '%') ORDER BY update_time DESC")
    List<Knowledge> search(String query);

    // 精确匹配问题
    @Select("SELECT * FROM knowledge WHERE question = #{question} LIMIT 1")
    Knowledge findByExactQuestion(String question);

    // 模糊匹配问题 - 按问题长度排序，优先返回更精确的匹配
    @Select("SELECT * FROM knowledge WHERE question LIKE CONCAT('%', #{question}, '%') " +
            "ORDER BY CHAR_LENGTH(question) ASC LIMIT 1")
    Knowledge findByFuzzyQuestion(String question);
}