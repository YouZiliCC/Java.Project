package com.paper.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.config.EnvConfig;

/**
 * AI 服务类
 * 统一管理各种 AI API 的调用
 * 支持 OpenAI、Gemini、Claude、DeepSeek、Ollama 等
 */
public class AIService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送消息给 AI 并获取回复
     * 自动根据 EnvConfig 中的配置选择 AI 提供商
     * 
     * @param message 用户消息
     * @return AI 回复
     */
    public String chat(String message) throws IOException {
        return chat(message, null);
    }
    
    /**
     * 发送消息给 AI 并获取回复（带系统提示）
     * 
     * @param message 用户消息
     * @param systemPrompt 系统提示（可选）
     * @return AI 回复
     */
    public String chat(String message, String systemPrompt) throws IOException {
        String provider = EnvConfig.getAIProvider();
        
        // 检查 API Key 是否配置
        String apiKey = EnvConfig.getCurrentAIApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your-") || apiKey.startsWith("sk-your-")) {
            if (!"ollama".equalsIgnoreCase(provider)) {
                return "错误：AI API Key 未配置。请在 .env 文件中设置 " + provider.toUpperCase() + "_API_KEY";
            }
        }
        
        return switch (provider.toLowerCase()) {
            case "openai" -> callOpenAI(message, systemPrompt);
            case "deepseek" -> callDeepSeek(message, systemPrompt);
            case "gemini" -> callGemini(message, systemPrompt);
            case "claude" -> callClaude(message, systemPrompt);
            case "ollama" -> callOllama(message, systemPrompt);
            default -> "不支持的 AI 提供商: " + provider;
        };
    }
    
    /**
     * 调用 OpenAI API
     */
    private String callOpenAI(String message, String systemPrompt) throws IOException {
        String apiBase = EnvConfig.get(EnvConfig.OPENAI_API_BASE, "https://api.openai.com/v1");
        String apiKey = EnvConfig.get(EnvConfig.OPENAI_API_KEY);
        String model = EnvConfig.get(EnvConfig.OPENAI_MODEL, "gpt-3.5-turbo");
        
        return callOpenAICompatible(apiBase, apiKey, model, message, systemPrompt);
    }
    
    /**
     * 调用 DeepSeek API（兼容 OpenAI 格式）
     */
    private String callDeepSeek(String message, String systemPrompt) throws IOException {
        String apiBase = EnvConfig.get(EnvConfig.DEEPSEEK_API_BASE, "https://api.deepseek.com/v1");
        String apiKey = EnvConfig.get(EnvConfig.DEEPSEEK_API_KEY);
        String model = EnvConfig.get(EnvConfig.DEEPSEEK_MODEL, "deepseek-chat");
        
        return callOpenAICompatible(apiBase, apiKey, model, message, systemPrompt);
    }
    
    /**
     * 调用 OpenAI 兼容的 API（OpenAI、DeepSeek 等）
     */
    private String callOpenAICompatible(String apiBase, String apiKey, String model, 
                                         String message, String systemPrompt) throws IOException {
        URL url = new URL(apiBase + "/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 2000);
            
            // 构建消息数组
            java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    return jsonResponse.path("choices").path(0).path("message").path("content").asText();
                }
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line);
                    }
                    return "API 调用失败 (" + responseCode + "): " + error.toString();
                }
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 调用 Google Gemini API
     */
    private String callGemini(String message, String systemPrompt) throws IOException {
        String apiKey = EnvConfig.get(EnvConfig.GEMINI_API_KEY);
        String model = EnvConfig.get(EnvConfig.GEMINI_MODEL, "gemini-pro");
        
        String fullMessage = systemPrompt != null ? systemPrompt + "\n\n" + message : message;
        
        URL url = new URL("https://generativelanguage.googleapis.com/v1/models/" + model + 
                          ":generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            java.util.List<Map<String, Object>> contents = new java.util.ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            java.util.List<Map<String, String>> parts = new java.util.ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", fullMessage);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    return jsonResponse.path("candidates").path(0).path("content")
                                       .path("parts").path(0).path("text").asText();
                }
            } else {
                return "Gemini API 调用失败: " + responseCode;
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 调用 Anthropic Claude API
     */
    private String callClaude(String message, String systemPrompt) throws IOException {
        String apiKey = EnvConfig.get(EnvConfig.CLAUDE_API_KEY);
        String model = EnvConfig.get(EnvConfig.CLAUDE_MODEL, "claude-3-sonnet-20240229");
        
        URL url = new URL("https://api.anthropic.com/v1/messages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 2000);
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                requestBody.put("system", systemPrompt);
            }
            
            java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);
            requestBody.put("messages", messages);
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    return jsonResponse.path("content").path(0).path("text").asText();
                }
            } else {
                return "Claude API 调用失败: " + responseCode;
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 调用本地 Ollama API
     */
    private String callOllama(String message, String systemPrompt) throws IOException {
        String apiBase = EnvConfig.get(EnvConfig.OLLAMA_API_BASE, "http://localhost:11434");
        String model = EnvConfig.get(EnvConfig.OLLAMA_MODEL, "llama2");
        
        URL url = new URL(apiBase + "/api/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000); // Ollama 本地可能较慢
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("stream", false);
            
            java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    return jsonResponse.path("message").path("content").asText();
                }
            } else {
                return "Ollama API 调用失败: " + responseCode + " (请确保 Ollama 正在运行)";
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 获取当前 AI 配置信息
     */
    public static Map<String, Object> getAIConfigInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", EnvConfig.getAIProvider());
        info.put("model", EnvConfig.getCurrentAIModel());
        info.put("apiKeyConfigured", EnvConfig.hasValidApiKey(
            EnvConfig.getAIProvider().toUpperCase() + "_API_KEY"));
        return info;
    }
}
