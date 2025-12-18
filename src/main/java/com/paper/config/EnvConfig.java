package com.paper.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 集中式环境配置管理
 * 支持从 .env 文件和系统环境变量读取配置
 * 
 * 使用方式：
 *   String apiKey = EnvConfig.get("OPENAI_API_KEY");
 *   String dbHost = EnvConfig.get("MYSQL_HOST", "localhost");
 */
public class EnvConfig {
    
    private static final Map<String, String> config = new HashMap<>();
    private static boolean initialized = false;
    
    // ============ 配置键常量 ============
    
    // 数据库配置
    public static final String DB_MODE = "DB_MODE";
    public static final String MYSQL_HOST = "MYSQL_HOST";
    public static final String MYSQL_PORT = "MYSQL_PORT";
    public static final String MYSQL_DATABASE = "MYSQL_DATABASE";
    public static final String MYSQL_USER = "MYSQL_USER";
    public static final String MYSQL_PASSWORD = "MYSQL_PASSWORD";
    
    // 邮件配置
    public static final String MAIL_ENABLED = "MAIL_ENABLED";
    public static final String MAIL_HOST = "MAIL_HOST";
    public static final String MAIL_PORT = "MAIL_PORT";
    public static final String MAIL_USERNAME = "MAIL_USERNAME";
    public static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    
    // AI 配置
    public static final String AI_PROVIDER = "AI_PROVIDER";
    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static final String OPENAI_API_BASE = "OPENAI_API_BASE";
    public static final String OPENAI_MODEL = "OPENAI_MODEL";
    public static final String GEMINI_API_KEY = "GEMINI_API_KEY";
    public static final String GEMINI_MODEL = "GEMINI_MODEL";
    public static final String CLAUDE_API_KEY = "CLAUDE_API_KEY";
    public static final String CLAUDE_MODEL = "CLAUDE_MODEL";
    public static final String DEEPSEEK_API_KEY = "DEEPSEEK_API_KEY";
    public static final String DEEPSEEK_API_BASE = "DEEPSEEK_API_BASE";
    public static final String DEEPSEEK_MODEL = "DEEPSEEK_MODEL";
    public static final String OLLAMA_API_BASE = "OLLAMA_API_BASE";
    public static final String OLLAMA_MODEL = "OLLAMA_MODEL";
    
    // 服务器配置
    public static final String SERVER_HOST = "SERVER_HOST";
    public static final String SERVER_PORT = "SERVER_PORT";
    
    // 安全配置
    public static final String JWT_SECRET = "JWT_SECRET";
    public static final String JWT_EXPIRATION = "JWT_EXPIRATION";
    
    // 日志配置
    public static final String LOG_LEVEL = "LOG_LEVEL";
    
    static {
        initialize();
    }
    
    /**
     * 初始化配置
     */
    private static synchronized void initialize() {
        if (initialized) return;
        
        // 1. 先加载 .env 文件
        loadEnvFile();
        
        // 2. 系统环境变量会覆盖 .env 文件中的配置
        loadSystemEnv();
        
        // 3. 设置默认值
        setDefaults();
        
        initialized = true;
        System.out.println("[EnvConfig] 环境配置加载完成，共加载 " + config.size() + " 个配置项");
    }
    
