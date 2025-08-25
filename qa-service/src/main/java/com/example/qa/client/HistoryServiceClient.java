// qa-service/src/main/java/com/example/qa/client/HistoryServiceClient.java
package com.example.qa.client;

import com.example.common.response.ApiResponse;
import com.example.qa.entity.Answer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "history-service")
public interface HistoryServiceClient {

    @PostMapping("/history/save")
    ApiResponse<Void> saveHistory(@RequestBody Answer answer);
}

