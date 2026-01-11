# PaperMaster - 学术论文管理与分析系统

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.6-green" alt="Spring Boot 3.5.6">
  <img src="https://img.shields.io/badge/Python-3.x-blue" alt="Python 3.x">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="MIT License">
</p>

## 📖 项目概述

PaperMaster 是一个基于 Spring Boot 的学术论文管理与分析系统，集成了论文检索、用户管理、多维度数据分析和 AI 辅助功能。系统支持 WoS（Web of Science）数据导入与分析，提供关键词翻译、颠覆性指数计算、新颖性分析、跨学科分析、主题分析等多种学术分析功能。

## ✨ 功能特性

### 🔐 用户认证
- 用户注册（支持邮箱验证）
- 用户登录（BCrypt 加密）
- 个人信息管理
- 密码安全修改

### 📚 论文检索
- 关键词智能搜索
- 多字段匹配查询
- 搜索结果分析展示

### 📊 数据分析
- **关键词分析**: 关键词提取与翻译
- **颠覆性指数计算**: 评估论文的颠覆性影响
- **新颖性分析**: 分析论文的创新程度
- **跨学科分析**: 评估研究的跨学科程度
- **主题分析**: 主题建模与分类
- **Topic分析**: 深度主题挖掘

### 📖 期刊管理
- 期刊信息浏览与对比
- 期刊详情 AI 智能分析
- 期刊指标数据管理

### 🤖 AI 辅助
- 集成 DeepSeek/OpenAI 兼容 API
- 期刊详情智能解读
- 论文推荐匹配

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| **后端框架** | Java 21 + Spring Boot 3.5.6 |
| **数据库** | MySQL 8.x / SQLite（开发测试） |
| **ORM** | Spring Data JPA + Hibernate |
| **前端** | HTML5 + CSS3 + JavaScript + Thymeleaf |
| **数据分析** | Python 3.x + Pandas + NumPy |
| **密码加密** | BCrypt |
| **构建工具** | Gradle 8.x |
| **HTTP客户端** | Apache HttpClient |

## 📁 项目结构

```
paper-master/
├── src/main/
│   ├── java/com/paper/
│   │   ├── PaperApplication.java        # 应用入口
│   │   ├── config/                      # 配置类
│   │   ├── controller/                  # 控制器层
│   │   │   ├── AuthController.java      # 认证控制器
│   │   │   ├── UserController.java      # 用户管理
│   │   │   ├── SearchController.java    # 论文搜索
│   │   │   └── AnalysisController.java  # 数据分析
│   │   ├── service/                     # 服务层
│   │   │   ├── UserService.java         # 用户业务逻辑
│   │   │   ├── AIService.java           # AI 对话服务
│   │   │   └── PythonCaller.java        # Python 脚本调用
│   │   ├── dao/                         # 数据访问层
│   │   ├── model/                       # 数据模型
│   │   ├── repository/                  # JPA 仓库
│   │   └── utils/                       # 工具类
│   └── resources/
│       ├── application.properties       # 应用配置
│       ├── prompts/                     # AI 提示词
│       ├── static/                      # 前端静态资源
│       │   ├── index.html               # 首页
│       │   ├── analysis.html            # 分析页面
│       │   ├── auth/                    # 认证页面
│       │   ├── user/                    # 用户页面
│       │   ├── css/                     # 样式文件
│       │   └── js/                      # JavaScript
│       ├── templates/                   # Thymeleaf 模板
│       │   └── journal/                 # 期刊相关模板
│       ├── python/                      # Python 分析脚本
│       │   ├── main.py                  # 主入口
│       │   ├── preprocess_wos_data_030.py   # WoS 数据预处理
│       │   ├── disrupt_calculator_031.py    # 颠覆性指数计算
│       │   ├── interdisciplinary_032.py     # 跨学科分析
│       │   ├── novelty_analyzer_033.py      # 新颖性分析
│       │   ├── theme_034.py                 # 主题分析
│       │   ├── topic_analyzer_036.py        # Topic 分析
│       │   └── translate_keywords_02.py     # 关键词翻译
│       └── sql/                         # 数据库脚本
│           ├── init_sqlite.sql          # SQLite 初始化
│           └── init_journal_metrics_sqlite.sql
├── uploads/                             # 上传文件目录
├── .env.example                         # 环境变量示例
├── build.gradle                         # Gradle 构建配置
└── README.md                            # 项目说明
```

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Gradle**: 8.x
- **Python**: 3.10+
- **数据库**: MySQL 8.x 或 SQLite

