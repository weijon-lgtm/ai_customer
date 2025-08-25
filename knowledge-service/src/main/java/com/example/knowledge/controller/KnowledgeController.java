// KnowledgeController.java - 添加精确查询接口
package com.example.knowledge.controller;

import com.example.common.response.ApiResponse;
import com.example.knowledge.entity.Knowledge;
import com.example.knowledge.service.KnowledgeService;
import com.example.knowledge.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    // 新增问答 - 适配前端 POST /knowledge
    @PostMapping
    public ApiResponse<Knowledge> createKnowledge(@RequestBody Knowledge knowledge) {
        Knowledge createdKnowledge = knowledgeService.createKnowledge(knowledge);
        return ApiResponse.success(createdKnowledge);
    }

    // 更新问答 - 适配前端 PUT /knowledge/{id}
    @PutMapping("/{id}")
    public ApiResponse<Knowledge> updateKnowledge(@PathVariable Long id, @RequestBody Knowledge knowledge) {
        knowledge.setId(id);
        Knowledge updatedKnowledge = knowledgeService.updateKnowledge(knowledge);
        return ApiResponse.success(updatedKnowledge);
    }

    // 删除问答 - 适配前端 DELETE /knowledge/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return ApiResponse.success(null);
    }

    // 根据ID获取
    @GetMapping("/{id}")
    public ApiResponse<Knowledge> getKnowledgeById(@PathVariable Long id) {
        Knowledge knowledge = knowledgeService.getKnowledgeById(id);
        return ApiResponse.success(knowledge);
    }

    // 获取所有问答 - 支持分页
    @GetMapping("/all")
    public ApiResponse<PageResult<Knowledge>> getAllKnowledge(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<Knowledge> result = knowledgeService.getAllKnowledge(page, size);
        return ApiResponse.success(result);
    }

    // 智能问答接口
    @PostMapping("/ask")
    public ApiResponse<String> askQuestion(@RequestParam String question) {
        String answer = knowledgeService.askQuestion(question);
        return ApiResponse.success(answer);
    }

    // 搜索知识库
    @GetMapping("/search")
    public ApiResponse<List<Knowledge>> searchKnowledge(@RequestParam String query) {
        List<Knowledge> results = knowledgeService.searchKnowledge(query);
        return ApiResponse.success(results);
    }

    // ★★★ 新增：精确查询问题接口 - 供 QA 服务调用 ★★★
    @GetMapping("/exact")
    public ApiResponse<Knowledge> findByQuestion(@RequestParam String question) {
        Knowledge knowledge = knowledgeService.findByExactQuestion(question);
        return ApiResponse.success(knowledge);
    }

    // 保留原有接口以兼容
    @PostMapping("/create")
    public ApiResponse<Knowledge> createKnowledgeOld(@RequestBody Knowledge knowledge) {
        return createKnowledge(knowledge);
    }

    @PutMapping("/update")
    public ApiResponse<Knowledge> updateKnowledgeOld(@RequestBody Knowledge knowledge) {
        Knowledge updatedKnowledge = knowledgeService.updateKnowledge(knowledge);
        return ApiResponse.success(updatedKnowledge);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteKnowledgeOld(@PathVariable Long id) {
        return deleteKnowledge(id);
    }
}