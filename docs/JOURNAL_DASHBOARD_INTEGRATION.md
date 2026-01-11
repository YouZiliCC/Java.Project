# 期刊Dashboard功能整合说明

## 概述

已将 `dashboard_app` 文件夹中的Python FastAPI应用完整转换为Java Spring Boot应用，并整合到现有项目中。

## 转换内容

### 1. 模型层 (Model)
- **JournalMetrics.java**: 期刊指标实体类，对应数据库journal_metrics表
- **UserSurvey.java**: 用户问卷调查实体类

### 2. 数据访问层 (DAO)
- **JournalMetricsDAO.java**: 
  - 期刊数据访问对象
  - 实现了所有数据库查询功能
  - 支持关键词解析（JSON数组、Python字符串等多种格式）

### 3. 业务逻辑层 (Service)
- **JournalService.java**:
  - 雷达图数据构建
  - 规则评语生成（四象限分析）
  - 用户画像与期刊相似度计算
  - 关键词解析和归一化
  - AI分析调用封装

### 4. 控制器层 (Controller)
- **JournalController.java**:
  - 期刊列表页面 (`/journal/`)
  - 期刊详情页面 (`/journal/{journal}`)
  - 期刊AI分析接口 (`POST /journal/{journal}/ai-analysis`)
  - 问卷页面 (`/journal/survey`)
  - 问卷提交 (`POST /journal/survey/analyze`)
  - 推荐页面 (`/journal/recommend`)

- **JournalCompareController.java**:
  - 推荐详情页面 (`/journal/recommend/{journal}`)
  - 推荐详情AI分析 (`POST /journal/recommend/{journal}/ai-analysis`)
  - 期刊对比页面 (`/journal/compare?j1=xxx&j2=yyy`)

### 5. 工具类 (Utils)
- **AIClient.java**: 
  - OpenAI兼容API调用封装
  - 支持DeepSeek等AI服务
  - 配置文件和环境变量双重配置支持

- **JournalDatabaseConfig.java**:
  - 数据库配置加载
  - 支持从metrics_db.json读取配置
  - 环境变量优先级覆盖

### 6. 模板和静态文件
已复制到对应位置：
- **模板文件**: `src/main/resources/templates/journal/`
  - journals.html (期刊列表)
  - journal_detail.html (期刊详情)
  - survey.html (问卷页面)
  - recommend.html (推荐列表)
  - recommend_detail.html (推荐详情)
  - compare.html (期刊对比)

- **静态文件**: `src/main/resources/static/journal/`
  - app.css (样式文件)

- **AI提示词**: `src/main/resources/prompts/`
  - journal_detail_system.txt
  - recommend_match_system.txt

## 配置说明

### 数据库配置

方式1：通过配置文件 `config/metrics_db.json`
```json
{
  "db_config": {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "your_password",
    "database": "paper_db",
    "charset": "utf8mb4"
  }
}
```

方式2：通过环境变量（优先级更高）
```
JOURNAL_DASHBOARD_DB_HOST=127.0.0.1
JOURNAL_DASHBOARD_DB_PORT=3306
JOURNAL_DASHBOARD_DB_USER=root
JOURNAL_DASHBOARD_DB_PASSWORD=your_password
JOURNAL_DASHBOARD_DB_DATABASE=paper_db
```

### AI配置

方式1：通过配置文件 `config/ai_config.json`
```json
{
  "DEEPSEEK_API_BASE": "https://api.deepseek.com/v3",
  "DEEPSEEK_API_KEY": "your_api_key_here",
  "DEEPSEEK_MODEL": "deepseek-chat"
}
```

方式2：通过环境变量（优先级更高）
```
DEEPSEEK_API_BASE=https://api.deepseek.com/v3
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_MODEL=deepseek-chat
```

## 访问路径

启动应用后，访问以下路径：

- **期刊列表**: http://localhost:8080/journal/
- **期刊详情**: http://localhost:8080/journal/{期刊名称}
- **问卷填写**: http://localhost:8080/journal/survey
- **个性化推荐**: http://localhost:8080/journal/recommend (需先填写问卷)
- **期刊对比**: http://localhost:8080/journal/compare?j1=期刊1&j2=期刊2

## 主要功能

### 1. 期刊浏览
- 查看所有期刊列表及其核心指标
- 查看单个期刊的详细信息和历年数据
- 雷达图可视化展示期刊特征
- AI智能分析期刊定位

### 2. 个性化推荐
- 用户填写研究兴趣问卷（关键词+偏好）
- 系统基于关键词匹配和画像相似度推荐期刊
- 展示用户画像雷达图
- 查看每个推荐期刊的详细匹配度分析
- AI智能生成投稿建议

### 3. 期刊对比
- 选择两本期刊进行多维度对比
- 叠加雷达图直观展示差异
- 柱状图对比各项指标
- 文本分析说明优劣势

## 技术特点

1. **完整的MVC架构**: Model - Service - DAO - Controller清晰分层
2. **RESTful API**: 支持异步AI分析接口
3. **Cookie会话**: 问卷数据通过Base64编码存储在Cookie中，无需数据库
4. **配置灵活**: 支持配置文件和环境变量，便于部署
5. **Thymeleaf模板**: 服务端渲染，与原Python应用保持一致的用户体验
6. **AI集成**: 完整支持OpenAI兼容的API（DeepSeek等）

## 依赖项

已在 `build.gradle` 中添加：
```gradle
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.2'
implementation 'org.apache.httpcomponents:httpclient:4.5.14'
runtimeOnly 'mysql:mysql-connector-java:8.0.30'
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
```

## 注意事项

1. **数据库表**: 需要确保MySQL中有 `journal_metrics` 表，包含所有必要字段
2. **AI配置**: 如果不配置AI，页面仍可正常访问，但AI分析按钮将无法使用
3. **模板路径**: HTML模板中引用静态资源的路径可能需要调整为 `/journal/xxx`
4. **端口**: 默认8080端口，可在 `application.properties` 中修改

## 下一步工作

1. 调整HTML模板中的静态资源引用路径
2. 配置 `config/metrics_db.json` 和 `config/ai_config.json`
3. 测试所有页面功能
4. 根据需要调整样式和布局
5. 考虑添加权限控制（与现有用户系统集成）

## Python到Java对照表

| Python模块 | Java类 | 说明 |
|-----------|--------|------|
| app.py | JournalController.java<br>JournalCompareController.java | FastAPI路由 → Spring MVC Controller |
| db.py | JournalMetricsDAO.java | SQLAlchemy → JDBC |
| ai_client.py | AIClient.java | requests → HttpURLConnection |
| model (隐式) | JournalMetrics.java<br>UserSurvey.java | 实体类 |
| business logic | JournalService.java | 业务逻辑层 |
