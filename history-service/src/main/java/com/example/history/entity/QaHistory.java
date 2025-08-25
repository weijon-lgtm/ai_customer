// history-service/src/main/java/com/example/history/entity/QaHistory.java
package com.example.history.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaHistory {
    private Long id;
    private Long userId;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}
