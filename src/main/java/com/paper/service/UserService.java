package com.paper.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.mindrot.jbcrypt.BCrypt;

import com.paper.dao.MySQLHelper;
import com.paper.model.User;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * 用户服务类
 * 负责用户认证、注册、邮件验证等业务逻辑
 */
public class UserService {
    
    private MySQLHelper mysqlHelper;
    private static final String QQ_MAIL_PASSWORD = System.getenv("QQ_MAIL_PASSWORD");
    
    // 存储验证码和邮箱的映射关系
    private static Map<String, String> verificationCodeMap = new HashMap<>();
    // 存储验证码创建时间
    private static Map<String, Long> codeExpireTimeMap = new HashMap<>();
    
    public UserService() throws ClassNotFoundException, SQLException {
        this.mysqlHelper = new MySQLHelper();
    }
    
    /**
     * 用户登录验证
     */
    public boolean login(User user) throws SQLException {
        String sql = "SELECT PASSWORD FROM USER WHERE uname = ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, user.getUname());
        
        ResultSet rs = null;
        try {
            if (result.get("result") != null) {
                rs = (ResultSet) result.get("result");
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");
                    return BCrypt.checkpw(user.getPassword(), storedHashedPassword);
                }
            }
        } finally {
            closeResultSet(rs);
        }
        return false;
    }

    /**
     * 修改密码
     */
    public String updatePassword(User user) {
        String sql = "UPDATE USER SET password = ? WHERE uname = ?";
        return mysqlHelper.executeSQL(sql, user.getPassword(), user.getUname());
    }
    
    /**
     * 发送注册验证码到邮箱
     */
    public String sendRegisterCode(String email) {
        String code = generateRandomCode();
        
        // 清理之前的验证码记录
        verificationCodeMap.remove(email);
        codeExpireTimeMap.remove(email);
        
        // 存储新验证码
        verificationCodeMap.put(email, code);
        codeExpireTimeMap.put(email, System.currentTimeMillis());
        
        // 邮件配置
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.debug", "true");
        
        final String fromEmail = "2878580863@qq.com";
        final String password = QQ_MAIL_PASSWORD;
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("账号注册验证码");
            message.setText("您的注册验证码是：" + code + "，有效期5分钟，请妥善保管。");
            
            Transport.send(message);
            return "发送成功";
        } catch (MessagingException e) {
            return "验证码发送失败：" + e.getMessage();
        }
    }
    
    /**
     * 通过邮箱和验证码完成注册
     * 注意：当前已关闭邮件验证，验证码参数可传空或任意值
     */
    public String registerByEmail(User user, String code) throws SQLException {
        // 验证邮箱是否已被注册
        if (isEmailExists(user.getEmail())) {
            return "该邮箱已被注册";
        }
        
        // 验证用户名是否已存在
        if (isUsernameExists(user.getUname())) {
            return "该用户名已存在";
        }
        
        // 邮件验证已关闭，跳过验证码检查
        // 如需开启邮件验证，取消下面代码的注释
        /*
        // 验证验证码
        String storedCode = verificationCodeMap.get(user.getEmail());
        Long expireTime = codeExpireTimeMap.get(user.getEmail());
        
        if (storedCode == null || !storedCode.equals(code)) {
            return "验证码错误";
        }
        
        // 验证码过期检查（5分钟）
        if (expireTime == null || System.currentTimeMillis() - expireTime > 5 * 60 * 1000) {
            verificationCodeMap.remove(user.getEmail());
            codeExpireTimeMap.remove(user.getEmail());
            return "验证码已过期";
        }
        */
        
        // 执行注册
        String sql = "INSERT INTO USER (uname, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String result = mysqlHelper.executeSQL(sql, user.getUname(), hashedPassword, user.getEmail());
        
        if (result.isEmpty()) {
            verificationCodeMap.remove(user.getEmail());
            codeExpireTimeMap.remove(user.getEmail());
            return "注册成功";
        }
        
        return result;
    }
    
    /**
     * 直接注册（无需验证码）
     */
    public String registerDirect(User user) throws SQLException {
        // 验证邮箱是否已被注册（邮箱可选）
        if (user.getEmail() != null && !user.getEmail().isEmpty() && isEmailExists(user.getEmail())) {
            return "该邮箱已被注册";
        }
        
        // 验证用户名是否已存在
        if (isUsernameExists(user.getUname())) {
            return "该用户名已存在";
        }
        
        // 验证用户名格式
        if (user.getUname() == null || user.getUname().trim().isEmpty()) {
            return "用户名不能为空";
        }
        
        if (user.getUname().length() < 2 || user.getUname().length() > 20) {
            return "用户名长度应在2-20个字符之间";
        }
        
        // 验证密码强度
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return "密码长度不能少于6位";
        }
        
        // 执行注册
        String sql = "INSERT INTO USER (uname, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String email = (user.getEmail() != null && !user.getEmail().isEmpty()) ? user.getEmail() : null;
        String result = mysqlHelper.executeSQL(sql, user.getUname(), hashedPassword, email);
        
        if (result.isEmpty()) {
            return "注册成功";
        }
        
        return result;
    }
    
    /**
     * 生成6位随机验证码
     */
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * 检查邮箱是否已存在
     */
    private boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT * FROM USER WHERE email = ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, email);
        
        ResultSet rs = null;
        try {
            if (result.get("result") != null) {
                rs = (ResultSet) result.get("result");
                return rs.next();
            }
        } finally {
            closeResultSet(rs);
        }
        return false;
    }
    
    /**
     * 检查用户名是否已存在
     */
    private boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT * FROM USER WHERE uname = ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, username);
        
        ResultSet rs = null;
        try {
            if (result.get("result") != null) {
                rs = (ResultSet) result.get("result");
                return rs.next();
            }
        } finally {
            closeResultSet(rs);
        }
        return false;
    }
    
    /**
     * 安全关闭 ResultSet
     */
    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
