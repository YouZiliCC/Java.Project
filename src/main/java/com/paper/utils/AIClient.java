package com.paper.utils;

import com.paper.config.AIProperties;
import com.paper.config.AIPromptProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI客户端工具类 - 使用Spring配置注入
 * 调用DeepSeek或其他OpenAI兼容的API
 */
@Component
public class AIClient {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final AIProperties aiProperties;
    private final AIPromptProperties promptProperties;
    private final ResourceLoader resourceLoader;
    
    @Autowired
    public AIClient(AIProperties aiProperties, 
                    AIPromptProperties promptProperties,
                    ResourceLoader resourceLoader) {
        this.aiProperties = aiProperties;
        this.promptProperties = promptProperties;
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * 调用OpenAI兼容的Chat Completions API
     */
    public String callChatCompletion(String systemPrompt, String userPrompt) 
            throws IOException {
        return callChatCompletion(systemPrompt, userPrompt, aiProperties.getTimeout());
    }
    
    /**
     * 调用OpenAI兼容的Chat Completions API（带超时）
     */
    public String callChatCompletion(String systemPrompt, String userPrompt, 
                                   double timeoutSeconds) throws IOException {
        String baseUrl = aiProperties.getBaseUrl();
        String apiKey = aiProperties.getKey();
        String model = aiProperties.getModel();
        
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new RuntimeException("未配置 ai.api.base-url");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("未配置 ai.api.key");
        }
        if (model == null || model.isEmpty()) {
            throw new RuntimeException("未配置 ai.api.model");
        }
        
        String endpoint = baseUrl.replaceAll("/$", "") + "/chat/completions";
        
        // 构造请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.2);
        
        // 构造消息列表
        Map<String, String>[] messages;
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages = new Map[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            };
        } else {
            messages = new Map[]{
                Map.of("role", "user", "content", userPrompt)
            };
        }
        payload.put("messages", messages);
        
        String jsonPayload = objectMapper.writeValueAsString(payload);
        
        // 发送HTTP POST请求
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout((int) (timeoutSeconds * 1000));
        conn.setReadTimeout((int) (timeoutSeconds * 1000));
        
        // 发送请求体
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                throw new IOException("API调用失败，响应码: " + responseCode + 
                    ", 错误信息: " + response.toString());
            }
        }
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            // 解析响应
            var root = objectMapper.readTree(response.toString());
            try {
                return root.get("choices").get(0).get("message").get("content").asText().strip();
            } catch (Exception e) {
                // 兜底：返回原始响应
                return response.toString();
            }
        }
    }
    
    /**
     * 加载期刊详情系统提示词
     */
    public String loadJournalDetailPrompt() {
        return loadPrompt(promptProperties.getJournalDetail());
    }
    
    /**
     * 加载推荐匹配系统提示词
     */
    public String loadRecommendMatchPrompt() {
        return loadPrompt(promptProperties.getRecommendMatch());
    }
    
    /**
     * 加载系统提示词
     */
    private String loadPrompt(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        try {
            Resource resource = resourceLoader.getResource(path);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8).strip();
                }
            }
        } catch (IOException e) {
            System.err.println("无法加载提示词文件: " + path + ", " + e.getMessage());
        }
        return "";
    }
    
    /**
     * 获取当前配置信息
     */
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("baseUrl", aiProperties.getBaseUrl());
        config.put("model", aiProperties.getModel());
        config.put("apiKeySet", 
            aiProperties.getKey() != null && !aiProperties.getKey().isEmpty() ? "true" : "false");
        return config;
    }
}
