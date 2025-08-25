package com.example.qa.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，解决Redis缓存兼容性问题
public class Answer {
    private String answer;      // 答案内容
    private String question;    // 问题内容
    private Long userId;        // 用户ID
}