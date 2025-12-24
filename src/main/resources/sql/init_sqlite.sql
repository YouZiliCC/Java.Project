-- ============================================
-- 学术论文管理系统 - SQLite 数据库初始化脚本
-- ============================================
-- 使用方法: sqlite3 paper_system.db < init_sqlite.sql
-- ============================================

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS USER (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uname VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_uname ON USER(uname);
CREATE INDEX IF NOT EXISTS idx_user_email ON USER(email);

-- ============================================
-- 论文表
-- ============================================
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
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_paper_wos_id ON PAPER(wos_id);
CREATE INDEX IF NOT EXISTS idx_paper_journal ON PAPER(journal);
CREATE INDEX IF NOT EXISTS idx_paper_publish_date ON PAPER(publish_date);

-- ============================================
-- 作者表
-- ============================================
CREATE TABLE IF NOT EXISTS AUTHOR (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    affiliation VARCHAR(255),
    email VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_author_name ON AUTHOR(name);

-- ============================================
-- 关键词表
-- ============================================
CREATE TABLE IF NOT EXISTS KEYWORD (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    keyword VARCHAR(100) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_keyword ON KEYWORD(keyword);

-- ============================================
-- 论文-作者关联表
-- ============================================
CREATE TABLE IF NOT EXISTS PAPER_AUTHOR (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paper_id INT NOT NULL,
    author_id INT NOT NULL,
    author_order INT DEFAULT 1,
    FOREIGN KEY (paper_id) REFERENCES PAPER(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES AUTHOR(id) ON DELETE CASCADE,
    UNIQUE (paper_id, author_id)
);

-- ============================================
-- 论文-关键词关联表
-- ============================================
CREATE TABLE IF NOT EXISTS PAPER_KEYWORD (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paper_id INT NOT NULL,
    keyword_id INT NOT NULL,
    FOREIGN KEY (paper_id) REFERENCES PAPER(id) ON DELETE CASCADE,
    FOREIGN KEY (keyword_id) REFERENCES KEYWORD(id) ON DELETE CASCADE,
    UNIQUE (paper_id, keyword_id)
);

-- ============================================
-- 分析记录表
-- ============================================
CREATE TABLE IF NOT EXISTS ANALYSIS_RECORD (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL,
    filename VARCHAR(100) NOT NULL UNIQUE,
    original_name VARCHAR(255),
    file_size BIGINT,
    analysis_result TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES USER(uname) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_analysis_username ON ANALYSIS_RECORD(username);
CREATE INDEX IF NOT EXISTS idx_analysis_created ON ANALYSIS_RECORD(created_at);

-- ============================================
-- 插入测试数据
-- ============================================

-- TODO

-- 验证数据
SELECT '数据库初始化完成！' AS message;
SELECT 'USER 表记录数: ' || COUNT(*) FROM USER;
SELECT 'PAPER 表记录数: ' || COUNT(*) FROM PAPER;
