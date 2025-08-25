// account-service/src/main/java/com/example/account/controller/UserController.java
package com.example.account.controller;

import com.example.account.dto.LoginRequest;
import com.example.account.dto.RegisterRequest;
import com.example.account.entity.User;
import com.example.account.service.UserService;
import com.example.account.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );
            return ApiResponse.success(user);
        } catch (RuntimeException e) {
            // 返回错误信息而不是500状态码
            return ApiResponse.error(e.getMessage()); // 默认400状态码
        }
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest request) {
        System.out.println("=== NEW LOGIN METHOD WITH TRY-CATCH ===");  // 添加这行
        try {
            User user = userService.login(
                    request.getUsername(),
                    request.getPassword()
            );
            return ApiResponse.success(user);
        } catch (RuntimeException e) {
            System.out.println("=== CAUGHT EXCEPTION: " + e.getMessage() + " ===");  // 添加这行
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
}