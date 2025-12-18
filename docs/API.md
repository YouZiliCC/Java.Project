# PaperMaster API 文档

## 概述

PaperMaster 是一个学术论文管理与分析系统，提供论文检索、用户管理和数据分析功能。

**基础URL**: `http://localhost:8080`

**响应格式**: JSON

---

## 目录

1. [认证接口](#认证接口)
2. [用户接口](#用户接口)
3. [搜索接口](#搜索接口)
4. [分析接口](#分析接口)

---

## 认证接口

### 用户登录

登录系统获取用户认证。

- **URL**: `/auth/login`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uname | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**响应示例**:

```
登录成功
```

```
用户名或密码错误
```

---

### 用户注册（直接注册）

注册新用户账号，无需验证码。

- **URL**: `/auth/register-direct`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uname | string | 是 | 用户名（2-20字符） |
| password | string | 是 | 密码（最少6位） |
| email | string | 否 | 邮箱地址 |

**响应示例**:

```
注册成功
```

```
该用户名已存在
```

---

## 用户接口

### 获取用户信息

获取指定用户的基本信息。

- **URL**: `/user/info`
- **Method**: `GET`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uname | string | 是 | 用户名 |

**响应示例**:

```json
{
    "success": true,
    "message": "success",
    "uname": "testuser",
    "email": "test@example.com"
}
```

---

### 修改密码

修改用户登录密码。

- **URL**: `/user/change-password`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uname | string | 是 | 用户名 |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码（最少6位） |

**响应示例**:

```json
{
    "success": true,
    "message": "密码修改成功"
}
```

```json
{
    "success": false,
    "message": "原密码错误"
}
```

---

### 修改邮箱

修改用户邮箱地址。

- **URL**: `/user/change-email`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uname | string | 是 | 用户名 |
| newEmail | string | 是 | 新邮箱地址 |

**响应示例**:

```json
{
    "success": true,
    "message": "邮箱修改成功"
}
```

---

## 搜索接口

### 论文搜索

根据关键词搜索论文。

- **URL**: `/search/result`
- **Method**: `GET`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 搜索关键词 |

**响应示例**:

```json
{
    "keyword": "machine learning",
    "searchTime": 0.125,
    "totalResults": 42,
    "results": [
        {
            "title": "Deep Learning for NLP",
            "author": "John Doe",
            "journal": "Nature",
            "publishDate": "2024-01-15",
            "citations": 156,
            "abstractText": "...",
            "keywords": "deep learning;NLP;transformer"
        }
    ],
    "analysis": {
        "total_papers": 42,
        "avg_citations": 45.5,
        "target_distribution": {"AI": 20, "ML": 22}
    }
}
```

---

## 分析接口

### 上传数据文件

上传论文数据文件用于分析。

- **URL**: `/analysis/upload`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 数据文件（支持JSON、CSV格式，最大10MB） |

**响应示例**:

```json
{
    "success": true,
    "message": "文件上传成功",
    "filename": "550e8400-e29b-41d4-a716-446655440000.json",
    "originalName": "papers.json",
    "size": 102400
}
```

---

### 运行数据分析

对上传的文件或数据库数据进行分析。

- **URL**: `/analysis/run`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 否 | 文件名（为空则分析数据库数据） |

**响应示例**:

```json
{
    "success": true,
    "message": "分析完成",
    "totalPapers": 100,
    "analysis": {
        "message": "数据分析完成",
        "total_papers": 100,
        "avg_citations": 35.5,
        "avg_refs": 28.3,
        "target_distribution": {
            "计算机科学": 45,
            "人工智能": 35,
            "数据科学": 20
        },
        "country_distribution": {
            "中国": 40,
            "美国": 30,
            "英国": 15
        },
        "most_cited_paper": {
            "title": "Attention Is All You Need",
            "citations": 50000,
            "author": "Vaswani et al."
        }
    }
}
```

---

### AI对话

与AI助手进行对话，获取论文分析相关帮助。

- **URL**: `/analysis/chat`
- **Method**: `POST`
- **Content-Type**: `application/json`

**请求体**:

```json
{
    "message": "如何分析论文数据？",
    "context": "可选的分析结果上下文"
}
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | string | 是 | 用户消息（最长1000字符） |
| context | string | 否 | 分析结果上下文 |

**响应示例**:

```json
{
    "success": true,
    "message": "success",
    "reply": "数据分析功能支持：\n1. 论文数量统计\n2. 引用次数分析\n3. 领域分布统计..."
}
```

---

### 获取文件列表

获取已上传的文件列表。

- **URL**: `/analysis/files`
- **Method**: `GET`

**响应示例**:

```json
{
    "success": true,
    "message": "success",
    "files": [
        "550e8400-e29b-41d4-a716-446655440000.json",
        "6ba7b810-9dad-11d1-80b4-00c04fd430c8.csv"
    ]
}
```

---

### 删除文件

删除已上传的文件。

- **URL**: `/analysis/delete`
- **Method**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 是 | 要删除的文件名 |

**响应示例**:

```json
{
    "success": true,
    "message": "文件删除成功"
}
```

---

## 错误码说明

所有接口统一使用以下响应格式：

```json
{
    "success": true/false,
    "message": "操作结果说明"
}
```

| success | 说明 |
|---------|------|
| true | 操作成功 |
| false | 操作失败，查看message获取错误信息 |

---

## 数据格式

### 论文数据 (JSON)

```json
[
    {
        "title": "论文标题",
        "author": "作者",
        "journal": "期刊名称",
        "publishDate": "2024-01-01",
        "citations": 100,
        "refs": 50,
        "target": "研究领域",
        "country": "国家",
        "keywords": "关键词1;关键词2",
        "abstractText": "摘要内容"
    }
]
```

### 论文数据 (CSV)

```csv
title,author,journal,citations,country,target
Deep Learning,John Doe,Nature,100,USA,AI
```

---

## 安全说明

1. **密码加密**: 所有密码使用 BCrypt 算法加密存储
2. **文件验证**: 上传文件限制为 JSON/CSV 格式，最大 10MB
3. **路径安全**: 文件名经过验证，防止路径遍历攻击
4. **输入验证**: 所有用户输入经过格式验证和长度限制

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2024-12-18 | 初始版本 |
