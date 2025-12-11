package com.paper.project;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paper.BBM.SearchManager;
import com.paper.BBM.PythonCaller;
import com.paper.Entity.Paper;

@Controller
public class HelloController {
    @GetMapping("/search/result")
    @ResponseBody
    public Map<String, Object> SearchResult(@RequestParam(required = false, defaultValue = "未传入内容") String keyword) {
        // 创建响应Map
        Map<String, Object> response = new HashMap<>();
        
        // 1. 业务逻辑：查询数据库获取搜索结果
        List<Paper> paperList = null;
        SearchManager searchManager = new SearchManager();
        long startTime = System.currentTimeMillis();
        
        try {
            paperList = searchManager.SearchByTarget(keyword);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            response.put("error", "搜索失败: " + e.getMessage());
            return response;
        }
        
        long endTime = System.currentTimeMillis();
        double searchTime = (endTime - startTime) / 1000.0;
        
        // 2. 设置基本搜索信息
        response.put("keyword", keyword);
        response.put("searchTime", searchTime);
        
        if (paperList != null && !paperList.isEmpty()) {
            response.put("totalResults", paperList.size());
            response.put("results", paperList);
            
            // 3. 调用Python脚本进行数据分析
            try {
                PythonCaller pythonCaller = new PythonCaller();
                Map<String, Object> analysisResult = pythonCaller.analyzePapers(paperList);
                response.put("analysis", analysisResult);
            } catch (Exception e) {
                System.err.println("数据分析失败: " + e.getMessage());
                response.put("analysisError", "数据分析失败: " + e.getMessage());
            }
            
        } else {
            response.put("totalResults", 0);
            response.put("results", paperList);
        }
        
        return response;
        
        // 2. 把数据存入 Model（key-value 形式）
        //model.addAttribute("keyword", keyword); // 存搜索关键词
        //model.addAttribute("results", paperList); // 存搜索结果数组

        // 3. 返回视图名（视图解析器会找 templates/search.html）
        //return "search";
    }
}
