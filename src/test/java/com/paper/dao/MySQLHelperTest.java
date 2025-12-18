package com.paper.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.paper.utils.DatabaseInitializer;

/**
 * MySQLHelper 单元测试类
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MySQLHelperTest {
    
    private MySQLHelper helper;
    
    @BeforeAll
    void setupDatabase() {
        DatabaseInitializer.initialize();
    }
    
    @BeforeEach
    void setUp() throws ClassNotFoundException, SQLException {
        helper = new MySQLHelper();
    }
    
    @Test
    @DisplayName("创建MySQLHelper实例不应抛出异常")
    void testCreateInstance() {
        assertDoesNotThrow(() -> {
            new MySQLHelper();
        });
    }
    
    @Test
    @DisplayName("执行SELECT查询应返回结果")
    void testExecuteSQLWithSelect() throws ClassNotFoundException, SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM USER";
        Map<String, Object> result = helper.executeSQLWithSelect(sql);
        
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.containsKey("result"), "结果应包含result键");
    }
    
    @Test
    @DisplayName("执行带参数的SELECT查询")
    void testExecuteSQLWithSelectAndParams() throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM USER WHERE uname = ?";
        Map<String, Object> result = helper.executeSQLWithSelect(sql, "nonexistent_user_12345");
        
        assertNotNull(result);
        // 查询不存在的用户，结果集应为空
    }
    
    @Test
    @DisplayName("执行INSERT语句")
    void testExecuteSQL() throws ClassNotFoundException, SQLException {
        String uniqueName = "test_" + System.currentTimeMillis();
        String sql = "INSERT INTO USER (uname, password, email) VALUES (?, ?, ?)";
        
        String result = helper.executeSQL(sql, uniqueName, "hashedpassword", "test@test.com");
        
        // 空字符串表示成功
        assertEquals("", result, "INSERT应该成功");
    }
    
    @Test
    @DisplayName("关闭连接不应抛出异常")
    void testClose() {
        assertDoesNotThrow(() -> {
            helper.close();
        });
    }
}
