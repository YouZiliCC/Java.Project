# PaperMaster - 学术论文管理系统

## 项目概述

PaperMaster 是一个基于 Spring Boot 的学术论文管理与分析系统，提供论文检索、用户管理、数据分析和 AI 辅助功能。

## 技术栈

- **后端**: Java 17 + Spring Boot 3.x
- **数据库**: MySQL / SQLite
- **前端**: HTML5 + CSS3 + JavaScript
- **数据分析**: Python 3.x
- **密码加密**: BCrypt

## 项目结构

```
src/
├── main/
│   ├── java/com/paper/
│   │   ├── controller/     # 控制器层
│   │   │   ├── AuthController.java      # 认证（登录/注册）
│   │   │   ├── UserController.java      # 用户管理
│   │   │   ├── SearchController.java    # 论文搜索
│   │   │   └── AnalysisController.java  # 数据分析
│   │   ├── service/        # 服务层
│   │   │   ├── UserService.java         # 用户业务逻辑
│   │   │   ├── SearchService.java       # 搜索业务逻辑
│   │   │   ├── AnalysisService.java     # 分析业务逻辑
│   │   │   └── PythonCaller.java        # Python脚本调用
│   │   ├── dao/            # 数据访问层
│   │   ├── model/          # 数据模型
│   │   └── utils/          # 工具类
│   │       ├── ResponseUtils.java       # 响应工具
│   │       └── ValidationUtils.java     # 验证工具
│   └── resources/
│       ├── static/         # 前端静态资源
│       │   ├── index.html              # 首页
│       │   ├── login.html              # 登录页
│       │   ├── register.html           # 注册页
│       │   ├── profile.html            # 个人中心
│       │   ├── analysis.html           # 期刊分析
│       │   ├── css/                    # 样式文件
│       │   └── js/                     # JavaScript
│       ├── python/         # Python分析脚本
│       └── sql/            # 数据库初始化脚本
└── test/                   # 单元测试
docs/
└── API.md                  # API文档
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
