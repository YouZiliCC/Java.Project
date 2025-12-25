package com.paper.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.config.EnvConfig;
import com.paper.dao.MySQLHelper;
import com.paper.model.Paper;

/**
 * 期刊分析服务类
 * <p>负责数据分析、AI对话等业务逻辑</p>
 * 
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>分析上传的JSON/CSV文件</li>
 *   <li>分析数据库中的论文数据</li>
 *   <li>提供AI对话功能（简化实现）</li>
 * </ul>
 * 
 * @author PaperMaster Team
 * @version 1.0
 * @since 2024-12-18
 */
public class AnalysisService {

    /** Python分析脚本路径 */
    private static final String ANALYSIS_SCRIPT_PATH = "src/main/resources/python/data_analysis.py";
    
    /** JSON对象映射器 */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final MySQLHelper mysqlHelper;

    /**
     * 构造函数，初始化数据库连接
     * 
     * @throws ClassNotFoundException 数据库驱动未找到
     * @throws SQLException 数据库连接失败
     */
    public AnalysisService() throws ClassNotFoundException, SQLException {
        this.mysqlHelper = new MySQLHelper();
    }

    /**
     * 分析上传的文件
     * <p>支持JSON和CSV格式的论文数据文件</p>
     * 
     * @param filePath 文件路径
     * @return 分析结果，包含统计信息
     * @throws Exception 文件读取或解析失败
     */
    public Map<String, Object> analyzeFile(String filePath) throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        File file = new File(filePath);
        if (!file.exists()) {
            response.put("success", false);
            response.put("message", "文件不存在");
            return response;
        }
        
        // 读取文件内容
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        // 根据文件类型进行分析
        String filename = file.getName().toLowerCase();
        List<Paper> papers = new ArrayList<>();
        
        if (filename.endsWith(".json")) {
            // 解析JSON文件
            papers = objectMapper.readValue(content.toString(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Paper.class));
        } else if (filename.endsWith(".csv")) {
            // 解析CSV文件
            papers = parseCsvContent(content.toString());
        } else {
            response.put("success", false);
            response.put("message", "不支持的文件格式，请上传JSON或CSV文件");
            return response;
        }
        
