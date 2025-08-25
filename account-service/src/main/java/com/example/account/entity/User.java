// account-service/src/main/java/com/example/account/entity/User.java
package com.example.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Boolean isAdmin;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
