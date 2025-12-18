package com.paper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.paper.utils.DatabaseInitializer;

/**
 * 学术论文管理系统 - 启动类
 * 
 * 包结构说明：
 * - com.paper.controller: HTTP请求控制器
 * - com.paper.service: 业务逻辑层
 * - com.paper.dao: 数据访问层
 * - com.paper.model: 数据模型/实体类
 * - com.paper.utils: 工具类（数据库配置、初始化等）
 */
@SpringBootApplication
public class PaperApplication {

    public static void main(String[] args) {
        // 初始化数据库（自动创建表结构）
        DatabaseInitializer.initialize();
        // 插入测试数据
        // TODO
        // DatabaseInitializer.insertTestData();
        
        SpringApplication.run(PaperApplication.class, args);
    }

}
