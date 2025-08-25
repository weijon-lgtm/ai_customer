package com.example.qa.service;

import com.example.common.response.ApiResponse;
import com.example.knowledge.entity.Knowledge;
import com.example.qa.client.KnowledgeServiceClient;
import com.example.qa.entity.Answer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekService {

    private final WebClient webClient;
    private final KnowledgeServiceClient knowledgeServiceClient;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    @PostConstruct
    public void init() {
        log.info("DeepSeek Service 初始化完成");
        log.info("API URL: {}", baseUrl);
        log.info("Model: {}", model);
        log.info("API Key: {}...{}",
                apiKey.substring(0, 10),
                apiKey.substring(apiKey.length() - 4));
    }

    // 原有的主要方法，增加知识库查询逻辑
    @Cacheable(value = "answerCache", key = "#question")
    public Answer getAnswer(String question, Long userId, List<String> context) {
        try {
            log.info("正在处理问题: {}", question);

            // 第一步：尝试从知识库精确匹配
            Answer knowledgeAnswer = getAnswerFromKnowledge(question, userId);
            if (knowledgeAnswer != null) {
                log.info("从知识库找到精确匹配答案");
                return knowledgeAnswer;
            }

            // 第二步：知识库没有精确匹配，调用 DeepSeek API
            log.info("知识库无精确匹配，调用 DeepSeek API");
            return getAnswerFromDeepSeek(question, userId, context);

        } catch (Exception e) {
            log.error("获取答案时发生错误: {}", e.getMessage(), e);
            return createFallbackAnswer(question, userId, e.getMessage());
        }
    }

    // 修改后的支持多轮对话的方法 - 优先使用缓存
    public Answer getAnswerWithHistory(String question, Long userId, List<Map<String, String>> history) {
        try {
            log.info("正在处理多轮对话问题: {}, 历史对话数量: {}", question, history != null ? history.size() : 0);

            // 第一步：如果没有历史对话或历史对话为空，使用原有的缓存方法
            if (history == null || history.isEmpty()) {
                log.info("无历史对话，使用原有缓存方法 getAnswer()");
                return getAnswer(question, userId, List.of());
            }

            // 第二步：即使有历史对话，也先尝试从缓存获取单独问题的答案
            log.info("存在历史对话，但先尝试从缓存获取单独问题的答案");
            Answer cachedAnswer = tryGetCachedAnswer(question, userId);
            if (cachedAnswer != null) {
                log.info("从缓存找到答案，直接返回（多轮对话中的缓存命中）");
                return cachedAnswer;
            }

            // 第三步：检查知识库精确匹配
            Answer knowledgeAnswer = getAnswerFromKnowledge(question, userId);
            if (knowledgeAnswer != null) {
                log.info("从知识库找到精确匹配答案");
                return knowledgeAnswer;
            }

            // 第四步：检查问题是否需要上下文理解
            if (needsContextualUnderstanding(question, history)) {
                log.info("问题需要上下文理解，调用 DeepSeek API 进行多轮对话");
                return getAnswerFromDeepSeekWithHistory(question, userId, history);
            } else {
                log.info("问题不需要上下文，使用普通缓存方法处理");
                return getAnswer(question, userId, List.of());
            }

        } catch (Exception e) {
            log.error("获取答案时发生错误: {}", e.getMessage(), e);
            return createFallbackAnswer(question, userId, e.getMessage());
        }
    }

    private final org.springframework.cache.CacheManager cacheManager;

    // 修改构造函数
    public DeepSeekService(WebClient webClient,
                           KnowledgeServiceClient knowledgeServiceClient,
                           org.springframework.cache.CacheManager cacheManager) {
        this.webClient = webClient;
        this.knowledgeServiceClient = knowledgeServiceClient;
        this.cacheManager = cacheManager;
    }

    // 新增：手动查询缓存的方法
    private Answer tryGetCachedAnswer(String question, Long userId) {
        try {
            org.springframework.cache.Cache cache = cacheManager.getCache("answerCache");
            if (cache != null) {
                org.springframework.cache.Cache.ValueWrapper wrapper = cache.get(question);
                if (wrapper != null) {
                    Answer cachedAnswer = (Answer) wrapper.get();
                    log.info("从缓存获取到答案: {}", question);
                    return cachedAnswer;
                }
            }
            log.debug("缓存中未找到问题: {}", question);
            return null;
        } catch (Exception e) {
            log.debug("查询缓存失败: {}", e.getMessage());
            return null;
        }
    }

    // 新增：判断问题是否需要上下文理解
    private boolean needsContextualUnderstanding(String question, List<Map<String, String>> history) {
        if (history == null || history.isEmpty()) {
            return false;
        }

        String lowerQuestion = question.toLowerCase().trim();

        // 包含指代词的问题需要上下文
        String[] contextualWords = {
                "它", "这个", "那个", "他", "她", "这", "那", "上面", "前面", "刚才", "之前",
                "this", "that", "it", "above", "previous", "earlier", "what", "which"
        };

        for (String word : contextualWords) {
            if (lowerQuestion.contains(word)) {
                log.info("问题包含指代词 '{}', 需要上下文理解", word);
                return true;
            }
        }

        // 简短问题可能需要上下文
        if (lowerQuestion.length() < 10) {
            log.info("问题过于简短 ({}字符), 可能需要上下文理解", lowerQuestion.length());
            return true;
        }

        // 问号结尾的简单疑问可能需要上下文
        if (lowerQuestion.endsWith("?") || lowerQuestion.endsWith("？")) {
            String[] simpleQuestions = {"什么", "怎么", "如何", "为什么", "when", "how", "why", "what"};
            for (String word : simpleQuestions) {
                if (lowerQuestion.contains(word)) {
                    log.info("问题是简单疑问且包含 '{}', 可能需要上下文", word);
                    return true;
                }
            }
        }

        log.info("问题判定为独立问题，不需要上下文理解");
        return false;
    }

    // 从知识库获取精确匹配的答案
    private Answer getAnswerFromKnowledge(String question, Long userId) {
        try {
            log.info("尝试从知识库精确查询: {}", question);

            ApiResponse<Knowledge> response = knowledgeServiceClient.findByQuestion(question);

            if (response != null && response.getData() != null) {
                Knowledge knowledge = response.getData();
                if (knowledge.getAnswer() != null && !knowledge.getAnswer().trim().isEmpty()) {
                    log.info("知识库找到匹配答案，长度: {}", knowledge.getAnswer().length());
                    return new Answer(knowledge.getAnswer(), question, userId);
                }
            }

            log.debug("知识库中未找到精确匹配的问题");
            return null;

        } catch (Exception e) {
            log.warn("查询知识库失败，继续使用 DeepSeek: {}", e.getMessage());
            return null;
        }
    }

    // 原有的 DeepSeek API 调用逻辑，提取为单独方法
    private Answer getAnswerFromDeepSeek(String question, Long userId, List<String> context) {
        try {
            log.info("正在调用 DeepSeek API，问题: {}", question);

            // 构建请求消息
            List<Map<String, String>> messages = buildMessages(question, context);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            log.debug("发送请求到: {}/chat/completions", baseUrl);

            // 发送请求
            Mono<Map> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                log.error("DeepSeek API 返回错误状态: {}", response.statusCode());
                                return response.bodyToMono(String.class)
                                        .map(body -> {
                                            log.error("错误响应体: {}", body);
                                            return new RuntimeException("API调用失败: " + response.statusCode() + " - " + body);
                                        });
                            }
                    )
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .doOnError(error -> log.error("请求发送失败: {}", error.getMessage()));

            Map<String, Object> response = responseMono.block();

            String answerContent = extractAnswerFromResponse(response);
            log.info("DeepSeek API 调用成功，答案长度: {}", answerContent.length());

            return new Answer(answerContent, question, userId);

        } catch (WebClientResponseException e) {
            log.error("DeepSeek API HTTP错误: 状态码={}, 响应体={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API调用失败", e);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 支持历史对话的 DeepSeek API 调用
    private Answer getAnswerFromDeepSeekWithHistory(String question, Long userId, List<Map<String, String>> history) {
        try {
            log.info("正在调用 DeepSeek API，问题: {}, 历史对话: {}", question, history != null ? history.size() : 0);

            // 构建包含历史对话的请求消息
            List<Map<String, String>> messages = buildMessagesWithHistory(question, history);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            log.debug("发送请求到: {}/chat/completions, 消息数量: {}", baseUrl, messages.size());

            // 发送请求
            Mono<Map> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                log.error("DeepSeek API 返回错误状态: {}", response.statusCode());
                                return response.bodyToMono(String.class)
                                        .map(body -> {
                                            log.error("错误响应体: {}", body);
                                            return new RuntimeException("API调用失败: " + response.statusCode() + " - " + body);
                                        });
                            }
                    )
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .doOnError(error -> log.error("请求发送失败: {}", error.getMessage()));

            Map<String, Object> response = responseMono.block();

            String answerContent = extractAnswerFromResponse(response);
            log.info("DeepSeek API 调用成功，答案长度: {}", answerContent.length());

            return new Answer(answerContent, question, userId);

        } catch (WebClientResponseException e) {
            log.error("DeepSeek API HTTP错误: 状态码={}, 响应体={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API调用失败", e);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 流式接口保持不变，但也可以考虑加入知识库查询
    public Flux<String> getAnswerStream(String question, Long userId, List<String> context) {
        try {
            // 可选：也可以在流式接口中先检查知识库
            Answer knowledgeAnswer = getAnswerFromKnowledge(question, userId);
            if (knowledgeAnswer != null) {
                log.info("流式接口从知识库找到答案，直接返回");
                return Flux.just("data: {\"content\": \"" + knowledgeAnswer.getAnswer().replace("\"", "\\\"") + "\"}\n\n")
                        .concatWith(Flux.just("data: [DONE]\n\n"));
            }

            // 如果知识库没有，继续使用流式 DeepSeek
            List<Map<String, String>> messages = buildMessages(question, context);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            requestBody.put("max_tokens", 1000);

            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .onErrorResume(error -> {
                        log.error("流式调用失败: {}", error.getMessage());
                        return Flux.just("data: {\"error\": \"AI服务暂时不可用，请稍后再试\"}\n\n");
                    });

        } catch (Exception e) {
            log.error("初始化流式调用失败: {}", e.getMessage());
            return Flux.just("data: {\"error\": \"AI服务暂时不可用，请稍后再试\"}\n\n");
        }
    }

    // 原有的构建消息方法
    private List<Map<String, String>> buildMessages(String question, List<String> context) {
        List<Map<String, String>> messages = new ArrayList<>();

        StringBuilder systemPrompt = new StringBuilder("You are a helpful AI customer service assistant.");
        if (context != null && !context.isEmpty()) {
            systemPrompt.append(" Use the following information to answer the question: \n");
            for (String ctx : context) {
                systemPrompt.append(ctx).append("\n");
            }
        }

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt.toString());
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);

        return messages;
    }

    // 构建包含历史对话的消息列表
    private List<Map<String, String>> buildMessagesWithHistory(String question, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统提示
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful AI customer service assistant. Please provide helpful and accurate responses based on the conversation history and the current question.");
        messages.add(systemMessage);

        // 添加历史对话（限制数量，避免token过多）
        if (history != null && !history.isEmpty()) {
            int maxHistoryCount = 10; // 最多保留最近10轮对话
            int startIndex = Math.max(0, history.size() - maxHistoryCount);

            for (int i = startIndex; i < history.size(); i++) {
                Map<String, String> historyItem = history.get(i);
                if (historyItem != null) {
                    String role = historyItem.get("role");
                    String content = historyItem.get("content");

                    if (role != null && content != null && !content.trim().isEmpty()) {
                        Map<String, String> message = new HashMap<>();
                        message.put("role", role);
                        message.put("content", content.trim());
                        messages.add(message);
                    }
                }
            }

            log.info("添加了 {} 条历史对话", history.size() - startIndex);
        }

        // 添加当前问题
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);

        log.debug("构建完成，总消息数量: {}", messages.size());
        return messages;
    }

    private String extractAnswerFromResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                return "抱歉，未收到有效响应";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null && !content.trim().isEmpty()) {
                        return content.trim();
                    }
                }
            }

            log.warn("响应格式异常，无法提取答案内容: {}", response);
            return "抱歉，未能获取到有效回复";

        } catch (Exception e) {
            log.error("解析响应时发生错误: {}", e.getMessage());
            return "抱歉，解析回复时发生错误";
        }
    }

    private Answer createFallbackAnswer(String question, Long userId, String errorMsg) {
        String fallbackAnswer = String.format(
                "抱歉，服务暂时不可用。您的问题是：\"%s\"。\n\n错误信息：%s\n\n请稍后再试或联系技术支持。",
                question, errorMsg
        );
        return new Answer(fallbackAnswer, question, userId);
    }

    public boolean testConnection() {
        try {
            log.info("开始测试 DeepSeek API 连接...");

            List<Map<String, String>> testMessages = new ArrayList<>();
            Map<String, String> testMessage = new HashMap<>();
            testMessage.put("role", "user");
            testMessage.put("content", "Hello");
            testMessages.add(testMessage);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", testMessages);
            requestBody.put("max_tokens", 5);

            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("DeepSeek API 连接测试成功");
            return true;

        } catch (Exception e) {
            log.error("DeepSeek API 连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}