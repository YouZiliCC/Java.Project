package com.paper.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @RequestMapping("/hello")
    @ResponseBody
    public String hi(@RequestParam(required = false, defaultValue = "未传入内容") String question) {
        // 拼接参数返回（此时question不可能为null，避免NullPointerException）
        return "你的搜索内容：" + question;
    }
}
