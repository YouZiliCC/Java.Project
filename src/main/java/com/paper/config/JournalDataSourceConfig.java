package com.paper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 期刊数据源配置
 */
@Configuration
public class JournalDataSourceConfig {
    
    /**
     * 期刊数据库数据源
     */
    @Bean(name = "journalDataSource")
    @ConfigurationProperties(prefix = "journal.datasource")
    @Primary
    public DataSource journalDataSource() {
        return DataSourceBuilder.create().build();
    }
}
