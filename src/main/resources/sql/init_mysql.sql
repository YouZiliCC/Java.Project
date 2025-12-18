-- ============================================
-- 学术论文管理系统 - MySQL 数据库初始化脚本
-- ============================================
-- 使用方法: mysql -u root -p < init_mysql.sql
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS paper_sys 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE paper_sys;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS USER (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    uname VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) COMMENT '邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_uname (uname),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 论文表
-- ============================================
CREATE TABLE IF NOT EXISTS PAPER (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '论文ID',
    wos_id VARCHAR(50) UNIQUE COMMENT 'Web of Science ID',
    title TEXT NOT NULL COMMENT '论文标题',
    abstract_text TEXT COMMENT '摘要',
    publish_date DATE COMMENT '发表日期',
    journal VARCHAR(255) COMMENT '期刊名称',
    volume INT COMMENT '卷号',
    issue INT COMMENT '期号',
    pages INT COMMENT '页数',
    doi VARCHAR(100) COMMENT 'DOI',
    country VARCHAR(100) COMMENT '国家/地区',
    author TEXT COMMENT '作者列表',
    target TEXT COMMENT '研究目标/领域',
    conference VARCHAR(255) COMMENT '会议名称',
    citations INT DEFAULT 0 COMMENT '被引次数',
    refs INT DEFAULT 0 COMMENT '参考文献数',
    keywords TEXT COMMENT '关键词(分号分隔)',
    INDEX idx_wos_id (wos_id),
    INDEX idx_title (title(100)),
    INDEX idx_journal (journal),
    INDEX idx_publish_date (publish_date),
    FULLTEXT INDEX ft_title_abstract (title, abstract_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文表';

-- ============================================
-- 作者表
-- ============================================
CREATE TABLE IF NOT EXISTS AUTHOR (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '作者ID',
    name VARCHAR(100) NOT NULL COMMENT '作者姓名',
    affiliation VARCHAR(255) COMMENT '所属机构',
    email VARCHAR(100) COMMENT '邮箱',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作者表';

-- ============================================
-- 关键词表
-- ============================================
CREATE TABLE IF NOT EXISTS KEYWORD (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '关键词ID',
    keyword VARCHAR(100) NOT NULL UNIQUE COMMENT '关键词',
    INDEX idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词表';

-- ============================================
-- 论文-作者关联表
-- ============================================
CREATE TABLE IF NOT EXISTS PAPER_AUTHOR (
    id INT PRIMARY KEY AUTO_INCREMENT,
    paper_id INT NOT NULL COMMENT '论文ID',
    author_id INT NOT NULL COMMENT '作者ID',
    author_order INT DEFAULT 1 COMMENT '作者排序',
    FOREIGN KEY (paper_id) REFERENCES PAPER(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES AUTHOR(id) ON DELETE CASCADE,
    UNIQUE KEY uk_paper_author (paper_id, author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文-作者关联表';

-- ============================================
-- 论文-关键词关联表
-- ============================================
CREATE TABLE IF NOT EXISTS PAPER_KEYWORD (
    id INT PRIMARY KEY AUTO_INCREMENT,
    paper_id INT NOT NULL COMMENT '论文ID',
    keyword_id INT NOT NULL COMMENT '关键词ID',
    FOREIGN KEY (paper_id) REFERENCES PAPER(id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES KEYWORD(id) ON DELETE CASCADE,
    UNIQUE KEY uk_paper_keyword (paper_id, keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文-关键词关联表';

-- ============================================
-- 插入测试数据
-- ============================================

-- TODO

-- 完成提示
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('用户数: ', COUNT(*)) AS user_count FROM USER;
SELECT CONCAT('论文数: ', COUNT(*)) AS paper_count FROM PAPER;
