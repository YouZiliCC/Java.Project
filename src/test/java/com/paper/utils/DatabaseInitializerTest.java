package com.paper.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

/**
 * DatabaseInitializer 单元测试类
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseInitializerTest {
    
    @Test
    @Order(1)
    @DisplayName("数据库初始化不应抛出异常")
    void testInitializeNoException() {
        assertDoesNotThrow(() -> {
            DatabaseInitializer.initialize();
        }, "数据库初始化不应抛出异常");
    }
    
    @Test
    @Order(2)
    @DisplayName("重复初始化应该安全（幂等性）")
    void testInitializeIdempotent() {
        // 多次初始化不应该出错
        assertDoesNotThrow(() -> {
            DatabaseInitializer.initialize();
            DatabaseInitializer.initialize();
            DatabaseInitializer.initialize();
        }, "重复初始化不应抛出异常");
    }
    
    // 注意：insertTestData 方法已被注释，如需测试请先取消注释
}
