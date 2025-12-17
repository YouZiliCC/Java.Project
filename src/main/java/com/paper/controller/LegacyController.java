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
     * 旧版注册接口
     * @deprecated 请使用 /auth/register
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
        return userService.registerByEmail(user, verifycode);
    }

    /**
     * 旧版验证码接口
     * @deprecated 请使用 /auth/verifycode
     */
    @RequestMapping("/verifycode")
    @ResponseBody
    public String verifycode(String email) throws ClassNotFoundException, SQLException {
        UserService userService = new UserService();
        return userService.sendRegisterCode(email);
    }
}
