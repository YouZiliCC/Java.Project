package com.paper.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paper.model.Paper;
import com.paper.service.PythonCaller;
import com.paper.service.SearchService;

/**
 * 搜索控制器
 * 处理论文搜索相关的HTTP请求
 */
@Controller
public class SearchController {

    @GetMapping("/search/result")
    @ResponseBody
    public Map<String, Object> searchResult(
            @RequestParam(required = false, defaultValue = "未传入内容") String keyword) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 1. 业务逻辑：查询数据库获取搜索结果
        List<Paper> paperList = null;
        SearchService searchService = new SearchService();
        long startTime = System.currentTimeMillis();
        
        try {
            paperList = searchService.searchByTarget(keyword);
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
    }
}
