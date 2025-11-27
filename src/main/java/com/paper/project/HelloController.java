package com.paper.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

@Controller
public class HelloController {
    @GetMapping("/search/result")
    @ResponseBody
    public String SearchResult(@RequestParam(required = false, defaultValue = "未传入内容") String keyword, Model model) {
        // 拼接参数返回（此时question不可能为null，避免NullPointerException）
        // 1. 业务逻辑：比如查询数据库获取搜索结果
        String[] searchResults = {"结果1：" + keyword, "结果2：" + keyword + "教程"};

        // 2. 把数据存入 Model（key-value 形式）
        model.addAttribute("keyword", keyword); // 存搜索关键词
        model.addAttribute("results", searchResults); // 存搜索结果数组

        // 3. 返回视图名（视图解析器会找 templates/search.html）
        return "search";
    }
}
