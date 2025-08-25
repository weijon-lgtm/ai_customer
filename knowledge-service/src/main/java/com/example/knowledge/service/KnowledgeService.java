// KnowledgeService.java - 添加精确查询方法
package com.example.knowledge.service;

import com.example.knowledge.entity.Knowledge;
import com.example.knowledge.mapper.KnowledgeMapper;
import com.example.knowledge.dto.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;

    public Knowledge createKnowledge(Knowledge knowledge) {
        knowledgeMapper.insert(knowledge);
        return knowledge;
    }

    public Knowledge updateKnowledge(Knowledge knowledge) {
        knowledgeMapper.update(knowledge);
        return knowledge;
    }

    public void deleteKnowledge(Long id) {
        knowledgeMapper.delete(id);
    }

    public Knowledge getKnowledgeById(Long id) {
        return knowledgeMapper.findById(id);
    }

    public List<Knowledge> getAllKnowledge() {
        return knowledgeMapper.findAll();
    }

    // 分页获取所有知识
    public PageResult<Knowledge> getAllKnowledge(int page, int size) {
        int offset = (page - 1) * size;
        List<Knowledge> data = knowledgeMapper.findByPage(offset, size);
        long total = knowledgeMapper.countAll();
        return PageResult.of(data, total, page, size);
    }

    // 智能问答 - 修正版本，使用正确的字段名
    public String askQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return "请输入您的问题。";
        }

        // 1. 精确匹配
        Knowledge exactMatch = knowledgeMapper.findByExactQuestion(question);
        if (exactMatch != null) {
            return exactMatch.getAnswer(); // 使用 getAnswer()
        }

        // 2. 模糊匹配
        Knowledge fuzzyMatch = knowledgeMapper.findByFuzzyQuestion(question);
        if (fuzzyMatch != null) {
            return fuzzyMatch.getAnswer(); // 使用 getAnswer()
        }

        // 3. 没有找到匹配答案
        return "抱歉，我没有找到相关答案。您可以尝试用不同的方式提问，或者联系管理员添加相关问题。";
    }

    public List<Knowledge> searchKnowledge(String query) {
        return knowledgeMapper.search(query);
    }

    // ★★★ 新增：精确查询方法 - 供 QA 服务调用 ★★★
    public Knowledge findByExactQuestion(String question) {
        try {
            log.info("精确查询问题: {}", question);
            Knowledge result = knowledgeMapper.findByExactQuestion(question);
            if (result != null) {
                log.info("找到精确匹配: 问题={}, 答案长度={}", result.getQuestion(), result.getAnswer().length());
            } else {
                log.debug("未找到精确匹配的问题: {}", question);
            }
            return result;
        } catch (Exception e) {
            log.error("精确查询问题时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
}