package com.paper.utils;

/**
 * 数据库配置类
 * 支持 SQLite（测试）和 MySQL（生产）两种模式
 */
public class DatabaseConfig {
    
    // 数据库模式：sqlite 或 mysql
    private static final String DB_MODE = System.getProperty("db.mode", "sqlite");
    
    // SQLite 配置（测试用，数据存储在项目根目录）
    public static final String SQLITE_URL = "jdbc:sqlite:paper_system.db";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
    
    // TODO: .env 文件中配置数据库连接信息
    // MySQL 配置（生产用）
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/paper_sys?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWORD = System.getenv("JAVA_DB_PASSWORD") != null 
            ? System.getenv("JAVA_DB_PASSWORD") : "123456";
    
    /**
     * 判断是否使用 SQLite 模式
     */
    public static boolean isSQLiteMode() {
        return "sqlite".equalsIgnoreCase(DB_MODE);
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
        return isSQLiteMode() ? SQLITE_URL : MYSQL_URL;
    }
    
    /**
     * 获取数据库用户名（SQLite 不需要）
     */
    public static String getUsername() {
        return isSQLiteMode() ? null : MYSQL_USER;
    }
    
    /**
     * 获取数据库密码（SQLite 不需要）
     */
    public static String getPassword() {
        return isSQLiteMode() ? null : MYSQL_PASSWORD;
    }
}
