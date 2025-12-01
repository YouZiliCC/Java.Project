package com.journal.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

/**
 * Thymeleaf配置类
 */
public class ThymeleafConfig {
    private static TemplateEngine templateEngine;

    /**
     * 初始化Thymeleaf模板引擎
     */
    public static void init(ServletContext servletContext) {
        // 创建模板解析器
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // 开发环境关闭缓存

        // 创建模板引擎
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    /**
     * 获取模板引擎
     */
    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
