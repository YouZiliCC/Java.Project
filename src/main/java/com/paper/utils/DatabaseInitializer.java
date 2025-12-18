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
        System.out.println("开始初始化数据库...");
        System.out.println("数据库模式: " + (DatabaseConfig.isSQLiteMode() ? "SQLite (测试)" : "MySQL (生产)"));
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
                
                System.out.println("数据库初始化完成！");
                
            }
        } catch (ClassNotFoundException e) {
            System.err.println("数据库驱动加载失败: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
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
                CREATE TABLE IF NOT EXISTS USER (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uname VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS USER (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    uname VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("✓ 用户表 (USER) 创建成功");
    }
    
    /**
     * 创建论文表
     */
    private static void createPaperTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS PAPER (
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
                CREATE TABLE IF NOT EXISTS PAPER (
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
        System.out.println("✓ 论文表 (PAPER) 创建成功");
    }
    
    /**
     * 创建作者表
     */
    private static void createAuthorTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS AUTHOR (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    affiliation VARCHAR(255),
                    email VARCHAR(100)
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS AUTHOR (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    affiliation VARCHAR(255),
                    email VARCHAR(100)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("✓ 作者表 (AUTHOR) 创建成功");
    }
    
    /**
     * 创建关键词表
     */
    private static void createKeywordTable(Statement stmt) throws SQLException {
        String sql;
        if (DatabaseConfig.isSQLiteMode()) {
            sql = """
                CREATE TABLE IF NOT EXISTS KEYWORD (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    keyword VARCHAR(100) NOT NULL UNIQUE
                )
            """;
        } else {
            sql = """
                CREATE TABLE IF NOT EXISTS KEYWORD (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    keyword VARCHAR(100) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        }
        stmt.executeUpdate(sql);
        System.out.println("✓ 关键词表 (KEYWORD) 创建成功");
    }
    
    /**
     * 插入测试数据
     */
    // public static void insertTestData() {
    //     System.out.println("插入测试数据...");
        
    //     try {
    //         Class.forName(DatabaseConfig.getDriverClassName());
            
    //         try (Connection conn = getConnection();
    //              Statement stmt = conn.createStatement()) {
                
    //             // 插入测试论文数据
    //             String insertPapers = """
    //                 INSERT OR IGNORE INTO PAPER (wos_id, title, abstract_text, publish_date, journal, author, citations, keywords, target)
    //                 VALUES 
    //                 ('WOS:001', 'Deep Learning for Natural Language Processing', 
    //                  'This paper presents a comprehensive survey of deep learning techniques for NLP tasks including sentiment analysis, machine translation, and question answering.',
    //                  '2024-01-15', 'IEEE Transactions on Neural Networks', 'Zhang Wei; Li Ming; Wang Fang', 156, 'deep learning;NLP;neural networks;transformer', 'deep learning'),
    //                 ('WOS:002', 'Machine Learning in Healthcare: A Review',
    //                  'We review the application of machine learning algorithms in healthcare, covering disease diagnosis, drug discovery, and personalized medicine.',
    //                  '2024-02-20', 'Nature Medicine', 'Chen Xiao; Liu Yang', 89, 'machine learning;healthcare;diagnosis;AI', 'machine learning'),
    //                 ('WOS:003', 'Quantum Computing: Current State and Future Directions',
    //                  'An overview of quantum computing technologies, including quantum algorithms, error correction, and potential applications in cryptography.',
    //                  '2024-03-10', 'Science', 'Wang Lei; Zhao Min; Sun Tao', 234, 'quantum computing;algorithms;cryptography', 'quantum'),
    //                 ('WOS:004', 'Sustainable Energy Systems: A Comprehensive Analysis',
    //                  'This study analyzes various sustainable energy systems including solar, wind, and hydrogen fuel cells for future energy infrastructure.',
    //                  '2024-04-05', 'Energy & Environmental Science', 'Li Hua; Zhang Yong', 67, 'renewable energy;solar;wind;sustainability', 'energy'),
    //                 ('WOS:005', 'Advances in Computer Vision with Convolutional Neural Networks',
    //                  'We present recent advances in computer vision using CNNs, including object detection, image segmentation, and visual recognition tasks.',
    //                  '2024-05-12', 'Computer Vision and Image Understanding', 'Wu Jian; Huang Wei; Xu Li', 198, 'computer vision;CNN;object detection;image processing', 'computer vision')
    //             """;
                
    //             // SQLite 使用 INSERT OR IGNORE，MySQL 使用 INSERT IGNORE
    //             if (!DatabaseConfig.isSQLiteMode()) {
    //                 insertPapers = insertPapers.replace("INSERT OR IGNORE", "INSERT IGNORE");
    //             }
                
    //             stmt.executeUpdate(insertPapers);
    //             System.out.println("✓ 测试论文数据插入成功");
                
    //         }
    //     } catch (Exception e) {
    //         System.err.println("插入测试数据失败: " + e.getMessage());
    //     }
    // }
}
