package com.paper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 学术论文管理系统 - 启动类
 * 
 * 包结构说明：
 * - com.paper.controller: HTTP请求控制器
 * - com.paper.service: 业务逻辑层
 * - com.paper.dao: 数据访问层
 * - com.paper.model: 数据模型/实体类
 */
@SpringBootApplication
public class PaperApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperApplication.class, args);
    }

}