        // 调用Python进行分析
        return analyzePapersData(papers);
    }

    /**
     * 分析数据库中的所有数据
     * <p>从数据库获取论文数据并进行统计分析</p>
     * 
     * @return 分析结果
     * @throws Exception 数据库查询或分析失败
     */
    public Map<String, Object> analyzeAllData() throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        // 从数据库获取所有论文数据
        List<Paper> papers = getAllPapers();
        
        if (papers.isEmpty()) {
            response.put("success", true);
            response.put("message", "数据库中暂无数据");
            response.put("analysis", new HashMap<>());
            return response;
        }
        
        return analyzePapersData(papers);
    }

    /**
     * 分析用户目录下的所有CSV/JSON文件
     * <p>读取用户上传目录下的所有数据文件并进行统计分析</p>
     * 
     * @param userDirPath 用户目录路径
     * @return 分析结果
     * @throws Exception 文件读取或分析失败
     */
    public Map<String, Object> analyzeUserDirectory(String userDirPath) throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        File userDir = new File(userDirPath);
        if (!userDir.exists() || !userDir.isDirectory()) {
            response.put("success", false);
            response.put("message", "用户目录不存在，请先上传文件");
            return response;
        }
        
        // 获取目录下所有CSV和JSON文件
        File[] dataFiles = userDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".csv") || lower.endsWith(".json");
        });
        
        if (dataFiles == null || dataFiles.length == 0) {
            response.put("success", false);
            response.put("message", "目录下没有数据文件，请先上传CSV或JSON文件");
            return response;
        }
        
        // 合并所有文件中的论文数据
        List<Paper> allPapers = new ArrayList<>();
        List<String> processedFiles = new ArrayList<>();
        
        for (File file : dataFiles) {
            try {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                
                List<Paper> papers;
                String filename = file.getName().toLowerCase();
                
                if (filename.endsWith(".json")) {
                    papers = objectMapper.readValue(content.toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Paper.class));
                } else {
                    papers = parseCsvContent(content.toString());
                }
                
                allPapers.addAll(papers);
                processedFiles.add(file.getName());
                
            } catch (Exception e) {
                System.err.println("处理文件失败: " + file.getName() + " - " + e.getMessage());
            }
        }
        
        if (allPapers.isEmpty()) {
            response.put("success", false);
            response.put("message", "无法从文件中读取到有效的论文数据");
            return response;
        }
        
        // 调用分析
        Map<String, Object> result = analyzePapersData(allPapers);
        result.put("processedFiles", processedFiles);
        result.put("totalFiles", processedFiles.size());
        
        return result;
    }

    /**
     * 调用Python脚本分析论文数据
     */
    private Map<String, Object> analyzePapersData(List<Paper> papers) throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        String papersJson = objectMapper.writeValueAsString(papers);
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", ANALYSIS_SCRIPT_PATH, papersJson
        );
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            response.put("success", false);
            response.put("message", "Python分析脚本执行失败");
            response.put("error", output.toString());
            return response;
        }
        
        Map<String, Object> analysisResult = objectMapper.readValue(output.toString(), Map.class);
        
        response.put("success", true);
        response.put("message", "分析完成");
        response.put("analysis", analysisResult);
        response.put("totalPapers", papers.size());
        
        return response;
    }

    /**
     * AI对话功能
     */
    public String chat(String message, String context) {
        String apiKey = EnvConfig.get(EnvConfig.DEEPSEEK_API_KEY);
        
        // 检查 API Key 是否有效
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your-")) {
            return fallbackChat(message, context);
        }
        
        try {
            AIService aiService = new AIService();
            String systemPrompt = "你是一个学术论文分析助手，帮助用户理解和分析学术论文数据。用中文回答。";
            
            if (context != null && !context.isEmpty()) {
                systemPrompt += "\n\n当前分析结果：" + context;
            }
            
            return aiService.chat(message, systemPrompt);
        } catch (Exception e) {
            System.err.println("AI 调用失败: " + e.getMessage());
            return fallbackChat(message, context);
        }
    }
    
    /**
     * 简单关键词匹配回复（未配置 API 时使用）
     */
    private String fallbackChat(String message, String context) {
        String msg = message.toLowerCase();
        
        if (msg.contains("论文") || msg.contains("paper")) {
            return "您可以上传论文数据文件（JSON/CSV），然后点击\"运行数据分析\"获取统计结果。";
        }
        if (msg.contains("引用") || msg.contains("citation")) {
            return "系统会统计平均引用数、最高被引论文等信息。";
        }
        if (msg.contains("分析") || msg.contains("analysis")) {
            return "支持：论文数量统计、引用分析、领域分布、国家分布等。";
        }
        if (msg.contains("帮助") || msg.contains("help")) {
            return "主要功能：1.上传数据 2.运行分析 3.AI助手\n\n配置 DEEPSEEK_API_KEY 可获得智能对话。";
        }
        if (context != null && !context.isEmpty()) {
            return "当前分析结果：" + context;
        }
        return "我是论文分析助手。请在 .env 中配置 DEEPSEEK_API_KEY 以启用智能对话。";
    }

    /**
     * 从数据库获取所有论文
     * 使用 papers 表（按文档定义的字段结构）
     */
    private List<Paper> getAllPapers() throws SQLException {
        List<Paper> papers = new ArrayList<>();
        String sql = "SELECT * FROM papers";
        
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql);
        ResultSet rs = null;
        
        try {
            if (result.get("result") != null) {
                rs = (ResultSet) result.get("result");
                while (rs.next()) {
                    Paper paper = new Paper();
                    paper.setTitle(rs.getString("title"));
                    paper.setDoi(rs.getString("doi"));
                    paper.setJournal(rs.getString("journal"));
                    paper.setKeywords(rs.getString("keywords"));
                    // publish_date 在新表中是年份整数
                    int publishYear = rs.getInt("publish_date");
                    if (publishYear > 0) {
                        paper.setPublishDate(java.time.LocalDate.of(publishYear, 1, 1));
                    }
                    paper.setTarget(rs.getString("target"));
                    paper.setAbstractText(rs.getString("abstract"));
                    paper.setCategory(rs.getString("category"));
                    // citations 在新表中是TEXT类型（参考文献列表）
                    String citationsText = rs.getString("citations");
                    if (citationsText != null) {
                        // 简单统计引用数（按分隔符）
                        paper.setCitations(citationsText.split(";").length);
                    }
                    papers.add(paper);
                }
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return papers;
    }

    /**
     * 解析CSV内容为论文列表
     */
    private List<Paper> parseCsvContent(String content) {
        List<Paper> papers = new ArrayList<>();
        String[] lines = content.split("\n");
        
        if (lines.length < 2) {
            return papers;
        }
        
        // 解析表头
        String[] headers = lines[0].split(",");
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerIndex.put(headers[i].trim().toLowerCase(), i);
        }
        
        // 解析数据行
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            if (values.length < 2) continue;
            
            Paper paper = new Paper();
            
            if (headerIndex.containsKey("title") && headerIndex.get("title") < values.length) {
                paper.setTitle(values[headerIndex.get("title")].trim());
            }
            if (headerIndex.containsKey("author") && headerIndex.get("author") < values.length) {
                paper.setAuthor(values[headerIndex.get("author")].trim());
            }
            if (headerIndex.containsKey("journal") && headerIndex.get("journal") < values.length) {
                paper.setJournal(values[headerIndex.get("journal")].trim());
            }
            if (headerIndex.containsKey("citations") && headerIndex.get("citations") < values.length) {
                try {
                    paper.setCitations(Integer.parseInt(values[headerIndex.get("citations")].trim()));
                } catch (NumberFormatException e) {
                    paper.setCitations(0);
                }
            }
            if (headerIndex.containsKey("country") && headerIndex.get("country") < values.length) {
                paper.setCountry(values[headerIndex.get("country")].trim());
            }
            if (headerIndex.containsKey("target") && headerIndex.get("target") < values.length) {
                paper.setTarget(values[headerIndex.get("target")].trim());
            }
            
            papers.add(paper);
        }
        
        return papers;
    }
}
