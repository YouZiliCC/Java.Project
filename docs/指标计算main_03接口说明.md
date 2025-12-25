# 接口说明

本文档为本仓库中对外可调用的核心接口（函数 / 类）说明，包含输入、输出、异常及示例。建议将调用前的数据用 `preprocess_wos_data.py` 归一化列名与格式，返回dataframe
- 使用示例：
```py
from preprocess_wos_data import WosDataCleaner
cleaner = WosDataCleaner()
# 默认从数据库读取
    df_cleaned = cleaner.clean(source_type='database')
# 如果要读取 Excel 文件夹
    # df_cleaned = cleaner.clean(source_type='excel', path="C:/Users/28623/Downloads/excel文件2")
    # 如果要读取 CSV 文件夹
    # df_cleaned = cleaner.clean(source_type='csv', path="C:/Users/28623/Downloads/csv文件")
```

---
02清洗后标准字段
doi
journal
title
abstract
keywords
publish_date
target
citations

---

## `analyze_disruption(df_all, *, top_n=None, config=None)`
(文件: disrupt_calculator.py)

- 描述：计算论文/期刊级颠覆性指标（Disruption）。
- 输入：
  - `df_all`：pandas.DataFrame;
  - `top_n`：可选 int，返回前 N 个期刊。默认全部

- 输出：pandas.DataFrame（期刊级），包含列：`journal`, `n_papers`, `enhanced_score`, `percent_score`，按 `percent_score` 降序。

  percent_score列作为最终指标
  
- 示例：
```py
from disrupt_calculator import analyze_disruption
result = analyze_disruption(df)
```

---

## `analyze_interdisciplinary(df_all, *, top_n=None, config=None)`
(文件: interdisciplinary.py)

- 描述：计算跨学科（TD）指标并按期刊聚合。
- 输入：
  - `df_all`：pandas.DataFrame，
  - `top_n`:默认全部
- 输出：pandas.DataFrame（期刊级），包含列：`journal`, `td_mean`, `paper_count`, `percent_score`（`td_mean * 2`并四舍五入到一位），按 `percent_score` 降序。

  percent_score列作为最终指标

- 示例：
```py
from interdisciplinary import analyze_interdisciplinary
journal_td = analyze_interdisciplinary(df, top_n=20)
```

---

## `analyze_journal_novelty(background_df, target_df=None, top_n=None, journal_col='journal', keywords_col='keywords', year_col='publish_date')`
(文件: novelty_analyzer.py)

- 描述：基于关键词对首次出现年份计算 Uzzi 风格的组合新颖性，并按期刊聚合。
- 输入：
  - `background_df`：用于构建关键词对首次出现年份的背景数据 DataFrame（必须含关键词和年份列）。
  - `target_df`：可选，若提供则对其计算新颖性；否则使用 `background_df`。
  - `top_n`：可选；
  -。
- 输出：pandas.DataFrame（期刊级），包含：`journal`, `novelty_score`, `paper_count`, `percent_score`（公式为 `novelty_score * 600` ）

  percent_score列作为最终指标

- 示例：
```py
from novelty_analyzer import analyze_journal_novelty
journal_novelty = analyze_journal_novelty(background_df=df, top_n=30)
```

---

## `analyze_topic_entropy(df, top_n=None, *, id_col='doi', journal_col='journal', keywords_col='keywords', abstract_col='abstract', term_source='keywords')`
(文件: topic_analyzer.py)

percent_score列作为最终指标

- 描述：基于内置 FOS 字典计算论文/期刊级主题熵（香农熵），并按期刊聚合为百分制得分。
- 输入：
  - `df`：pandas.DataFrame
  - 从关键词和/或摘要抽取术语并映射到 `FOS_dict`，计算每篇论文的领域分布与熵。

- 输出：pandas.DataFrame（期刊级），包含：`journal`, `entropy_mean`, `avg_field_count`, `paper_count`, `percent_score`（公式`entropy_mean * 100`），按 `percent_score` 降序。
- 示例：
```py
from topic_analyzer import analyze_topic_entropy
topic_df = analyze_topic_entropy(df)
```
---

## `ThemeHotnessAnalyzer` 类
(文件: theme.py)

- 构造：`ThemeHotnessAnalyzer(df, journal_col="journal", keyword_col="keywords")`
  - `df`：pandas.DataFrame，，会过滤得到 2021–2025 年数据；
  - 若无法识别年份或提取失败会抛出 ValueError。

  
- 方法：`run(self, top_n=None)`
  - 返回 pandas.DataFrame，列：`journal`, `theme_concentration`, `hot_response`，‘top_keywords’。
  - `theme_concentration` 与 `hot_response` 均以百分制返回（0..100），`theme_concentration` 保留两位小数，计算基于关键词分布熵的归一化。
  ‘top_keywords’展示每个期刊频率最高5个关键词列表
- 示例：
```py
from theme import ThemeHotnessAnalyzer
analyzer = ThemeHotnessAnalyzer(df)
theme_df = analyzer.run(top_n=None)
```

---







# 其他文件  ：清洗、上传数据库功能

## `main.py` 功能说明

