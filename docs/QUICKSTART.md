# 期刊Dashboard快速启动指南

## 前置要求

1. **JDK 21** 已安装
2. **MySQL数据库** 已运行，并有 `journal_metrics` 表
3. **（可选）DeepSeek API密钥** 用于AI分析功能

## 配置步骤

### 1. 配置数据库

编辑 `config/metrics_db.json`:

```json
{
  "db_config": {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "你的MySQL密码",
    "database": "你的数据库名",
    "charset": "utf8mb4"
  }
}
```

### 2. 配置AI服务（可选）

编辑 `config/ai_config.json`:

```json
{
  "DEEPSEEK_API_BASE": "https://api.deepseek.com/v3",
  "DEEPSEEK_API_KEY": "你的API密钥",
  "DEEPSEEK_MODEL": "deepseek-chat"
}
```

> 如果不配置AI，系统仍可正常使用，只是AI分析按钮将不可用。

### 3. 数据库表结构

确保MySQL中有 `journal_metrics` 表，包含以下字段：

```sql
CREATE TABLE journal_metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    journal VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    disruption DOUBLE,
    interdisciplinary DOUBLE,
    novelty DOUBLE,
    topic DOUBLE,
    theme_concentration DOUBLE,
    hot_response DOUBLE,
    paper_count INT,
    category VARCHAR(255),
    top_keywords_2021 TEXT,
    top_keywords_2022 TEXT,
    top_keywords_2023 TEXT,
    top_keywords_2024 TEXT,
    top_keywords_2025 TEXT,
    INDEX idx_journal (journal),
    INDEX idx_year (year)
);
```

## 启动应用

### 方式1: 使用Gradle

```bash
# Windows PowerShell
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### 方式2: 使用IDE

1. 在IDEA或VSCode中打开项目
2. 运行 `PaperApplication.java` 主类

## 访问应用

启动成功后，浏览器访问：

- **期刊列表**: http://localhost:8080/journal/
- **填写问卷**: http://localhost:8080/journal/survey
- **查看推荐**: http://localhost:8080/journal/recommend
- **对比期刊**: http://localhost:8080/journal/compare

## 功能测试流程

### 1. 测试期刊浏览
1. 访问 http://localhost:8080/journal/
2. 应该能看到所有期刊列表
3. 点击任意期刊名称，查看详情页

### 2. 测试个性化推荐
1. 访问 http://localhost:8080/journal/survey
2. 填写研究关键词（如：machine learning, deep learning）
3. 选择偏好（1-5分）
4. 提交后自动跳转到推荐页面
5. 查看Top 10推荐期刊
6. 点击任意期刊查看详细匹配分析

### 3. 测试期刊对比
1. 访问 http://localhost:8080/journal/compare
2. 从下拉框选择两本期刊
3. 点击"对比"按钮
4. 查看雷达图、柱状图和文字分析

### 4. 测试AI分析（需配置API）
1. 在期刊详情页点击"AI分析"按钮
2. 等待AI生成分析报告
3. 在推荐详情页点击"AI匹配分析"
4. 查看AI生成的投稿建议

## 常见问题

### Q: 启动时报数据库连接错误
**A**: 检查以下几点：
- MySQL是否正在运行
- `config/metrics_db.json` 中的密码是否正确
- 数据库名称是否存在
- 用户是否有权限访问该数据库

### Q: 页面显示404
**A**: 
- 确认URL路径是否正确（必须包含 `/journal/` 前缀）
- 检查Controller类上的 `@RequestMapping` 注解

### Q: 静态资源（CSS）无法加载
**A**: 
- 检查 `src/main/resources/static/journal/` 目录是否存在CSS文件
- 清除浏览器缓存重试

### Q: AI分析按钮点击无响应
**A**: 
- 检查 `config/ai_config.json` 是否正确配置
- 打开浏览器开发者工具查看网络请求错误信息
- 检查API密钥是否有效

### Q: 推荐页面提示"未填写问卷"
**A**: 
- 必须先访问 `/journal/survey` 填写问卷
- 问卷数据存储在Cookie中，清除Cookie后需重新填写

## 环境变量配置（可选）

如果不想在配置文件中存储敏感信息，可以使用环境变量：

### Windows PowerShell
```powershell
$env:JOURNAL_DASHBOARD_DB_PASSWORD="your_password"
$env:DEEPSEEK_API_KEY="your_api_key"
.\gradlew.bat bootRun
```

### Linux/Mac
```bash
export JOURNAL_DASHBOARD_DB_PASSWORD="your_password"
export DEEPSEEK_API_KEY="your_api_key"
./gradlew bootRun
```

## 端口配置

默认端口是8080，如需修改，编辑 `src/main/resources/application.properties`:

```properties
server.port=9090
```

## 下一步

- 根据实际需求调整HTML模板样式
- 集成到现有的用户登录系统
- 添加更多数据可视化图表
- 优化移动端显示效果

## 技术支持

如遇到问题，请查看：
1. 控制台日志输出
2. [docs/JOURNAL_DASHBOARD_INTEGRATION.md](JOURNAL_DASHBOARD_INTEGRATION.md) 详细文档
3. 浏览器开发者工具的Network和Console面板
