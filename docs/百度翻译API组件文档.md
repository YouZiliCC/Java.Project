# 百度翻译API组件文档

## 1. 组件概述

`baidu_translator.py`是一个新设计的百度翻译API组件，遵循百度翻译API v3最新规范，提供更可靠、高效的翻译服务。该组件封装了百度翻译API的核心功能，并提供了丰富的扩展特性，如词表管理、请求频率控制、错误重试、结果缓存等。

## 2. 核心类与功能

### 2.1 `NewBaiduTranslator`类

这是组件的核心类，提供了所有翻译相关的功能：

```python
class NewBaiduTranslator:
    # 百度翻译API v3版本的基础URL
    BASE_URL_V3 = "https://fanyi-api.baidu.com/api/trans/vip/translate"
    
    # 支持的语言列表
    SUPPORTED_LANGUAGES = {
        'auto': '自动检测',
        'zh': '中文',
        'en': '英语',
        'ja': '日语',
        'ko': '韩语',
        'fr': '法语',
        'de': '德语',
        'ru': '俄语',
        'es': '西班牙语',
        'pt': '葡萄牙语',
        'it': '意大利语'
    }
    
    def __init__(self, app_id: str = None, secret_key: str = None, **kwargs):
        # 初始化方法
        pass
```

### 2.2 核心功能方法

#### 2.2.1 初始化配置
```python
def __init__(self, app_id: str = None, secret_key: str = None, **kwargs):
    # API凭证设置
    # 参数验证
    # 配置参数设置（超时时间、重试次数、请求频率等）
    pass
```

#### 2.2.2 单个文本翻译
```python
def translate(self, text: str, from_lang: str = 'auto', to_lang: str = 'zh', add_to_glossary: bool = False) -> str:
    # 翻译单个文本
    # 支持自动检测源语言
    # 支持将结果添加到词表
    pass
```

#### 2.2.3 批量文本翻译
```python
def batch_translate(self, texts: List[str], from_lang: str = 'auto', to_lang: str = 'zh', add_to_glossary: bool = False) -> List[str]:
    # 批量翻译文本列表
    # 支持分批处理
    # 显示翻译进度
    pass
```

#### 2.2.4 词表管理
```python
def add_to_glossary(self, source_text: str, translated_text: str, from_lang: str = 'zh', to_lang: str = 'en'):
    # 添加翻译对到词表
    pass

def remove_from_glossary(self, source_text: str, from_lang: str = 'zh', to_lang: str = 'en'):
    # 从词表中移除翻译对
    pass

def clear_glossary(self):
    # 清空翻译词表
    pass

def import_glossary(self, glossary_file: str, overwrite: bool = False) -> Dict:
    # 从外部文件导入词表
    pass

def export_glossary(self, export_file: str) -> bool:
    # 导出词表到外部文件
    pass
```

## 3. 主要特性

### 3.1 词表优先策略
- 翻译前先检查词表，命中词表直接返回结果
- 支持自定义词表导入/导出
- 自动保存新增翻译对到词表

### 3.2 可靠的错误处理
- 指数退避重试机制（最多5次重试）
- 针对不同错误类型的智能处理策略
- 对致命错误（如认证错误）直接返回原文本

### 3.3 性能优化
- 请求频率控制（默认每2秒1个请求）
- 批量翻译支持（默认批次大小为10）
- 网络超时自动重试

### 3.4 详细的日志记录
- 支持不同级别的日志输出
- 记录翻译统计信息（词表命中数、错误数等）
- 显示翻译进度和状态

## 4. 使用示例

### 4.1 基本使用

```python
from baidu_translator import NewBaiduTranslator

# 创建翻译器实例
translator = NewBaiduTranslator()

# 翻译单个文本
result = translator.translate("Hello, world!", from_lang="en", to_lang="zh")
print(result)  # 输出: "你好，世界！"

# 翻译中文到英文
result = translator.translate("你好，世界！", from_lang="zh", to_lang="en")
print(result)  # 输出: "Hello, world!"
```

### 4.2 批量翻译

```python
# 批量翻译文本列表
texts = ["Hello", "World", "Python", "Translation"]
translated_texts = translator.batch_translate(texts, from_lang="en", to_lang="zh")
print(translated_texts)  # 输出: ['你好', '世界', 'Python', '翻译']
```

### 4.3 词表管理

```python
# 添加翻译对到词表
translator.add_to_glossary("人工智能", "Artificial Intelligence")
translator.add_to_glossary("机器学习", "Machine Learning")

# 翻译时会优先使用词表
result = translator.translate("人工智能")
print(result)  # 输出: "Artificial Intelligence"

# 导出词表
translator.export_glossary("my_glossary.json")

# 导入词表
translator.import_glossary("another_glossary.json")
```

### 4.4 自定义配置

```python
# 自定义配置创建翻译器
translator = NewBaiduTranslator(
    app_id="your_app_id",
    secret_key="your_secret_key",
    timeout=60,  # 超时时间60秒
    max_retries=10,  # 最多重试10次
    retry_delay=10,  # 重试延迟10秒
    rate_limit=0.2,  # 每秒最多0.2个请求（每5秒1个请求）
    glossary_file="my_custom_glossary.json"  # 自定义词表文件
)
```