- 描述：提供一个简易的流程驱动脚本，演示如何用仓库中各个分析接口计算期刊指标并上传到数据库（示例输入为 `targetdata.csv`）。
- 行为概要：
  - 从 `targetdata.csv` 读取论文数据（期望包含 `journal`、`keywords`、`abstract`、`publish_date`/`year` 等列）。
  - 依次调用：
    1. `analyze_disruption(df)` 计算颠覆性指标并返回期刊级 `percent_score`。
    2. `analyze_interdisciplinary(df)` 计算跨学科指标。
    3. `analyze_journal_novelty(df)` 计算新颖性指标。
    4. `analyze_topic_entropy(df)` 计算主题熵/复杂度指标。
    5. 使用 `ThemeHotnessAnalyzer(df)` 的 `run()` 方法计算主题集中度与热点响应度。
  - 将上述结果交给 `upload_journal_metrics(...)`，并指定 `year`（示例中为 `2025`）写入数据库。
- 用法示例：
```py
python main.py
```
- 注意：`main.py` 只是示例流水脚本，推荐在生产使用前：确保输入经 `preprocess_wos_data.py` 清洗、对齐列名与数据格式，并在测试数据库中先运行以验证表结构与唯一键约束。

## `01_upload.py` 功能说明

- 描述：把清洗后的文件或文件夹中的 Excel/CSV 数据上传（插入）到原始数据库 `papers` 表。该脚本以 `config/clean_config.yaml` 中的 `field_mapping` 与 `data_source` 配置为依据，执行列映射、去重 DOI 并逐条写入数据库。
- 主要行为：
  - 读取 `config/clean_config.yaml` 获取 `field_mapping` 与 `data_source`（包括 Excel 目录或 CSV 回退路径及数据库连接信息）。
  - 从配置指定的 Excel 目录读取所有 `*.xls*` 文件，使用智能读取器 `read_excel_file`：对 `.xls` 文件遇到 `xlrd` 失败时尝试以 HTML (`pd.read_html`) 回退（适配 CNKI 导出的 HTML-in-.xls 文件）。
  - 使用 `apply_field_mapping` 将源列名严格按规范化（小写/去空格）做精确匹配到目标字段名；合并同一目标的多个源列（从左到右取第一个非空值），并只保留 `field_mapping` 指定的目标列。
  - 在控制台打印最终保留的目标列与其对应源列，以及包含 `keyword` / `关键词` 的候选源列（并标注是否已映射到 `keywords`）。
- 注意事项：
  - 映射仅使用规范化后的精确匹配（严格模式），不会进行模糊包含匹配；若某些重要列未被映射，请在 `config/clean_config.yaml` 中补充候选列名。
  - 插入操作会跳过已存在 DOI 的记录（以预取的 DOI 集合为准），因此对并发插入或外部更新场景需注意可能的竞态条件。
  - 脚本默认将数据写入 `papers` 表；在运行前请确认表结构与字段名与 `field_mapping` 的目标列一致。

---

## `preprocess_wos_data.py` 功能说明

- 模块/类：`WosDataCleaner`（类名）

- 概要描述：
  - 将excel/csv文件夹/原始数据库的论文元数据读取并清洗为分析可用的 pandas.DataFrame。`__main__` 中示例将其保存为 CSV）。




## `upload_journal_metrics.py` 功能说明

- 描述：将多个期刊级指标 DataFrame 合并并写入 MySQL 表 `journal_metrics`。
- 输入参数：
  - `disrupt_df`：`analyze_disruption` 返回的 DataFrame（含 `journal`、`percent_score`）。
  - `interdisciplinary_df`：`analyze_interdisciplinary` 返回的 DataFrame（含 `journal`、`percent_score`）。
  - `novelty_df`：`analyze_journal_novelty` 返回的 DataFrame（含 `journal`、percent_score`）。
  - `topic_df`：`analyze_topic_entropy` 返回的 DataFrame（含 `journal`、`percent_score`）。
  - `theme_df`：`ThemeHotnessAnalyzer.run()` 返回的 DataFrame（含 `journal`、`theme_concentration`、`hot_response`）。
  - `year`：整数年份，用于写入 `year` 列。
  - `db_config`：可选数据库连接配置字典（主机/端口/用户/密码/库/字符集），若不提供使用内置默认值。
- 行为摘要：
  - 将各输入 DataFrame 的 `percent_score` 列重命名为统一字段（`disruption`、`interdisciplinary`、`novelty`、`topic`），并与 `theme_df` 做外连接合并，保证所有期刊被保留。
  - 为合并结果添加 `year` 列。
  - 使用 `pymysql` 建立数据库连接并逐行执行 `INSERT ... ON DUPLICATE KEY UPDATE` 将记录写入 `journal_metrics` 表（主键/唯一键应至少包括 `journal, year` 或根据数据库表定义）。
  - 在执行 SQL 前会把 pandas 的 `NaN` 值转换为 `None` 避免驱动报错。
  - 函数在完成后会提交事务并打印写入条数。
- 注意事项：
  - 该函数会覆盖已存在记录（因为使用了 ON DUPLICATE KEY UPDATE）；若需只在新值非空时更新，请在 SQL 中调整 UPDATE 子句或让我帮你实现 "仅当新值非 NULL 时更新" 的版本。

---

