package com.paper.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化工具类
 * 自动创建必要的表结构
 */
public class DatabaseInitializer {
    
    /**
     * 初始化数据库（创建表）
     */
    public static void initialize() {
        System.out.println("======================================");
        System.out.println("[DB Init] Starting database initialization...");
        System.out.println("[DB Init] Mode: " + (DatabaseConfig.isSQLiteMode() ? "SQLite (dev)" : "MySQL (prod)"));
        System.out.println("======================================");
        
        try {
            Class.forName(DatabaseConfig.getDriverClassName());
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // 创建用户表
                createUserTable(stmt);
                
                // 创建论文表
                createPaperTable(stmt);
                
                // 创建作者表
                createAuthorTable(stmt);
                
                // 创建关键词表
                createKeywordTable(stmt);
                
                // 创建分析记录表
                createAnalysisRecordTable(stmt);
                
                System.out.println("[DB Init] Database initialization completed!");
                
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver load failed: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
    
    private static Connection getConnection() throws SQLException {
        if (DatabaseConfig.isSQLiteMode()) {
            return DriverManager.getConnection(DatabaseConfig.getUrl());
        } else {
            return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
            );
        }
    }
    
    /**
     * 创建用户表
     */
    private static void createUserTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uname VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    uname VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("  [OK] Table 'users' created");
    }
    
    /**
     * 创建论文表
     */
    private static void createPaperTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS papers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    wos_id VARCHAR(50) UNIQUE,
                    title TEXT NOT NULL,
                    abstract_text TEXT,
                    publish_date DATE,
                    journal VARCHAR(255),
                    volume INT,
                    issue INT,
                    pages INT,
                    doi VARCHAR(100),
                    country VARCHAR(100),
                    author TEXT,
                    target TEXT,
                    conference VARCHAR(255),
                    citations INT DEFAULT 0,
                    refs INT DEFAULT 0,
                    keywords TEXT
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS papers (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    wos_id VARCHAR(50) UNIQUE,
                    title TEXT NOT NULL,
                    abstract_text TEXT,
                    publish_date DATE,
                    journal VARCHAR(255),
                    volume INT,
                    issue INT,
                    pages INT,
                    doi VARCHAR(100),
                    country VARCHAR(100),
                    author TEXT,
                    target TEXT,
                    conference VARCHAR(255),
                    citations INT DEFAULT 0,
                    refs INT DEFAULT 0,
                    keywords TEXT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("  [OK] Table 'papers' created");
    }
    
    /**
     * 创建作者表
     */
    private static void createAuthorTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS authors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    affiliation VARCHAR(255),
                    email VARCHAR(100)
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS authors (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    affiliation VARCHAR(255),
                    email VARCHAR(100)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("  [OK] Table 'authors' created");
    }
    
    /**
     * 创建关键词表
     */
    private static void createKeywordTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS keywords (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    keyword VARCHAR(100) NOT NULL UNIQUE
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS keywords (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    keyword VARCHAR(100) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("  [OK] Table 'keywords' created");
    }
    
    /**
     * 创建分析记录表
     */
    private static void createAnalysisRecordTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS analysis_record (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(50) NOT NULL,
                    filename VARCHAR(100) NOT NULL UNIQUE,
                    original_name VARCHAR(255),
                    file_size BIGINT,
                    analysis_result TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS analysis_record (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL,
                    filename VARCHAR(100) NOT NULL UNIQUE,
                    original_name VARCHAR(255),
                    file_size BIGINT,
                    analysis_result TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_analysis_username (username),
                    INDEX idx_analysis_created (created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        
        // 为 SQLite 创建索引
        if (DatabaseConfig.isSQLiteMode()) {
            try {
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_analysis_username ON analysis_record(username)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_analysis_created ON analysis_record(created_at)");
            } catch (SQLException e) {
                // 索引可能已存在，忽略
            }
        }
        System.out.println("  [OK] Table 'analysis_record' created");
    }
}
