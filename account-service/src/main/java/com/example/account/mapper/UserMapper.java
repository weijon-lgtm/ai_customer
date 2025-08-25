// account-service/src/main/java/com/example/account/mapper/UserMapper.java
package com.example.account.mapper;

import com.example.account.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username, password, email, is_admin, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{email}, #{isAdmin}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Update("UPDATE user SET password = #{password}, email = #{email}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(User user);
}
