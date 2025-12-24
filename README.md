# PaperMaster - 学术论文管理系统

## 项目概述

PaperMaster 是一个基于 Spring Boot 的学术论文管理与分析系统，提供论文检索、用户管理、数据分析和 AI 辅助功能。

## 技术栈

- **后端**: Java 21 + Spring Boot 3.5
- **数据库**: MySQL 8.x / SQLite（开发测试）
- **前端**: HTML5 + CSS3 + JavaScript
- **数据分析**: Python 3.x
- **密码加密**: BCrypt
- **构建工具**: Gradle

## 项目结构

```
paper-master/
├── src/main/
│   ├── java/com/paper/
│   │   ├── PaperApplication.java    # 应用入口
│   │   ├── config/                  # 配置类
│   │   │   └── EnvConfig.java       # 环境配置管理
│   │   ├── controller/              # 控制器层
│   │   │   ├── AuthController.java      # 认证（登录/注册）
│   │   │   ├── UserController.java      # 用户管理
│   │   │   ├── SearchController.java    # 论文搜索
│   │   │   └── AnalysisController.java  # 数据分析
│   │   ├── service/                 # 服务层
│   │   │   ├── UserService.java         # 用户业务逻辑
│   │   │   ├── SearchService.java       # 搜索业务逻辑
│   │   │   ├── AnalysisService.java     # 分析业务逻辑
│   │   │   ├── AIService.java           # AI对话服务
│   │   │   └── PythonCaller.java        # Python脚本调用
│   │   ├── dao/                     # 数据访问层
│   │   │   └── MySQLHelper.java         # 数据库操作工具
│   │   ├── model/                   # 数据模型
│   │   │   ├── User.java / Paper.java / Author.java / Keyword.java
│   │   └── utils/                   # 工具类
│   │       ├── DatabaseConfig.java      # 数据库配置
│   │       ├── DatabaseInitializer.java # 数据库初始化
│   │       ├── ResponseUtils.java       # 响应工具
│   │       └── ValidationUtils.java     # 验证工具
│   └── resources/
│       ├── application.properties   # 应用配置
│       ├── static/                  # 前端静态资源
│       │   ├── *.html               # 页面文件
│       │   ├── css/style.css        # 样式
│       │   └── js/                  # JavaScript
│       ├── python/                  # Python分析脚本
│       │   └── data_analysis.py
│       └── sql/                     # 数据库初始化脚本
│           ├── init_mysql.sql
│           └── init_sqlite.sql
├── docs/API.md                      # API文档
├── .env.example                     # 环境变量示例
└── build.gradle                     # Gradle构建配置
```

## 功能模块

### 1. 用户认证
- 用户注册（支持邮箱）
- 用户登录（BCrypt加密）
- 个人信息管理
- 密码修改

### 2. 论文检索
- 关键词搜索
- 多字段匹配
- 结果分析展示

### 3. 期刊分析
- 数据文件上传（JSON/CSV）
- 数据库数据分析
- 统计结果可视化
- AI 对话辅助

## 快速开始

### 环境要求
- JDK 17+
- Gradle 8.x
- Python 3.x
- MySQL 8.x (或 SQLite)

### 启动步骤

1. **配置数据库**
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/paper
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

2. **初始化数据库**
   ```sql
   -- 执行 src/main/resources/sql/init_mysql.sql
   ```

3. **构建项目**
   ```bash
   ./gradlew build
   ```

4. **运行项目**
   ```bash
   ./gradlew bootRun
   ```

5. **访问应用**
   ```
   http://localhost:8080
   ```

## API 文档

详细API文档请参阅 [docs/API.md](docs/API.md)

## 安全特性

- ✅ 密码 BCrypt 加密存储
- ✅ 输入验证防止注入攻击
- ✅ 文件上传类型和大小限制
- ✅ 路径遍历攻击防护
- ✅ XSS 防护（输入清理）

## 开发指南

### 添加新接口
1. 在对应 Controller 中添加方法
2. 使用 `ResponseUtils` 统一响应格式
3. 使用 `ValidationUtils` 验证输入
4. 更新 API 文档

### 代码规范
- 遵循 Java 命名规范
- 添加完整的 JavaDoc 注释
- 使用工具类减少重复代码

## 许可证

MIT License
