package com.paper.service;

import java.io.BufferedReader;
import java.io.File;
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

    /** Python分析主脚本路径 */
    private static final String PYTHON_MAIN_SCRIPT = "src/main/resources/python/main.py";
    
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
     * <p>直接调用Python脚本分析文件所在目录</p>
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
        
        // 验证文件类型
        String filename = file.getName().toLowerCase();
        if (!filename.endsWith(".json") && !filename.endsWith(".csv")) {
            response.put("success", false);
            response.put("message", "不支持的文件格式，请上传JSON或CSV文件");
            return response;
        }
        
        // 直接使用文件所在目录调用Python分析
        // 这样可以保留原始CSV/JSON格式，让Python脚本直接处理
        String userDirPath = file.getParent();
        return runPythonAnalysis(userDirPath);
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
     * <p>调用Python脚本进行完整的指标分析</p>
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
        
        // 调用 Python main.py --user-dir 模式进行分析
        return runPythonAnalysis(userDirPath);
    }
    
    /**
     * 调用Python脚本分析用户数据
     * Python 脚本会将结果保存到 outputs/analysis_result.json 文件
     * 
     * @param userDirPath 用户目录路径
     * @return 分析结果
     */
    private Map<String, Object> runPythonAnalysis(String userDirPath) throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        // 删除旧的结果文件，确保读取的是新结果
        File oldResultFile = new File(userDirPath, "outputs/analysis_result.json");
        if (oldResultFile.exists()) {
            oldResultFile.delete();
            System.out.println("[Debug] Deleted old result file: " + oldResultFile.getAbsolutePath());
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", "-u", PYTHON_MAIN_SCRIPT, 
                "--user-dir", userDirPath,
                "--json"  // 启用JSON模式（日志输出到stderr）
        );
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(false);  // 分离错误输出
        
        // 设置 Python 环境变量，强制使用 UTF-8 编码
        Map<String, String> env = processBuilder.environment();
        env.put("PYTHONIOENCODING", "utf-8");
        env.put("PYTHONUTF8", "1");
        
        System.out.println("[Debug] Starting Python analysis process...");
        Process process = processBuilder.start();
        
        // 使用多线程同时读取stdout和stderr，避免缓冲区满导致的死锁
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        
        // 异步读取stderr（日志信息）
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                    System.err.println("[Python] " + line);
                }
            } catch (Exception e) {
                System.err.println("[Error] Failed to read stderr: " + e.getMessage());
            }
        });
        stderrThread.start();
        
        // 主线程读取stdout
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        
        // 等待stderr线程完成
        stderrThread.join();
        
        int exitCode = process.waitFor();
        System.out.println("[Debug] Python process exit code: " + exitCode);
        
        if (exitCode != 0) {
            response.put("success", false);
            response.put("message", "Python分析脚本执行失败");
            response.put("error", errorOutput.toString());
            return response;
        }
        
        // 从文件读取分析结果
        File resultFile = new File(userDirPath, "outputs/analysis_result.json");
        if (!resultFile.exists()) {
            response.put("success", false);
            response.put("message", "分析结果文件不存在: " + resultFile.getAbsolutePath());
            return response;
        }
        
        try {
            System.out.println("[Debug] Reading result file: " + resultFile.getAbsolutePath());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> analysisResult = objectMapper.readValue(resultFile, Map.class);
            
            // 检查Python返回的结果
            if (analysisResult.containsKey("success") && Boolean.FALSE.equals(analysisResult.get("success"))) {
                response.put("success", false);
                response.put("message", analysisResult.get("message"));
                return response;
            }
            
            response.put("success", true);
            response.put("message", "分析完成");
            response.put("analysis", analysisResult);
            response.put("totalPapers", analysisResult.get("total_records"));
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "解析分析结果文件失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 调用Python脚本分析论文数据（JSON格式）
     * 这个方法用于分析单个文件或数据库的论文数据
     */
    private Map<String, Object> analyzePapersData(List<Paper> papers) throws Exception {
        Map<String, Object> response = new HashMap<>();
        
        // 将论文数据写入临时文件
        File tempFile = File.createTempFile("papers_", ".json");
        tempFile.deleteOnExit();
        objectMapper.writeValue(tempFile, papers);
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", "-u", PYTHON_MAIN_SCRIPT, 
                "--user-dir", tempFile.getParent(),
                "--json"
        );
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(false);  // 分离错误输出，避免日志混入 JSON
        
        // 设置 Python 环境变量，强制使用 UTF-8 编码
        Map<String, String> env = processBuilder.environment();
        env.put("PYTHONIOENCODING", "utf-8");
        env.put("PYTHONUTF8", "1");
        
        Process process = processBuilder.start();
        
        // 使用多线程同时读取stdout和stderr，避免缓冲区满导致的死锁
        StringBuilder output = new StringBuilder();
        
        // 异步读取stderr（日志信息）
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[Python] " + line);
                }
            } catch (Exception e) {
                System.err.println("[Error] Failed to read stderr: " + e.getMessage());
            }
        });
        stderrThread.start();
        
        // 主线程读取stdout（JSON）
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        
        // 等待stderr线程完成
        stderrThread.join();
        
        int exitCode = process.waitFor();
        
        // 清理临时文件
        tempFile.delete();
        
        if (exitCode != 0) {
            response.put("success", false);
            response.put("message", "Python分析脚本执行失败");
            response.put("error", output.toString());
            return response;
        }
        
        @SuppressWarnings("unchecked")
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
            System.err.println("AI call failed: " + e.getMessage());
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
}
