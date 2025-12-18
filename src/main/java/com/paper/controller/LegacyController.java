package com.paper.controller;

import java.sql.SQLException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paper.service.UserService;
import com.paper.model.User;

/**
 * 兼容旧接口的控制器
 * 保持原有的URL路径不变，确保前端代码无需修改
 * 
 * 注意：这是为了向后兼容而保留的，新开发请使用 AuthController
 */
@Controller
public class LegacyController {

    /**
     * 旧版登录接口
     * @deprecated 请使用 /auth/login
     */
    @RequestMapping("/login")
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
     * 旧版注册接口（已关闭验证码验证）
     * @deprecated 请使用 /auth/register 或 /auth/register-direct
     */
    @RequestMapping("/submit")
    @ResponseBody
    public String submit(String uname, String password, String email, String verifycode) 
            throws ClassNotFoundException, SQLException {
        User user = new User();
        user.setUname(uname);
        user.setPassword(password);
        user.setEmail(email);
        
        UserService userService = new UserService();
        // 邮件验证已关闭，使用直接注册
        return userService.registerDirect(user);
    }

    /**
     * 直接注册接口（无需验证码）
     */
    @RequestMapping("/register-direct")
    @ResponseBody
    public String registerDirect(String uname, String password, String email) 
            throws ClassNotFoundException, SQLException {
        User user = new User();
        user.setUname(uname);
        user.setPassword(password);
        user.setEmail(email);
        
        UserService userService = new UserService();
        return userService.registerDirect(user);
    }

    /**
     * 旧版验证码接口（已关闭）
     * @deprecated 邮件验证功能已关闭
     */
    @RequestMapping("/verifycode")
    @ResponseBody
    public String verifycode(String email) throws ClassNotFoundException, SQLException {
        // 邮件验证已关闭，返回提示信息
        return "邮件验证功能已关闭，请直接注册";
    }
}
