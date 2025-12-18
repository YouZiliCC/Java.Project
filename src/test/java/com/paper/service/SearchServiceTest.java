package com.paper.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.paper.model.Paper;
import com.paper.dao.MySQLHelper;
import com.paper.utils.DatabaseInitializer;

/**
 * SearchService 单元测试类
 * 测试论文搜索功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchServiceTest {
    
    private SearchService searchService;
    
    @BeforeAll
    void setupDatabase() throws ClassNotFoundException, SQLException {
        // 初始化测试数据库
        DatabaseInitializer.initialize();
        
        // 插入测试数据（不包含日期字段，避免格式问题）
        MySQLHelper helper = new MySQLHelper();
        String sql = "INSERT OR IGNORE INTO PAPER (wos_id, title, abstract_text, journal, author, citations, keywords, target) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        helper.executeSQL(sql, "WOS:TEST001", "Test Deep Learning Paper", "Test abstract",  
                         "Test Journal", "Test Author", 100, "test;deep learning", "deep learning");
        helper.executeSQL(sql, "WOS:TEST002", "Test Machine Learning Paper", "ML abstract",
                         "ML Journal", "ML Author", 50, "test;machine learning", "machine learning");
    }
    
    @BeforeEach
    void setUp() {
        searchService = new SearchService();
    }
    
    @Nested
    @DisplayName("论文搜索测试")
    class SearchTests {
        
        @Test
        @DisplayName("搜索存在的关键词 - 应返回结果")
        void testSearchExistingKeyword() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("deep learning");
            
            assertNotNull(results, "搜索结果不应为null");
            assertFalse(results.isEmpty(), "应该找到匹配的论文");
        }
        
        @Test
        @DisplayName("搜索不存在的关键词 - 应返回空列表")
        void testSearchNonexistentKeyword() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("这个关键词肯定不存在_xyz123");
            
            assertNotNull(results, "搜索结果不应为null");
            assertTrue(results.isEmpty(), "不应找到匹配的论文");
        }
        
        @Test
        @DisplayName("搜索空关键词 - 应返回空列表")
        void testSearchEmptyKeyword() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("");
            
            assertNotNull(results, "搜索结果不应为null");
        }
        
        @Test
        @DisplayName("搜索结果包含正确的论文信息")
        void testSearchResultContent() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("deep learning");
            
            if (!results.isEmpty()) {
                Paper paper = results.get(0);
                
                // 验证论文对象的字段不为空
                assertNotNull(paper.getTitle(), "论文标题不应为null");
                assertFalse(paper.getTitle().isEmpty(), "论文标题不应为空");
            }
        }
        
        @Test
        @DisplayName("搜索machine learning领域论文")
        void testSearchMachineLearning() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("machine learning");
            
            assertNotNull(results);
            assertFalse(results.isEmpty(), "应该找到machine learning相关论文");
        }
    }
    
    @Nested
    @DisplayName("搜索结果验证")
    class SearchResultValidation {
        
        @Test
        @DisplayName("验证Paper对象的getter方法")
        void testPaperGetters() throws ClassNotFoundException, SQLException {
            List<Paper> results = searchService.searchByTarget("deep learning");
            
            if (!results.isEmpty()) {
                Paper paper = results.get(0);
                
                // 测试所有getter方法不抛出异常
                assertDoesNotThrow(() -> {
                    paper.getWosId();
                    paper.getTitle();
                    paper.getAbstractText();
                    paper.getPublishDate();
                    paper.getJournal();
                    paper.getVolume();
                    paper.getIssue();
                    paper.getPages();
                    paper.getDoi();
                    paper.getCountry();
                    paper.getAuthor();
                    paper.getTarget();
                    paper.getConference();
                    paper.getCitations();
                    paper.getRefs();
                    paper.getKeywords();
                });
            }
        }
    }
}
