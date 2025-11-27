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
    
    private static final String DB_PASSWORD = System.getenv("JAVA_DB_PASSWORD");
    

    public MySQLHelper() throws ClassNotFoundException, SQLException{
        //System.out.println("密码是"+DB_PASSWORD);
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://39.106.90.206:3306/PAPER_sys","PAPER_sys",DB_PASSWORD);
    }
    
    public String executeSQL(String sqlString, Object... params) {
        String errorString = "";
        PreparedStatement pstmt = null;
        try {
            // 1. 确保连接自动提交（关键：防止事务未提交）
            if (connection.getAutoCommit() == false) {
                connection.setAutoCommit(true);
            }
        
            // 2. 使用PreparedStatement替代Statement
            pstmt = this.connection.prepareStatement(sqlString);
            // 3. 设置参数
            setParameters(pstmt, params);
            // 4. 关键修改：用 executeUpdate() 执行 INSERT，获取受影响行数
            int affectedRows = pstmt.executeUpdate();
        
            // 5. 验证是否成功插入（受影响行数 > 0 表示成功）
            if (affectedRows == 0) {
                errorString = "SQL执行成功，但未影响任何数据（可能参数不匹配）";
            }
        
        } catch (SQLException ex) {
            // 捕获SQL异常，返回具体错误信息（方便排查）
            errorString = "SQL执行异常：" + ex.getMessage();
            // 若有事务，发生异常时回滚（避免脏数据）
            try {
                if (connection != null && !connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                errorString += "；回滚事务失败：" + e.getMessage();
            }
        } finally {
            // 6. 关闭资源
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