    /**
     * 加载 .env 文件
     */
    private static void loadEnvFile() {
        String[] possiblePaths = {
            ".env",
            "../.env",
            System.getProperty("user.dir") + "/.env"
        };
        
        for (String path : possiblePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        
                        // 跳过空行和注释
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        
                        // 解析 KEY=VALUE 格式
                        int equalIndex = line.indexOf('=');
                        if (equalIndex > 0) {
                            String key = line.substring(0, equalIndex).trim();
                            String value = line.substring(equalIndex + 1).trim();
                            
                            // 移除引号
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            
                            config.put(key, value);
                        }
                    }
                    System.out.println("[EnvConfig] 已加载 .env 文件: " + envFile.getAbsolutePath());
                    return;
                } catch (IOException e) {
                    System.err.println("[EnvConfig] 读取 .env 文件失败: " + e.getMessage());
                }
            }
        }
        System.out.println("[EnvConfig] 未找到 .env 文件，将使用系统环境变量和默认值");
    }
    
    /**
     * 加载系统环境变量（会覆盖 .env 文件中的值）
     */
    private static void loadSystemEnv() {
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            // 只加载我们关心的环境变量
            if (isRelevantKey(entry.getKey())) {
                config.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 判断是否是相关的环境变量
     */
    private static boolean isRelevantKey(String key) {
        return key.startsWith("DB_") ||
               key.startsWith("MYSQL_") ||
               key.startsWith("MAIL_") ||
               key.startsWith("AI_") ||
               key.startsWith("OPENAI_") ||
               key.startsWith("GEMINI_") ||
               key.startsWith("CLAUDE_") ||
               key.startsWith("DEEPSEEK_") ||
               key.startsWith("OLLAMA_") ||
               key.startsWith("SERVER_") ||
               key.startsWith("JWT_") ||
               key.startsWith("LOG_") ||
               key.equals("QQ_MAIL_PASSWORD") ||  // 兼容旧配置
               key.equals("JAVA_DB_PASSWORD");    // 兼容旧配置
    }
    
    /**
     * 设置默认值
     */
    private static void setDefaults() {
        setDefault(DB_MODE, "sqlite");
        setDefault(MYSQL_HOST, "localhost");
        setDefault(MYSQL_PORT, "3306");
        setDefault(MYSQL_DATABASE, "paper_sys");
        setDefault(MYSQL_USER, "root");
        
        setDefault(MAIL_ENABLED, "false");
        setDefault(MAIL_HOST, "smtp.qq.com");
        setDefault(MAIL_PORT, "465");
        
        setDefault(AI_PROVIDER, "openai");
        setDefault(OPENAI_API_BASE, "https://api.openai.com/v1");
        setDefault(OPENAI_MODEL, "gpt-3.5-turbo");
        setDefault(DEEPSEEK_API_BASE, "https://api.deepseek.com/v1");
        setDefault(DEEPSEEK_MODEL, "deepseek-chat");
        setDefault(OLLAMA_API_BASE, "http://localhost:11434");
        setDefault(OLLAMA_MODEL, "llama2");
        
        setDefault(SERVER_HOST, "0.0.0.0");
        setDefault(SERVER_PORT, "8080");
        
        setDefault(JWT_EXPIRATION, "86400");
        setDefault(LOG_LEVEL, "INFO");
        
        // 兼容旧的环境变量名
        if (config.containsKey("QQ_MAIL_PASSWORD") && !config.containsKey(MAIL_PASSWORD)) {
            config.put(MAIL_PASSWORD, config.get("QQ_MAIL_PASSWORD"));
        }
        if (config.containsKey("JAVA_DB_PASSWORD") && !config.containsKey(MYSQL_PASSWORD)) {
            config.put(MYSQL_PASSWORD, config.get("JAVA_DB_PASSWORD"));
        }
    }
    
    private static void setDefault(String key, String defaultValue) {
        config.putIfAbsent(key, defaultValue);
    }
    
    // ============ 公共访问方法 ============
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回 null
     */
    public static String get(String key) {
        return config.get(key);
    }
    
    /**
     * 获取配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在返回默认值
     */
    public static String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取整数配置值
     */
    public static int getInt(String key, int defaultValue) {
        String value = config.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = config.get(key);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * 检查配置是否存在且非空
     */
    public static boolean has(String key) {
        String value = config.get(key);
        return value != null && !value.isEmpty();
    }
    
    /**
     * 检查是否配置了有效的 API Key
     */
    public static boolean hasValidApiKey(String key) {
        String value = config.get(key);
        return value != null && 
               !value.isEmpty() && 
               !value.startsWith("your-") && 
               !value.startsWith("sk-your-");
    }
    
    // ============ 便捷方法 ============
    
    /**
     * 是否使用 SQLite 模式
     */
    public static boolean isSQLiteMode() {
        return "sqlite".equalsIgnoreCase(get(DB_MODE, "sqlite"));
    }
    
    /**
     * 是否启用邮件服务
     */
    public static boolean isMailEnabled() {
        return getBoolean(MAIL_ENABLED, false) && hasValidApiKey(MAIL_PASSWORD);
    }
    
    /**
     * 获取当前 AI 提供商
     */
    public static String getAIProvider() {
        return get(AI_PROVIDER, "openai");
    }
    
    /**
     * 获取当前 AI 提供商的 API Key
     */
    public static String getCurrentAIApiKey() {
        String provider = getAIProvider();
        return switch (provider.toLowerCase()) {
            case "openai" -> get(OPENAI_API_KEY);
            case "gemini" -> get(GEMINI_API_KEY);
            case "claude" -> get(CLAUDE_API_KEY);
            case "deepseek" -> get(DEEPSEEK_API_KEY);
            default -> null;
        };
    }
    
    /**
     * 获取当前 AI 提供商的 API Base URL
     */
    public static String getCurrentAIApiBase() {
        String provider = getAIProvider();
        return switch (provider.toLowerCase()) {
            case "openai" -> get(OPENAI_API_BASE, "https://api.openai.com/v1");
            case "deepseek" -> get(DEEPSEEK_API_BASE, "https://api.deepseek.com/v1");
            case "ollama" -> get(OLLAMA_API_BASE, "http://localhost:11434");
            default -> null;
        };
    }
    
    /**
     * 获取当前 AI 提供商的模型名称
     */
    public static String getCurrentAIModel() {
        String provider = getAIProvider();
        return switch (provider.toLowerCase()) {
            case "openai" -> get(OPENAI_MODEL, "gpt-3.5-turbo");
            case "gemini" -> get(GEMINI_MODEL, "gemini-pro");
            case "claude" -> get(CLAUDE_MODEL, "claude-3-sonnet-20240229");
            case "deepseek" -> get(DEEPSEEK_MODEL, "deepseek-chat");
            case "ollama" -> get(OLLAMA_MODEL, "llama2");
            default -> null;
        };
    }
    
    /**
     * 获取 MySQL 连接 URL
     */
    public static String getMySQLUrl() {
        String host = get(MYSQL_HOST, "localhost");
        String port = get(MYSQL_PORT, "3306");
        String database = get(MYSQL_DATABASE, "paper_sys");
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                             host, port, database);
    }
    
    /**
     * 打印当前配置（隐藏敏感信息）
     */
    public static void printConfig() {
        System.out.println("\n========== 当前环境配置 ==========");
        System.out.println("数据库模式: " + get(DB_MODE));
        if (!isSQLiteMode()) {
            System.out.println("MySQL 主机: " + get(MYSQL_HOST) + ":" + get(MYSQL_PORT));
            System.out.println("MySQL 数据库: " + get(MYSQL_DATABASE));
        }
        System.out.println("邮件服务: " + (isMailEnabled() ? "已启用" : "已禁用"));
        System.out.println("AI 提供商: " + getAIProvider());
        System.out.println("AI API Key: " + (hasValidApiKey(getCurrentAIApiKey() != null ? 
                          getAIProvider().toUpperCase() + "_API_KEY" : "") ? "已配置" : "未配置"));
        System.out.println("服务器: " + get(SERVER_HOST) + ":" + get(SERVER_PORT));
        System.out.println("==================================\n");
    }
}
