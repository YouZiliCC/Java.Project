package com.paper.utils;

import com.paper.config.EnvConfig;

/**
 * 数据库配置类
 * 支持 SQLite（测试）和 MySQL（生产）两种模式
 * 
 * 配置通过 EnvConfig 集中管理，可在 .env 文件或环境变量中设置
 */
public class DatabaseConfig {
    
    // SQLite 配置（测试用，数据存储在项目根目录）
    public static final String SQLITE_URL = "jdbc:sqlite:paper_system.db";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    
    // MySQL 驱动
    public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    /**
     * 判断是否使用 SQLite 模式
     */
    public static boolean isSQLiteMode() {
        return EnvConfig.isSQLiteMode();
    }
    
    /**
     * 获取数据库驱动类名
     */
    public static String getDriverClassName() {
        return isSQLiteMode() ? SQLITE_DRIVER : MYSQL_DRIVER;
    }
    
    /**
     * 获取数据库连接 URL
     */
    public static String getUrl() {
        return isSQLiteMode() ? SQLITE_URL : EnvConfig.getMySQLUrl();
    }
    
    /**
     * 获取数据库用户名（SQLite 不需要）
     */
    public static String getUsername() {
        return isSQLiteMode() ? null : EnvConfig.get(EnvConfig.MYSQL_USER, "root");
    }
    
    /**
     * 获取数据库密码（SQLite 不需要）
     */
    public static String getPassword() {
        return isSQLiteMode() ? null : EnvConfig.get(EnvConfig.MYSQL_PASSWORD);
    }
}
