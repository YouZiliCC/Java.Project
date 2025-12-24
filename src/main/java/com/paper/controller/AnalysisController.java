package com.paper.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.dao.AnalysisDAO;
import com.paper.model.AnalysisRecord;
import com.paper.service.AnalysisService;
import com.paper.utils.ResponseUtils;
import com.paper.utils.ValidationUtils;

/**
 * 期刊分析控制器
 */
@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".json", ".csv");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 上传数据文件（需要用户名）
     */
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String username) {
        
        if (file.isEmpty()) {
            return ResponseUtils.error("请选择要上传的文件");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseUtils.error("文件大小不能超过10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseUtils.error("仅支持JSON和CSV格式文件");
        }
        
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 保存到数据库
            if (ValidationUtils.isNotBlank(username)) {
                try {
                    AnalysisDAO dao = new AnalysisDAO();
                    dao.saveUploadRecord(username, uniqueFilename, originalFilename, file.getSize());
                    dao.close();
                } catch (Exception e) {
                    System.err.println("保存上传记录失败: " + e.getMessage());
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("filename", uniqueFilename);
            data.put("originalName", originalFilename);
            data.put("size", file.getSize());
            return ResponseUtils.success("文件上传成功", data);
            
        } catch (IOException e) {
            return ResponseUtils.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 运行数据分析（保存结果到数据库）
     */
    @PostMapping("/run")
    @ResponseBody
    public Map<String, Object> runAnalysis(
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String username) {
        try {
            AnalysisService analysisService = new AnalysisService();
            Map<String, Object> result;
            
            if (ValidationUtils.isNotBlank(filename)) {
                if (!ValidationUtils.isSafeFilename(filename)) {
                    return ResponseUtils.error("无效的文件名");
                }
                result = analysisService.analyzeFile(UPLOAD_DIR + filename);
            } else {
                result = analysisService.analyzeAllData();
            }
            
            // 保存分析结果到数据库
            if (result.get("success").equals(true) && ValidationUtils.isNotBlank(filename)) {
                try {
                    AnalysisDAO dao = new AnalysisDAO();
                    String analysisJson = objectMapper.writeValueAsString(result.get("analysis"));
                    dao.updateAnalysisResult(filename, analysisJson);
                    dao.close();
                } catch (Exception e) {
                    System.err.println("保存分析结果失败: " + e.getMessage());
                }
            }
            
            return result;
            
        } catch (Exception e) {
            return ResponseUtils.error("数据分析失败: " + e.getMessage());
        }
    }

    /**
     * AI对话接口（包含分析上下文）
     */
    @PostMapping("/chat")
    @ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String context = request.get("context");
        String username = request.get("username");
        
        if (ValidationUtils.isBlank(message)) {
            return ResponseUtils.error("请输入消息");
        }
        
        if (message.length() > 1000) {
            return ResponseUtils.error("消息内容过长");
        }
        
        try {
            // 如果没有传入 context，尝试获取用户最近的分析结果
            if (ValidationUtils.isBlank(context) && ValidationUtils.isNotBlank(username)) {
                try {
                    AnalysisDAO dao = new AnalysisDAO();
                    AnalysisRecord record = dao.getLatestByUsername(username);
                    if (record != null && record.getAnalysisResult() != null) {
                        context = record.getAnalysisResult();
                    }
                    dao.close();
                } catch (Exception e) {
                    System.err.println("获取分析上下文失败: " + e.getMessage());
                }
            }
            
            AnalysisService analysisService = new AnalysisService();
            String reply = analysisService.chat(message.trim(), context);
            
            Map<String, Object> data = new HashMap<>();
            data.put("reply", reply);
            return ResponseUtils.success("success", data);
            
        } catch (Exception e) {
            return ResponseUtils.error("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的分析历史
     */
    @GetMapping("/history")
    @ResponseBody
    public Map<String, Object> getHistory(@RequestParam String username) {
        if (ValidationUtils.isBlank(username)) {
            return ResponseUtils.error("请提供用户名");
        }
        
        try {
            AnalysisDAO dao = new AnalysisDAO();
            List<AnalysisRecord> records = dao.getHistoryByUsername(username, 10);
            dao.close();
            
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (AnalysisRecord record : records) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId());
                item.put("filename", record.getFilename());
                item.put("originalName", record.getOriginalName());
                item.put("fileSize", record.getFileSize());
                item.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
                
                // 解析分析结果摘要
                if (record.getAnalysisResult() != null) {
                    try {
                        Map<String, Object> analysis = objectMapper.readValue(record.getAnalysisResult(), Map.class);
                        item.put("totalPapers", analysis.get("total_papers"));
                        item.put("avgCitations", analysis.get("avg_citations"));
                    } catch (Exception e) {
                        item.put("totalPapers", 0);
                    }
                }
                historyList.add(item);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("history", historyList);
            return ResponseUtils.success("success", data);
            
        } catch (Exception e) {
            return ResponseUtils.error("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取特定分析记录详情
     */
    @GetMapping("/detail")
    @ResponseBody
    public Map<String, Object> getDetail(@RequestParam String filename) {
        if (ValidationUtils.isBlank(filename) || !ValidationUtils.isSafeFilename(filename)) {
            return ResponseUtils.error("无效的文件名");
        }
        
        try {
            AnalysisDAO dao = new AnalysisDAO();
            AnalysisRecord record = dao.getByFilename(filename);
            dao.close();
            
            if (record == null) {
                return ResponseUtils.error("记录不存在");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("filename", record.getFilename());
            data.put("originalName", record.getOriginalName());
            data.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
            
            if (record.getAnalysisResult() != null) {
                data.put("analysis", objectMapper.readValue(record.getAnalysisResult(), Map.class));
            }
            
            return ResponseUtils.success("success", data);
            
        } catch (Exception e) {
            return ResponseUtils.error("获取详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取 AI 配置状态
     */
    @GetMapping("/ai-status")
    @ResponseBody
    public Map<String, Object> getAIStatus() {
        Map<String, Object> data = new HashMap<>();
        
        boolean isConfigured = com.paper.config.EnvConfig.hasValidDeepSeekKey();
        
        data.put("configured", isConfigured);
        data.put("provider", "deepseek");
        data.put("model", com.paper.config.EnvConfig.get(
            com.paper.config.EnvConfig.DEEPSEEK_MODEL, "deepseek-chat"));
        
        if (!isConfigured) {
            data.put("message", "AI 功能未启用。请在 .env 文件中配置 DEEPSEEK_API_KEY");
        }
        
        return ResponseUtils.success("success", data);
    }

    /**
     * 获取已上传的文件列表
     */
    @GetMapping("/files")
    @ResponseBody
    public Map<String, Object> listFiles() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            List<String> fileList = new ArrayList<>();
            
            if (Files.exists(uploadPath) && Files.isDirectory(uploadPath)) {
                Files.list(uploadPath)
                     .filter(Files::isRegularFile)
                     .forEach(path -> fileList.add(path.getFileName().toString()));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("files", fileList);
            return ResponseUtils.success("success", data);
            
        } catch (IOException e) {
            return ResponseUtils.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除已上传的文件
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteFile(@RequestParam String filename) {
        if (!ValidationUtils.isSafeFilename(filename)) {
            return ResponseUtils.error("无效的文件名");
        }
        
        try {
            // 删除数据库记录
            try {
                AnalysisDAO dao = new AnalysisDAO();
                dao.deleteByFilename(filename);
                dao.close();
            } catch (Exception e) {
                System.err.println("删除数据库记录失败: " + e.getMessage());
            }
            
            // 删除文件
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseUtils.success("文件删除成功");
            } else {
                return ResponseUtils.error("文件不存在");
            }
        } catch (IOException e) {
            return ResponseUtils.error("文件删除失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
