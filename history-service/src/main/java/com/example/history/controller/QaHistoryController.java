// history-service/src/main/java/com/example/history/controller/QaHistoryController.java
package com.example.history.controller;

import com.example.common.response.ApiResponse;
import com.example.history.dto.PageResult;
import com.example.history.entity.QaHistory;
import com.example.history.service.QaHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class QaHistoryController {

    private final QaHistoryService qaHistoryService;

    @PostMapping("/save")
    public ApiResponse<Void> saveHistory(@RequestBody QaHistory qaHistory) {
        qaHistoryService.saveHistory(qaHistory);
        return ApiResponse.success(null);
    }

    // 修改原有接口，支持分页参数
    @GetMapping("/user/{userId}")
    public ApiResponse<?> getHistoryByUserId(@PathVariable Long userId,
                                             @RequestParam(value = "page", required = false) Integer page,
                                             @RequestParam(value = "size", required = false) Integer size) {

        // 如果没有分页参数，使用原有的查询方式（向后兼容）
        if (page == null || size == null) {
            List<QaHistory> historyList = qaHistoryService.getHistoryByUserId(userId);
            return ApiResponse.success(historyList);
        }

        // 使用分页查询
        PageResult<QaHistory> pageResult = qaHistoryService.getHistoryByUserIdWithPagination(userId, page, size);
        return ApiResponse.success(pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<QaHistory> getHistoryById(@PathVariable Long id) {
        QaHistory qaHistory = qaHistoryService.getHistoryById(id);
        return ApiResponse.success(qaHistory);
    }
}