// 修改后的 QaController.java - 支持多轮对话
package com.example.qa.controller;

import com.example.common.response.ApiResponse;
import com.example.qa.client.HistoryServiceClient;
import com.example.qa.client.KnowledgeServiceClient;
import com.example.qa.entity.Answer;
import com.example.qa.entity.Question;
import com.example.qa.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
@Slf4j
public class QaController {

    private final DeepSeekService deepSeekService;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final HistoryServiceClient historyServiceClient;

    // 修改后的聊天接口 - 支持多轮对话
    @PostMapping("/message")
    public Map<String, Object> chatMessage(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到聊天请求，完整请求体: {}", request);

            String questionText = (String) request.get("question");
            Object userIdObj = request.get("userId");

            // 获取对话历史 - 修改为安全获取
            List<Map<String, String>> history = null;
            if (request.containsKey("history")) {
                history = (List<Map<String, String>>) request.get("history");
            }

            log.info("解析出的问题: [{}], 用户ID对象: [{}], 历史对话数量: {}",
                    questionText, userIdObj, history != null ? history.size() : 0);

            if (questionText == null || questionText.trim().isEmpty()) {
                log.warn("问题内容为空，使用默认问题");
                questionText = "你好";
            }

            Long userId;
            try {
                userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : 1L;
            } catch (Exception e) {
                log.warn("用户ID解析失败: {}, 使用默认值1", userIdObj);
                userId = 1L;
            }

            log.info("最终处理的问题: [{}], 用户ID: [{}]", questionText, userId);

            try {
                log.info("开始调用 DeepSeek 服务...");
                Answer answer;

                // 根据是否有历史对话选择不同的方法
                if (history == null || history.isEmpty()) {
                    log.info("无历史对话，使用普通方法（支持Redis缓存）");
                    answer = deepSeekService.getAnswer(questionText, userId, List.of());
                } else {
                    log.info("存在历史对话，使用多轮对话方法");
                    answer = deepSeekService.getAnswerWithHistory(questionText, userId, history);
                }

                log.info("DeepSeek 服务返回: {}", answer);

                String finalAnswer;
                if (answer != null && answer.getAnswer() != null && !answer.getAnswer().trim().isEmpty()) {
                    finalAnswer = answer.getAnswer();
                    log.info("使用 DeepSeek 回答: {}", finalAnswer);
                } else {
                    finalAnswer = "感谢您的提问：" + questionText + "。这是一个测试回答。";
                    log.info("DeepSeek 返回空结果，使用备用回答: {}", finalAnswer);
                }

                response.put("message", finalAnswer);
                response.put("success", true);

                // 尝试保存历史记录（异步，不影响响应）
                try {
                    if (answer != null) {
                        answer.setQuestion(questionText);
                        answer.setAnswer(finalAnswer);
                        historyServiceClient.saveHistory(answer);
                        log.info("历史记录保存成功");
                    }
                } catch (Exception e) {
                    log.warn("保存历史记录失败: {}", e.getMessage());
                }

                log.info("聊天接口成功返回: {}", response);
                return response;

            } catch (Exception e) {
                log.error("调用 DeepSeek 服务失败: {}", e.getMessage(), e);

                String fallbackAnswer = "您好！感谢您的提问：" + questionText + "。由于服务暂时繁忙，这是一个临时回答。请稍后再试。";

                response.put("message", fallbackAnswer);
                response.put("success", true);

                log.info("使用备用回答: {}", fallbackAnswer);
                return response;
            }

        } catch (Exception e) {
            log.error("聊天接口处理错误: {}", e.getMessage(), e);

            response.put("message", "抱歉，服务暂时不可用，请稍后重试。");
            response.put("success", false);
            response.put("error", e.getMessage());

            return response;
        }
    }

    // 原有的 ask 接口保持不变
    @PostMapping("/ask")
    public ApiResponse<Answer> ask(@RequestBody Question question) {
        try {
            log.info("收到问题: {}, 用户ID: {}", question.getContent(), question.getUserId());

            List<String> context = List.of();

            try {
                log.info("尝试调用知识库服务...");
                ApiResponse<List<String>> knowledgeResponse = knowledgeServiceClient.searchKnowledge(question.getContent());

                if (knowledgeResponse != null && knowledgeResponse.getData() != null) {
                    context = knowledgeResponse.getData();
                    log.info("知识库返回 {} 条相关信息", context.size());
                } else {
                    log.warn("知识库服务返回空结果");
                }
            } catch (Exception e) {
                log.warn("知识库服务调用失败，使用空上下文继续处理: {}", e.getMessage());
            }

            Answer answer = deepSeekService.getAnswer(question.getContent(), question.getUserId(), context);

            if (answer.getQuestion() == null || answer.getQuestion().trim().isEmpty()) {
                answer.setQuestion(question.getContent());
            }
            if (answer.getAnswer() == null || answer.getAnswer().trim().isEmpty()) {
                answer.setAnswer("抱歉，暂时无法提供回答。");
            }

            try {
                log.info("保存历史记录: question={}, answer={}", answer.getQuestion(), answer.getAnswer());
                historyServiceClient.saveHistory(answer);
                log.info("历史记录保存成功");
            } catch (Exception e) {
                log.warn("保存历史记录失败，但不影响回答: {}", e.getMessage());
            }

            return ApiResponse.success(answer);

        } catch (Exception e) {
            log.error("处理问题时发生错误: {}", e.getMessage(), e);

            Answer fallbackAnswer = new Answer(
                    "抱歉，服务暂时不可用。请稍后再试。",
                    question.getContent(),
                    question.getUserId()
            );

            return ApiResponse.success(fallbackAnswer);
        }
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestBody Question question) {
        try {
            List<String> context = List.of();

            try {
                ApiResponse<List<String>> knowledgeResponse = knowledgeServiceClient.searchKnowledge(question.getContent());
                if (knowledgeResponse != null && knowledgeResponse.getData() != null) {
                    context = knowledgeResponse.getData();
                }
            } catch (Exception e) {
                log.warn("流式接口知识库服务调用失败: {}", e.getMessage());
            }

            Flux<String> answerStream = deepSeekService.getAnswerStream(question.getContent(), question.getUserId(), context);

            answerStream.reduce("", (acc, part) -> acc + part)
                    .doOnNext(fullAnswer -> {
                        try {
                            Answer answer = new Answer(fullAnswer, question.getContent(), question.getUserId());
                            historyServiceClient.saveHistory(answer);
                            log.info("流式回答历史记录保存成功");
                        } catch (Exception e) {
                            log.warn("保存流式回答历史记录失败: {}", e.getMessage());
                        }
                    })
                    .subscribe();

            return answerStream;

        } catch (Exception e) {
            log.error("处理流式问题时发生错误: {}", e.getMessage(), e);
            return Flux.just("data: {\"error\": \"服务暂时不可用，请稍后再试\"}\n\n");
        }
    }
}