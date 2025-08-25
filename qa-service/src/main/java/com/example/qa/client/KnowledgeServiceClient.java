// KnowledgeServiceClient.java - 添加精确查询方法
package com.example.qa.client;

import com.example.common.response.ApiResponse;
import com.example.knowledge.entity.Knowledge;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "knowledge-service")
public interface KnowledgeServiceClient {

    // 原有的搜索方法
    @PostMapping("/knowledge/search")
    ApiResponse<List<String>> searchKnowledge(@RequestParam String query);

    // ★★★ 新增：精确匹配问题的方法 ★★★
    @GetMapping("/knowledge/exact")
    ApiResponse<Knowledge> findByQuestion(@RequestParam String question);
}