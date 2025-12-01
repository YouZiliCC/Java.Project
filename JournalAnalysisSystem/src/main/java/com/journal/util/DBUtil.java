package com.journal.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 使用HikariCP连接池管理数据库连接
 */
public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static HikariDataSource dataSource;

    static {
        try {
            initDataSource();
        } catch (Exception e) {
            logger.error("初始化数据库连接池失败", e);
            throw new RuntimeException("初始化数据库连接池失败", e);
        }
    }

    private static void initDataSource() throws IOException {
        Properties props = new Properties();
        try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new IOException("找不到db.properties配置文件");
            }
            props.load(is);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));

        // 连接池配置
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));

        // 其他优化配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        logger.info("数据库连接池初始化成功");
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接（归还到连接池）
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("关闭数据库连接失败", e);
            }
        }
    }

    /**
     * 关闭数据源
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("数据库连接池已关闭");
        }
    }
}
