// qa-service/src/main/java/com/example/qa/entity/Question.java
package com.example.qa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String content;
    private Long userId;
}
