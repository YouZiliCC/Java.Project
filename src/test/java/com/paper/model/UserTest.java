package com.paper.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User 模型类单元测试
 */
public class UserTest {
    
    @Test
    @DisplayName("User对象创建和getter/setter测试")
    void testUserGetterSetter() {
        User user = new User();
        
        user.setUname("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertEquals("testuser", user.getUname());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
    }
    
    @Test
    @DisplayName("User对象可以设置null值")
    void testUserNullValues() {
        User user = new User();
        
        user.setUname(null);
        user.setPassword(null);
        user.setEmail(null);
        
        assertNull(user.getUname());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
    }
    
    @Test
    @DisplayName("User对象可以设置空字符串")
    void testUserEmptyValues() {
        User user = new User();
        
        user.setUname("");
        user.setPassword("");
        user.setEmail("");
        
        assertEquals("", user.getUname());
        assertEquals("", user.getPassword());
        assertEquals("", user.getEmail());
    }
}
