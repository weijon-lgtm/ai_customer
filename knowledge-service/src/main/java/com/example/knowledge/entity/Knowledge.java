// knowledge-service/src/main/java/com/example/knowledge/entity/Knowledge.java
package com.example.knowledge.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Knowledge {
    private Long id;
    private String question;
    private String answer;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
