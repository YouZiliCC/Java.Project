package com.paper.controller;

import java.sql.SQLException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paper.model.User;
import com.paper.service.UserService;

/**
 * 认证控制器
 * 处理用户登录、注册、验证码等认证相关的HTTP请求
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ResponseBody
    public String login(String uname, String password) throws ClassNotFoundException, SQLException {
        User user = new User();
        user.setUname(uname);
        user.setPassword(password);
        
        UserService userService = new UserService();
        if (userService.login(user)) {
            return "登录成功";
        } else {
            return "用户名或密码错误";
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ResponseBody
    public String register(String uname, String password, String email, String verifycode) 
            throws ClassNotFoundException, SQLException {
        User user = new User();
        user.setUname(uname);
        user.setPassword(password);
        user.setEmail(email);
        
        UserService userService = new UserService();
        return userService.registerByEmail(user, verifycode);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/verifycode")
    @ResponseBody
    public String sendVerifyCode(String email) throws ClassNotFoundException, SQLException {
        UserService userService = new UserService();
        return userService.sendRegisterCode(email);
    }
}
