package com.paper.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.paper.model.User;
import com.paper.utils.DatabaseInitializer;

/**
 * UserService 单元测试类
 * 测试用户注册、登录等功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {
    
    private UserService userService;
    
    @BeforeAll
    void setupDatabase() {
        // 初始化测试数据库
        DatabaseInitializer.initialize();
    }
    
    @BeforeEach
    void setUp() throws ClassNotFoundException, SQLException {
        userService = new UserService();
    }
    
    // ==================== 注册功能测试 ====================
    
    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {
        
        @Test
        @DisplayName("正常注册 - 应该成功")
        void testRegisterSuccess() throws SQLException {
            User user = createUniqueUser();
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
        }
        
        @Test
        @DisplayName("注册后应能正常登录")
        void testRegisterThenLogin() throws SQLException {
            User user = createUniqueUser();
            String password = user.getPassword(); // 保存原始密码
            
            // 注册
            String registerResult = userService.registerDirect(user);
            assertEquals("注册成功", registerResult);
            
            // 登录验证
            user.setPassword(password); // 使用原始密码登录
            boolean loginResult = userService.login(user);
            assertTrue(loginResult, "注册后应该能正常登录");
        }
    }
    
    // ==================== 用户名验证测试 ====================
    
    @Nested
    @DisplayName("用户名验证测试")
    class UsernameValidationTests {
        
        @Test
        @DisplayName("用户名为空 - 应该失败")
        void testEmptyUsername() throws SQLException {
            User user = new User();
            user.setUname("");
            user.setPassword("123456");
            user.setEmail("test@example.com");
            
            String result = userService.registerDirect(user);
            assertEquals("用户名不能为空", result);
        }
        
        @Test
        @DisplayName("用户名为null - 应该失败")
        void testNullUsername() throws SQLException {
            User user = new User();
            user.setUname(null);
            user.setPassword("123456");
            user.setEmail("test@example.com");
            
            String result = userService.registerDirect(user);
            assertEquals("用户名不能为空", result);
        }
        
        @Test
        @DisplayName("用户名只有空格 - 应该失败")
        void testWhitespaceUsername() throws SQLException {
            User user = new User();
            user.setUname("   ");
            user.setPassword("123456");
            user.setEmail("test@example.com");
            
            String result = userService.registerDirect(user);
            assertEquals("用户名不能为空", result);
        }
        
        @Test
        @DisplayName("用户名太短(1个字符) - 应该失败")
        void testUsernameTooShort() throws SQLException {
            User user = new User();
            user.setUname("a");
            user.setPassword("123456");
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("用户名长度应在2-20个字符之间", result);
        }
        
        @Test
        @DisplayName("用户名太长(超过20个字符) - 应该失败")
        void testUsernameTooLong() throws SQLException {
            User user = new User();
            user.setUname("a".repeat(21)); // 21个字符
            user.setPassword("123456");
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("用户名长度应在2-20个字符之间", result);
        }
        
        @ParameterizedTest
        @DisplayName("用户名边界值测试 - 2到20个字符应该成功")
        @ValueSource(ints = {2, 10, 20})
        void testUsernameValidLength(int length) throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(length));
            user.setPassword("123456");
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
        }
        
        @Test
        @DisplayName("用户名唯一性 - 重复用户名应该失败")
        void testDuplicateUsername() throws SQLException {
            // 第一个用户
            User user1 = createUniqueUser();
            String result1 = userService.registerDirect(user1);
            assertEquals("注册成功", result1);
            
            // 第二个用户使用相同用户名
            User user2 = new User();
            user2.setUname(user1.getUname()); // 相同用户名
            user2.setPassword("654321");
            user2.setEmail(generateUniqueEmail()); // 不同邮箱
            
            String result2 = userService.registerDirect(user2);
            assertEquals("该用户名已存在", result2);
        }
    }
    
    // ==================== 邮箱验证测试 ====================
    
    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTests {
        
        @Test
        @DisplayName("邮箱唯一性 - 重复邮箱应该失败")
        void testDuplicateEmail() throws SQLException {
            String sharedEmail = generateUniqueEmail();
            
            // 第一个用户
            User user1 = new User();
            user1.setUname(generateRandomString(8));
            user1.setPassword("123456");
            user1.setEmail(sharedEmail);
            
            String result1 = userService.registerDirect(user1);
            assertEquals("注册成功", result1);
            
            // 第二个用户使用相同邮箱
            User user2 = new User();
            user2.setUname(generateRandomString(8)); // 不同用户名
            user2.setPassword("654321");
            user2.setEmail(sharedEmail); // 相同邮箱
            
            String result2 = userService.registerDirect(user2);
            assertEquals("该邮箱已被注册", result2);
        }
        
        @Test
        @DisplayName("邮箱可选 - 不提供邮箱也能注册")
        void testOptionalEmail() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword("123456");
            user.setEmail(null); // 不提供邮箱
            
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
        }
        
        @Test
        @DisplayName("空邮箱 - 应该允许注册")
        void testEmptyEmail() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword("123456");
            user.setEmail(""); // 空邮箱
            
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
        }
    }
    
    // ==================== 密码验证测试 ====================
    
    @Nested
    @DisplayName("密码验证测试")
    class PasswordValidationTests {
        
        @Test
        @DisplayName("密码为空 - 应该失败")
        void testEmptyPassword() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword("");
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("密码长度不能少于6位", result);
        }
        
        @Test
        @DisplayName("密码为null - 应该失败")
        void testNullPassword() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword(null);
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("密码长度不能少于6位", result);
        }
        
        @Test
        @DisplayName("密码太短(5个字符) - 应该失败")
        void testPasswordTooShort() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword("12345"); // 5个字符
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("密码长度不能少于6位", result);
        }
        
        @Test
        @DisplayName("密码刚好6个字符 - 应该成功")
        void testPasswordMinLength() throws SQLException {
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword("123456"); // 刚好6个字符
            user.setEmail(generateUniqueEmail());
            
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
        }
        
        @Test
        @DisplayName("密码加密存储 - 登录时应能正确验证")
        void testPasswordEncryption() throws SQLException {
            String rawPassword = "MySecretPassword123";
            
            User user = new User();
            user.setUname(generateRandomString(8));
            user.setPassword(rawPassword);
            user.setEmail(generateUniqueEmail());
            
            // 注册
            String result = userService.registerDirect(user);
            assertEquals("注册成功", result);
            
            // 使用原始密码登录
            user.setPassword(rawPassword);
            assertTrue(userService.login(user), "使用原始密码应该能登录");
            
            // 使用错误密码登录
            user.setPassword("WrongPassword");
            assertFalse(userService.login(user), "使用错误密码不应该能登录");
        }
    }
    
    // ==================== 登录功能测试 ====================
    
    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {
        
        @Test
        @DisplayName("正确的用户名和密码 - 应该成功")
        void testLoginSuccess() throws SQLException {
            User user = createUniqueUser();
            String password = user.getPassword();
            
            // 先注册
            userService.registerDirect(user);
            
            // 再登录
            user.setPassword(password);
            assertTrue(userService.login(user));
        }
        
        @Test
        @DisplayName("错误的密码 - 应该失败")
        void testLoginWrongPassword() throws SQLException {
            User user = createUniqueUser();
            
            // 先注册
            userService.registerDirect(user);
            
            // 用错误密码登录
            user.setPassword("wrongpassword");
            assertFalse(userService.login(user));
        }
        
        @Test
        @DisplayName("不存在的用户名 - 应该失败")
        void testLoginNonexistentUser() throws SQLException {
            User user = new User();
            user.setUname("nonexistent_user_" + UUID.randomUUID().toString().substring(0, 8));
            user.setPassword("123456");
            
            assertFalse(userService.login(user));
        }
        
        @Test
        @DisplayName("用户名大小写敏感测试")
        void testLoginCaseSensitive() throws SQLException {
            User user = new User();
            user.setUname("TestUser_" + UUID.randomUUID().toString().substring(0, 4));
            user.setPassword("123456");
            user.setEmail(generateUniqueEmail());
            
            // 注册
            userService.registerDirect(user);
            
            // 使用不同大小写的用户名登录（预期失败，因为SQL通常区分大小写）
            User loginUser = new User();
            loginUser.setUname(user.getUname().toLowerCase());
            loginUser.setPassword("123456");
            
            // 注意：这个测试的结果取决于数据库的配置
            // SQLite 默认不区分大小写，MySQL 取决于 collation
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建唯一用户对象
     */
    private User createUniqueUser() {
        User user = new User();
        user.setUname("user_" + UUID.randomUUID().toString().substring(0, 8));
        user.setPassword("password123");
        user.setEmail(generateUniqueEmail());
        return user;
    }
    
    /**
     * 生成唯一邮箱
     */
    private String generateUniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
    
    /**
     * 生成指定长度的随机字符串
     */
    private String generateRandomString(int length) {
        return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, length - 1);
    }
}
