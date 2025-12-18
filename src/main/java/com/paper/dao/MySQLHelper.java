package com.paper.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.paper.utils.DatabaseConfig;

/**
 * 数据库帮助类
 * 支持 SQLite（测试）和 MySQL（生产）
 */
public class MySQLHelper {
    
    private Connection connection;

    public MySQLHelper() throws ClassNotFoundException, SQLException {
        Class.forName(DatabaseConfig.getDriverClassName());
        
        if (DatabaseConfig.isSQLiteMode()) {
            connection = DriverManager.getConnection(DatabaseConfig.getUrl());
        } else {
            connection = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
            );
        }
    }
    
    /**
     * 执行更新类SQL（INSERT, UPDATE, DELETE）
     * @param sql SQL语句
     * @param params 参数
     * @return 错误信息（空字符串表示成功）
     */
    public String executeSQL(String sql, Object... params) {
        String errorString = "";
        PreparedStatement pstmt = null;
        
        try {
            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
        
            pstmt = connection.prepareStatement(sql);
            setParameters(pstmt, params);
            int affectedRows = pstmt.executeUpdate();
        
            if (affectedRows == 0) {
                errorString = "SQL执行成功，但未影响任何数据（可能参数不匹配）";
            }
        
        } catch (SQLException ex) {
            errorString = "SQL执行异常：" + ex.getMessage();
            try {
                if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                errorString += "；回滚事务失败：" + e.getMessage();
            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    errorString += "；关闭PreparedStatement失败：" + e.getMessage();
                }
            }
        }
        return errorString;
    }
    
    /**
     * 执行查询类SQL（SELECT）
     * @param sql SQL语句
     * @param params 参数
     * @return 包含 "result" (ResultSet) 和 "error" (String) 的Map
     */
    public Map<String, Object> executeSQLWithSelect(String sql, Object... params) {
        Map<String, Object> result = new HashMap<>();
        ResultSet resultSet = null;
        PreparedStatement pstmt = null;
        String errorString = "";
        
        try {
            pstmt = connection.prepareStatement(sql);
            setParameters(pstmt, params);
            resultSet = pstmt.executeQuery();
        } catch (Exception e) {
            errorString = e.getMessage();
        }
        
        result.put("result", resultSet);
        result.put("error", errorString);
        return result;
    }
    
    /**
     * 设置PreparedStatement的参数
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
