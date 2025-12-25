# 百度翻译API组件使用说明

## 简介

这是一个基于百度翻译API v3开发的Python组件，提供可靠、高效的翻译服务，支持单句翻译、批量翻译、翻译词表管理等功能。

## 功能特点

- ✅ **多语言翻译**：支持中英日韩法德俄西葡意等多种语言
- ✅ **自动检测语言**：自动识别输入文本的语言
- ✅ **批量翻译**：高效处理大量文本翻译请求
- ✅ **翻译词表**：支持自定义翻译对，提升翻译一致性
- ✅ **请求频率控制**：自动控制API请求频率，避免触发限制
- ✅ **自动重试**：网络错误或API限制时自动重试
- ✅ **详细日志**：记录翻译过程和统计信息
- ✅ **统计功能**：追踪翻译次数、词表命中、错误情况等

## 安装依赖

```bash
pip install requests
```

## 快速开始

### 1. 获取百度翻译API凭证

在使用前，你需要：
1. 访问 [百度翻译开放平台](https://fanyi-api.baidu.com/)
2. 注册并创建应用，获取`App ID`和`Secret Key`

### 2. 基本使用

```python
from baidu_translator import NewBaiduTranslator

# 方法1：直接提供凭证
translator = NewBaiduTranslator(
    app_id="你的App ID",
    secret_key="你的Secret Key"
)

# 方法2：通过环境变量设置凭证（推荐）
# 先设置环境变量：BAIDU_APP_ID和BAIDU_SECRET_KEY
translator = NewBaiduTranslator()

# 单句翻译
result = translator.translate("Hello world", from_lang="en", to_lang="zh")
print(f"翻译结果: {result}")

# 批量翻译
texts = ["Hello", "How are you?", "Goodbye"]
translated_texts = translator.batch_translate(texts, from_lang="en", to_lang="zh")
print(f"批量翻译结果: {translated_texts}")
```

## API 文档

### 类初始化

```python
NewBaiduTranslator(app_id=None, secret_key=None, **kwargs)
```

**参数：**
- `app_id`: 百度翻译API的App ID
- `secret_key`: 百度翻译API的Secret Key
- `timeout`: 请求超时时间（秒），默认30秒
- `max_retries`: 最大重试次数，默认5次
- `retry_delay`: 重试间隔（秒），默认5秒
- `batch_size`: 批量翻译的批次大小，默认10
- `rate_limit`: 每秒最大请求数，默认0.5（每2秒1个请求）
- `glossary_file`: 翻译词表文件路径，默认'translation_glossary.json'

### 主要方法

#### 翻译单个文本
```python
translate(text: str, from_lang: str = 'auto', to_lang: str = 'zh', add_to_glossary: bool = False) -> str
```

**参数：**
- `text`: 待翻译文本
- `from_lang`: 源语言（默认自动检测）
- `to_lang`: 目标语言（默认中文）
- `add_to_glossary`: 是否将翻译结果添加到词表

**返回：**
- 翻译后的文本

#### 批量翻译文本
```python
batch_translate(texts: List[str], from_lang: str = 'auto', to_lang: str = 'zh', add_to_glossary: bool = False) -> List[str]
```

**参数：**
- `texts`: 待翻译文本列表
- `from_lang`: 源语言（默认自动检测）
- `to_lang`: 目标语言（默认中文）
- `add_to_glossary`: 是否将翻译结果添加到词表

**返回：**
- 翻译后的文本列表

### 词表管理

#### 添加翻译对
```python
add_to_glossary(source_text: str, translated_text: str, from_lang: str = 'zh', to_lang: str = 'en')
```

#### 移除翻译对
```python
remove_from_glossary(source_text: str, from_lang: str = 'zh', to_lang: str = 'en')
```

#### 导入词表
```python
import_glossary(glossary_file: str, overwrite: bool = False) -> Dict
```

#### 导出词表
```python
export_glossary(export_file: str) -> bool
```

### 统计功能

#### 打印统计信息
```python
print_stats()
```

#### 获取统计信息
```python
get_stats() -> Dict
```

## 支持的语言

| 代码 | 语言   | 代码 | 语言   |
|------|--------|------|--------|
| auto | 自动检测 | zh   | 中文   |
| en   | 英语   | ja   | 日语   |
| ko   | 韩语   | fr   | 法语   |
| de   | 德语   | ru   | 俄语   |
| es   | 西班牙语 | pt   | 葡萄牙语 |
| it   | 意大利语 |      |        |

## 高级配置

```python
# 自定义配置示例
translator = NewBaiduTranslator(
    timeout=60,                # 60秒超时
    max_retries=3,             # 最多重试3次
    retry_delay=3,             # 重试间隔3秒
    batch_size=5,              # 批量大小5
    rate_limit=1.0,            # 每秒1个请求
    glossary_file="my_glossary.json"  # 自定义词表文件
)
```

## 注意事项

1. **API凭证**：使用前必须提供有效的百度翻译API `App ID` 和 `Secret Key`
2. **使用限制**：百度翻译API有使用次数限制，请合理控制请求频率
3. **网络问题**：组件内置了自动重试机制，可处理网络波动
4. **词表管理**：自定义词表会自动保存到文件，下次启动时会自动加载
5. **日志级别**：默认日志级别为INFO，可通过`logging.basicConfig()`调整

## 示例代码

### 基本翻译

```python
from baidu_translator import NewBaiduTranslator

# 创建翻译器实例（请替换为自己的凭证）
translator = NewBaiduTranslator(
    app_id="你的App ID",
    secret_key="你的Secret Key"
)

# 翻译单个文本
result = translator.translate("Hello world", from_lang="en", to_lang="zh")
print(f"翻译结果: {result}")

# 翻译列表
texts = ["Hello", "How are you?", "Goodbye"]
translated_texts = translator.batch_translate(texts, from_lang="en", to_lang="zh")
print(f"批量翻译结果: {translated_texts}")
```

### 使用翻译词表

```python
from baidu_translator import NewBaiduTranslator

translator = NewBaiduTranslator(
    app_id="你的App ID",
    secret_key="你的Secret Key",
    glossary_file="tech_terms.json"
)

# 添加专业术语翻译对
translator.add_to_glossary("人工智能", "Artificial Intelligence")
translator.add_to_glossary("机器学习", "Machine Learning")

# 使用词表翻译（会优先匹配词表）
result1 = translator.translate("人工智能")
result2 = translator.translate("机器学习")
print(f"词表翻译结果1: {result1}")
print(f"词表翻译结果2: {result2}")
```

## 错误处理

组件会自动处理以下错误：
- API认证错误
- 请求频率过高
- 网络连接错误
- 超时错误
- JSON解析错误

对于致命错误，会返回原文本并记录日志。

## 许可证

本组件基于MIT许可证开源。
