package com.paper.DBM;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MySQLHelper {
    private Connection connection;
    
    public MySQLHelper() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PAPER_sys","root","MySQLnewPassword2025");
    }
    
    // 执行非查询的SQL语句，支持参数化查询
    public String executeSQL(String sqlString, Object... params){
        String errorString = "";
        PreparedStatement pstmt = null;
        try {
            // 使用PreparedStatement替代Statement
            pstmt = this.connection.prepareStatement(sqlString);
            // 设置参数
            setParameters(pstmt, params);
            // 执行sql语句
            pstmt.execute();
        } catch (SQLException ex) {
            errorString = ex.getMessage();
        } finally {
            // 关闭资源
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
    
    // 执行查询的SQL语句，支持参数化查询
    public Map<String,Object> executeSQLWithSelect(String sqlString, Object... params){
        Map<String,Object> map = new HashMap<String,Object>();
        ResultSet resultset = null;
        PreparedStatement pstmt = null;
        String errorString = "";
        
        try {
            pstmt = this.connection.prepareStatement(sqlString);
            // 设置参数
            setParameters(pstmt, params);
            resultset = pstmt.executeQuery();
        } catch (Exception e) {
            errorString = e.getMessage();
        }
        
        map.put("result", resultset);
        map.put("error", errorString);
        return map;
    }
    
    // 设置PreparedStatement的参数
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    // 关闭数据库连接
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