### 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd paper-master
```

#### 2. 配置环境变量

```bash
# 复制环境变量示例文件
cp .env.example .env

# 编辑 .env 文件，填入实际配置
```

#### 3. 安装 Python 依赖

```bash
cd src/main/resources/python
pip install -r requirements.txt
```

#### 4. 配置应用

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置（SQLite - 开发环境）
spring.datasource.url=jdbc:sqlite:paper.db

# 或使用 MySQL（生产环境）
# spring.datasource.url=jdbc:mysql://localhost:3306/paper
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# AI API 配置（可选）
ai.api.base-url=https://api.deepseek.com/v3
ai.api.key=your_api_key
ai.api.model=deepseek-chat
```

#### 5. 构建项目

```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

#### 6. 运行项目

```bash
# Windows
gradlew.bat bootRun

# Linux/macOS
./gradlew bootRun
```

#### 7. 访问应用

打开浏览器访问: [http://localhost:5000](http://localhost:5000)

## 📡 API 接口

### 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |

### 用户接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/user/profile` | 获取用户信息 |
| PUT | `/api/user/profile` | 更新用户信息 |
| PUT | `/api/user/password` | 修改密码 |

### 分析接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/analysis/upload` | 上传分析文件 |
| POST | `/api/analysis/run` | 执行分析任务 |
| GET | `/api/analysis/result` | 获取分析结果 |

### 期刊接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/journal/list` | 期刊列表 |
| GET | `/journal/detail/{id}` | 期刊详情 |
| GET | `/journal/compare` | 期刊对比 |

## 🔒 安全特性

- ✅ 密码 BCrypt 加密存储
- ✅ 输入验证防止 SQL 注入
- ✅ 文件上传类型和大小限制（最大 1GB）
- ✅ 路径遍历攻击防护
- ✅ XSS 防护（输入清理）
- ✅ UTF-8 编码统一处理

## 🔧 配置说明

### 数据库配置

系统支持 SQLite 和 MySQL 两种数据库：

**SQLite（开发环境推荐）**
```properties
spring.datasource.url=jdbc:sqlite:paper.db
spring.datasource.driver-class-name=org.sqlite.JDBC
```

**MySQL（生产环境推荐）**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paper?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### AI 服务配置

支持 DeepSeek 及 OpenAI 兼容的 API：

```properties
ai.api.base-url=https://api.deepseek.com/v3
ai.api.key=sk-xxxxx
ai.api.model=deepseek-chat
ai.api.timeout=60
```

## 📊 Python 分析模块

| 脚本 | 功能 |
|------|------|
| `preprocess_wos_data_030.py` | WoS 数据预处理 |
| `disrupt_calculator_031.py` | 颠覆性指数计算 |
| `interdisciplinary_032.py` | 跨学科分析 |
| `novelty_analyzer_033.py` | 新颖性分析 |
| `theme_034.py` | 主题分析 |
| `topic_analyzer_036.py` | Topic 分析 |
| `translate_keywords_02.py` | 关键词翻译 |
| `baidu_translator_021.py` | 百度翻译 API |

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 发起 Pull Request

## 📝 开发规范

- 遵循 Java 命名规范
- 添加完整的 JavaDoc 注释
- 使用工具类减少重复代码
- 使用 `ResponseUtils` 统一响应格式
- 使用 `ValidationUtils` 验证输入

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 📧 联系方式

如有问题或建议，请提交 Issue 或联系项目维护者。

---

<p align="center">Made with ❤️ for Academic Research</p>
