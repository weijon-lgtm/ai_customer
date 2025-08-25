// account-service/src/main/java/com/example/account/service/UserService.java
package com.example.account.service;

import com.example.account.entity.User;
import com.example.account.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public User register(String username, String password, String email) {
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In a real app, hash the password
        user.setEmail(email);
        user.setIsAdmin(false);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }

    public User getUserById(Long id) {
        return userMapper.findById(id);
    }
}