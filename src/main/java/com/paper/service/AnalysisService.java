package com.paper.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.dao.MySQLHelper;
import com.paper.model.Paper;
import com.paper.utils.ResponseUtils;

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
    
    /** 数据库查询限制 */
    private static final int MAX_QUERY_LIMIT = 1000;
    
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
     * 注意：这是一个简化的实现，实际生产环境需要接入真正的AI API
     */
    public String chat(String message, String context) {
        // 简单的关键词匹配回复
        // 实际生产环境应该接入OpenAI、Azure OpenAI或其他AI服务
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("论文") || lowerMessage.contains("paper")) {
            return "关于论文分析，您可以上传论文数据文件（支持JSON和CSV格式），然后点击\"运行数据分析\"按钮来获取统计结果。分析结果包括论文总数、平均引用次数、领域分布等信息。";
        }
        
        if (lowerMessage.contains("引用") || lowerMessage.contains("citation")) {
            return "引用分析是衡量论文影响力的重要指标。系统会统计平均引用数、最高被引论文等信息。高引用论文通常代表该领域的重要研究成果。";
        }
        
        if (lowerMessage.contains("分析") || lowerMessage.contains("analysis")) {
            return "数据分析功能支持：\n1. 论文数量统计\n2. 引用次数分析\n3. 领域分布统计\n4. 国家/地区分布\n5. 最高被引论文识别\n\n您可以上传数据或直接分析数据库中的现有数据。";
        }
        
        if (lowerMessage.contains("上传") || lowerMessage.contains("upload")) {
            return "支持上传JSON和CSV格式的论文数据文件。JSON文件应包含论文对象数组，CSV文件应包含标题、作者、期刊、引用数等字段。";
        }
        
        if (lowerMessage.contains("帮助") || lowerMessage.contains("help")) {
            return "欢迎使用期刊分析系统！\n\n主要功能：\n1. 数据上传：上传您的论文数据\n2. 数据分析：运行统计分析\n3. AI助手：解答您的问题\n\n您可以问我任何关于论文分析的问题！";
        }
        
        if (context != null && !context.isEmpty()) {
            return "根据当前的分析结果，" + context + "。您还有什么想了解的吗？";
        }
        
        return "感谢您的提问！我是期刊分析AI助手，可以帮助您理解数据分析结果、解答论文相关问题。请问有什么可以帮助您的？";
    }

    /**
     * 从数据库获取所有论文
     */
    private List<Paper> getAllPapers() throws SQLException {
        List<Paper> papers = new ArrayList<>();
        String sql = "SELECT * FROM PAPER LIMIT 1000";
        
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql);
        ResultSet rs = null;
        
        try {
            if (result.get("result") != null) {
                rs = (ResultSet) result.get("result");
                while (rs.next()) {
                    Paper paper = new Paper();
                    paper.setTitle(rs.getString("title"));
                    paper.setAuthor(rs.getString("author"));
                    paper.setJournal(rs.getString("journal"));
                    paper.setPublishDate(rs.getString("publish_date"));
                    paper.setCitations(rs.getInt("citations"));
                    paper.setRefs(rs.getInt("refs"));
                    paper.setTarget(rs.getString("target"));
                    paper.setCountry(rs.getString("country"));
                    paper.setKeywords(rs.getString("keywords"));
                    paper.setAbstractText(rs.getString("abstract_text"));
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
