package com.paper.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DatabaseConfig 单元测试类
 */
public class DatabaseConfigTest {
    
    @Test
    @DisplayName("默认使用SQLite模式")
    void testDefaultSQLiteMode() {
        // 默认应该是SQLite模式
        assertTrue(DatabaseConfig.isSQLiteMode(), "默认应该使用SQLite模式");
    }
    
    @Test
    @DisplayName("SQLite驱动类名正确")
    void testSQLiteDriverClassName() {
        if (DatabaseConfig.isSQLiteMode()) {
            assertEquals("org.sqlite.JDBC", DatabaseConfig.getDriverClassName());
        }
    }
    
    @Test
    @DisplayName("SQLite URL格式正确")
    void testSQLiteUrl() {
        if (DatabaseConfig.isSQLiteMode()) {
            String url = DatabaseConfig.getUrl();
            assertTrue(url.startsWith("jdbc:sqlite:"), "SQLite URL应该以jdbc:sqlite:开头");
        }
    }
    
    @Test
    @DisplayName("SQLite模式下用户名为null")
    void testSQLiteUsername() {
        if (DatabaseConfig.isSQLiteMode()) {
            assertNull(DatabaseConfig.getUsername(), "SQLite模式下用户名应该为null");
        }
    }
    
    @Test
    @DisplayName("SQLite模式下密码为null")
    void testSQLitePassword() {
        if (DatabaseConfig.isSQLiteMode()) {
            assertNull(DatabaseConfig.getPassword(), "SQLite模式下密码应该为null");
        }
    }
    
    @Test
    @DisplayName("配置常量不为null")
    void testConfigConstants() {
        assertNotNull(DatabaseConfig.SQLITE_URL);
        assertNotNull(DatabaseConfig.SQLITE_DRIVER);
        assertNotNull(DatabaseConfig.MYSQL_URL);
        assertNotNull(DatabaseConfig.MYSQL_DRIVER);
        assertNotNull(DatabaseConfig.MYSQL_USER);
    }
}