### 4.5 翻译统计

```python
# 打印翻译统计信息
translator.print_stats()

# 获取翻译统计信息stats = translator.get_stats()
print(stats)
# 输出: {'total_translations': 10, 'glossary_hits': 3, 'error_count': 0, 'glossary_size': 5}
```

## 5. 配置说明

### 5.1 API凭证配置

可以通过以下三种方式配置API凭证：

1. **构造函数参数**：
   ```python
   translator = NewBaiduTranslator(app_id="your_app_id", secret_key="your_secret_key")
   ```

2. **环境变量**：
   ```bash
   export BAIDU_APP_ID="your_app_id"
   export BAIDU_SECRET_KEY="your_secret_key"
   ```

3. **默认值**：
   组件内置了默认的API凭证（仅供测试使用）

### 5.2 核心配置参数

| 参数名 | 默认值 | 说明 |
|--------|--------|------|
| timeout | 30 | 请求超时时间（秒） |
| max_retries | 5 | 最大重试次数 |
| retry_delay | 5 | 重试间隔（秒） |
| batch_size | 10 | 批量翻译的批次大小 |
| rate_limit | 0.5 | 每秒最大请求数（默认每2秒1个请求） |
| glossary_file | translation_glossary.json | 词表文件路径 |

## 6. 错误处理机制

### 6.1 API错误码处理

组件会自动处理百度翻译API返回的错误码：

- **52003**：认证错误 - 直接返回原文本
- **54001**：签名错误 - 直接返回原文本
- **54000**：请求参数错误 - 直接返回原文本
- **58001**：语言不支持 - 直接返回原文本
- **54003**：请求频率过高 - 增加等待时间并重试
- **54005**：额度限制 - 增加等待时间并重试

### 6.2 网络错误处理

- **Timeout**：网络超时 - 重试
- **ConnectionError**：网络连接错误 - 重试
- **RequestException**：其他网络请求错误 - 重试

### 6.3 指数退避策略

重试间隔采用指数退避策略：
```python
wait_time = min((2 ** attempt) * self.retry_delay, 60)  # 最大延迟不超过60秒
```

## 7. 性能优化

### 7.1 请求频率控制

组件内置了请求频率控制机制，避免触发百度翻译API的频率限制：

```python
def _check_rate_limit(self):
    # 计算平均请求间隔时间
    avg_interval = 1.0 / self.rate_limit if self.rate_limit > 0 else 1.0
    
    # 如果两次请求间隔小于平均间隔时间，等待
    if time_elapsed < avg_interval:
        wait_time = avg_interval - time_elapsed
        time.sleep(wait_time)
```

### 7.2 批量处理

批量翻译功能支持将大量文本分成小批次处理，提高翻译效率：

```python
for i in range(0, len(texts), self.batch_size):
    batch = texts[i:i + self.batch_size]
    # 翻译批次
    batch_translated = [self.translate(text, from_lang, to_lang, add_to_glossary) for text in batch]
    translated_texts.extend(batch_translated)
```

## 8. 日志与调试

### 8.1 日志配置

组件默认配置了详细的日志输出：

```python
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
```

### 8.2 日志级别

- **INFO**：显示翻译进度、统计信息等
- **DEBUG**：显示详细的请求信息、等待时间等
- **WARNING**：显示警告信息、重试信息等
- **ERROR**：显示错误信息

## 9. 应用场景

### 9.1 关键词翻译

适合批量翻译学术论文关键词、商品标签等：

```python
keywords = ["人工智能", "机器学习", "深度学习", "自然语言处理"]
translated_keywords = translator.batch_translate(keywords, from_lang="zh", to_lang="en")
```

### 9.2 文本内容翻译

适合翻译文章摘要、产品描述等：

```python
summary = "这是一篇关于人工智能应用的文章摘要。"
translated_summary = translator.translate(summary, from_lang="zh", to_lang="en")
```

### 9.3 多语言支持

支持多种语言之间的互译：

```python
# 中文 -> 日语
translated_text = translator.translate("你好", from_lang="zh", to_lang="ja")

# 英语 -> 法语
translated_text = translator.translate("Hello", from_lang="en", to_lang="fr")
```

## 10. 注意事项

1. **API凭证安全**：请妥善保管您的百度翻译API凭证，避免泄露
2. **请求频率限制**：百度翻译API有请求频率限制，请合理设置`rate_limit`参数
3. **词表管理**：定期备份词表文件，避免数据丢失
4. **错误处理**：在生产环境中，请根据实际需求调整错误处理策略
5. **性能优化**：对于大量文本翻译，建议使用批量翻译功能

## 11. 总结

`baidu_translator.py`是一个功能强大、可靠高效的百度翻译API封装组件，提供了丰富的功能特性和灵活的配置选项。它支持词表优先翻译、批量翻译、错误重试、请求频率控制等功能，适合各种翻译场景的应用开发。

通过合理使用该组件，可以大大提高翻译效率和质量，同时降低开发难度和维护成本。