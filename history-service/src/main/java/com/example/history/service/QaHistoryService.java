// history-service/src/main/java/com/example/history/service/QaHistoryService.java
package com.example.history.service;

import com.example.history.dto.PageResult;
import com.example.history.entity.QaHistory;
import com.example.history.mapper.QaHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QaHistoryService {

    private final QaHistoryMapper qaHistoryMapper;

    public void saveHistory(QaHistory qaHistory) {
        qaHistoryMapper.insert(qaHistory);
    }

    public List<QaHistory> getHistoryByUserId(Long userId) {
        return qaHistoryMapper.findByUserId(userId);
    }

    // 新增分页查询方法
    public PageResult<QaHistory> getHistoryByUserIdWithPagination(Long userId, int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<QaHistory> historyList = qaHistoryMapper.findByUserIdWithPagination(userId, offset, size);

        // 查询总数
        long total = qaHistoryMapper.countByUserId(userId);

        return PageResult.of(historyList, total, page, size);
    }

    public QaHistory getHistoryById(Long id) {
        return qaHistoryMapper.findById(id);
    }
}