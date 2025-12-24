package com.paper.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paper.model.AnalysisRecord;

/**
 * 分析记录数据访问对象
 */
public class AnalysisDAO {
    
    private final MySQLHelper mysqlHelper;
    
    public AnalysisDAO() throws ClassNotFoundException, SQLException {
        this.mysqlHelper = new MySQLHelper();
    }
    
    /**
     * 保存文件上传记录
     */
    public String saveUploadRecord(String username, String filename, String originalName, Long fileSize) {
        String sql = "INSERT INTO ANALYSIS_RECORD (username, filename, original_name, file_size) VALUES (?, ?, ?, ?)";
        return mysqlHelper.executeSQL(sql, username, filename, originalName, fileSize);
    }
    
    /**
     * 更新分析结果
     */
    public String updateAnalysisResult(String filename, String analysisResult) {
        String sql = "UPDATE ANALYSIS_RECORD SET analysis_result = ? WHERE filename = ?";
        return mysqlHelper.executeSQL(sql, analysisResult, filename);
    }
    
    /**
     * 根据文件名获取记录
     */
    public AnalysisRecord getByFilename(String filename) {
        String sql = "SELECT * FROM ANALYSIS_RECORD WHERE filename = ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, filename);
        
        String error = (String) result.get("error");
        if (error != null && !error.isEmpty()) {
            return null;
        }
        
        try (ResultSet rs = (ResultSet) result.get("result")) {
            if (rs != null && rs.next()) {
                return mapResultSetToRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询分析记录失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取用户的分析历史
     */
    public List<AnalysisRecord> getHistoryByUsername(String username, int limit) {
        String sql = "SELECT * FROM ANALYSIS_RECORD WHERE username = ? AND analysis_result IS NOT NULL ORDER BY created_at DESC LIMIT ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, username, limit);
        
        List<AnalysisRecord> records = new ArrayList<>();
        String error = (String) result.get("error");
        if (error != null && !error.isEmpty()) {
            return records;
        }
        
        try (ResultSet rs = (ResultSet) result.get("result")) {
            while (rs != null && rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询分析历史失败: " + e.getMessage());
        }
        return records;
    }
    
    /**
     * 获取用户最近一次分析结果
     */
    public AnalysisRecord getLatestByUsername(String username) {
        String sql = "SELECT * FROM ANALYSIS_RECORD WHERE username = ? AND analysis_result IS NOT NULL ORDER BY created_at DESC LIMIT 1";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, username);
        
        String error = (String) result.get("error");
        if (error != null && !error.isEmpty()) {
            return null;
        }
        
        try (ResultSet rs = (ResultSet) result.get("result")) {
            if (rs != null && rs.next()) {
                return mapResultSetToRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询最近分析记录失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 删除分析记录
     */
    public String deleteByFilename(String filename) {
        String sql = "DELETE FROM ANALYSIS_RECORD WHERE filename = ?";
        return mysqlHelper.executeSQL(sql, filename);
    }
    
    /**
     * 将 ResultSet 映射为 AnalysisRecord
     */
    private AnalysisRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        AnalysisRecord record = new AnalysisRecord();
        record.setId(rs.getLong("id"));
        record.setUsername(rs.getString("username"));
        record.setFilename(rs.getString("filename"));
        record.setOriginalName(rs.getString("original_name"));
        record.setFileSize(rs.getLong("file_size"));
        record.setAnalysisResult(rs.getString("analysis_result"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try {
                record.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
            } catch (Exception e) {
                record.setCreatedAt(LocalDateTime.now());
            }
        }
        return record;
    }
    
    public void close() {
        mysqlHelper.close();
    }
}
