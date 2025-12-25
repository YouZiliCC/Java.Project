package com.paper.utils;

import java.io.File;

/**
 * 数据库配置类
 * 使用 SQLite 作为唯一数据库
 * 数据库文件存储在 data/paper_system.db
 */
public class DatabaseConfig {
    
    // SQLite 配置
    public static final String SQLITE_DB_FILE = "data/paper_system.db";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    
    /**
     * 确保数据目录存在
     */
    static {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
    
    /**
     * 始终使用 SQLite 模式
     */
    public static boolean isSQLiteMode() {
        return true;
    }
    
    /**
     * 获取数据库驱动类名
     */
    public static String getDriverClassName() {
        return SQLITE_DRIVER;
    }
    
    /**
     * 获取数据库连接 URL
     */
    public static String getUrl() {
        return "jdbc:sqlite:" + SQLITE_DB_FILE;
    }
    
    /**
     * 获取数据库用户名（SQLite 不需要）
     */
    public static String getUsername() {
        return null;
    }
    
    /**
     * 获取数据库密码（SQLite 不需要）
     */
    public static String getPassword() {
        return null;
    }
}
