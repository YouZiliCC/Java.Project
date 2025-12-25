# python_sum 工程脚本总览

## 1. 总体调用关系（入口链路 + 数据流向）

### 1.1 一键入口（01 → 02 → 03）

- `main.py` 通过 `subprocess` 顺序执行：
  1) `upload_01.py`
  2) `translate_keywords_02.py --config config/clean_config.yaml`
  3) `main_03.py --clean-config config/clean_config.yaml --metrics-db-config config/metrics_db.json [--year YYYY]`

### 1.2 数据流（表/文件）

| 步骤 | 脚本 | 主要输入 | 主要输出 |
|---|---|---|---|
| 01 | `upload_01.py` | Excel目录（`clean_config.yaml:data_source.excel_dir`）或回退 CSV（工程内 `data/cleaned/*.csv`） | MySQL 表 `papers`（逐行插入，按 DOI/Title 去重） |
| 02 | `translate_keywords_02.py` | MySQL 表（`clean_config.yaml:data_source.database.table`，通常 `papers`） + 百度翻译 | 生成 `citing`（英文 DOI / 中文题名）+ 翻译后的 `keywords`；写回 MySQL 表 `cleaned`（由 `clean_config.yaml:write_back` 控制） + 输出 CSV 到 `data/cleaned/` |
| 03 | `main_03.py` | MySQL 表 `cleaned`（强制使用 `clean_config.yaml:write_back.table` 作为读取表） | 计算 5 个指标 + 写入 MySQL 表 `journal_metrics`（连接信息来自 `metrics_db.json`，可由 `--year` 覆盖年份） |

### 1.3 03 内部调用关系（指标计算）

- `main_03.py`
  - `WosDataCleaner.clean(source_type="database")`（读取并生成 `citing`）
  - 指标计算：
    - `disrupt_calculator_031.analyze_disruption(df)`
    - `interdisciplinary_032.analyze_interdisciplinary(df)`
    - `novelty_analyzer_033.analyze_journal_novelty(df)`
    - `topic_analyzer_036.analyze_topic_entropy(df)`
    - `theme_034.ThemeHotnessAnalyzer(df).run(top_n=None)`
  - 指标落库：`upload_journal_metrics_035.upload_journal_metrics(...)`

---

## 2. 逐文件说明（功能 / 输入 / 输出）

> 说明口径：这里的“输入/输出”描述以脚本实际读写行为为准（命令行参数、配置文件、数据库表、生成文件）。

### 2.1 `main.py`

- 功能
  - 一键串联执行 01→02→03，不做任何数据处理。
- 输入
  - CLI：
    - `--clean-config`：传给 02/03（默认 `config/clean_config.yaml`）
    - `--metrics-db-config`：传给 03（默认 `config/metrics_db.json`）
    - `--year`：传给 03（可选）
- 输出
  - 控制台打印每步命令与失败码；不直接产出数据文件/数据库写入（写入由各步骤脚本完成）。

### 2.2 `upload_01.py`

- 功能
  - 读取 Excel 目录（或回退 CSV），按 `field_mapping` 统一字段名，然后写入 MySQL 的 `papers` 表。
  - 具有去重策略：
    - DOI 非空：按 DOI 去重
    - DOI 为空：按 Title 去重
    - publish_date 无法解析：跳过
- 输入
  - 配置：`config/clean_config.yaml`
    - `data_source.excel_dir`：Excel 文件夹（存在则优先读）
    - `field_mapping`：源列名 → 标准列名（doi/journal/keywords/publish_date/target/citations/title/abstract/category 等）
    - `data_source.database`：数据库连接参数（注意：脚本实际写死插入表为 `papers`，并且查询计数也使用 `papers`）
  - 数据：
    - Excel：`*.xls*`（含 `.xls` 失败时会回退 `pd.read_html` 以兼容 CNKI 的“伪 xls”）
    - CSV 回退：优先 `data/cleaned/all_data.csv` 等
- 输出
  - MySQL：表 `papers` 插入新记录
  - 控制台：输出映射结果、插入统计、跳过原因统计

### 2.3 `translate_keywords_02.py`

- 功能
  - 从数据库/Excel 读取数据，执行“对齐 Java 字段”的清洗：
    - 字段映射与保留必要列
    - 由 `citations` 生成 `citing`：英文优先提取 DOI，中文优先提取题名（并做去噪）
    - 关键词清洗 + 翻译（优先词表命中，缺失时调用百度翻译并回写词表）
  - 可选把清洗结果写回数据库（`write_back.enabled`）。
- 输入
  - CLI：`--config / -c` 指向 `clean_config.yaml`
  - 配置：
    - `config/clean_config.yaml`
      - `data_source.type`：`excel` 或 `database`
      - `data_source.database.table`：数据来源表（通常 `papers`）
      - `field_mapping`：字段映射
      - `cleaning`：drop_conference/min_year/target_journals/top_n 等
      - `write_back`：写回开关、数据库连接、目标表名、if_exists、chunksize
    - `config/config.json`：用于推导“哪些列是需要的列”（`get_needed_columns_from_configs`）
  - 词表文件（如果存在会被优先选用）：
    - `keyword_glossary_clean.json` / `keyword_glossary.json` / `rebuilt_keyword_glossary.json`
  - 外部服务：百度翻译 API（由 `baidu_translator_021.py` 封装）
- 输出
  - 文件：
    - `data/cleaned/alldata_cleaned.csv`
    - `data/cleaned/targetdata_cleaned.csv`
  - 数据库（可选）：
    - 按 `write_back` 写入目标表（代码中会把历史遗留表名映射为 `cleaned`）
  - 返回值：作为模块调用时 `WOSCleanService.run()` 返回 `(df, all_path, target_path)`

### 2.4 `preprocess_wos_data_030.py`

- 功能
  - 提供可复用的清洗器 `WosDataCleaner`（03 使用它从 DB 读数据并生成 `citing`）。
  - 支持 Excel/CSV/Database 三种来源。
  - 关键逻辑：`extract_dois_from_references` —— 英文提 DOI、中文提题名（并尽量兼容“编号串联”的中文参考文献）。
- 输入
  - 构造参数：`WosDataCleaner(clean_config=...)`（可外部传入配置；不传则用文件内 `CLEAN_CONFIG`）
  - 数据源：
    - DB：`clean_config.data_source.database`（默认表 `cleaned`）
- 输出
  - 返回 `DataFrame`（`clean()` 的返回值）
  - 若直接运行脚本：会输出 `cleaned_data.csv` 到当前目录（示例用法）

### 2.5 `main_03.py`

- 功能
  - 从数据库读取“清洗后的论文数据”，计算 5 个期刊指标并上传到 `journal_metrics`。
- 输入
  - CLI：
    - `--clean-config`：读取 DB 与字段映射信息（默认 `config/clean_config.yaml`）
    - `--metrics-db-config`：指标库连接配置（默认 `config/metrics_db.json`）
    - `--year`：指标年份（可覆盖配置文件）
  - 关键点：03 会强制把读取表设置为 `clean_config.yaml:write_back.table`（即 02 的写回表，通常是 `cleaned`）。
- 输出
  - 数据库：调用 `upload_journal_metrics_035.upload_journal_metrics(...)` 写入表 `journal_metrics`
  - 控制台：输出各指标计算进度

### 2.6 `disrupt_calculator_031.py`

- 功能
  - 计算“期刊颠覆性指数”（论文级 disruption_index → 期刊聚合 enhanced_score → percent_score）。
  - 网络构建策略：引用条目规范化为 token 进网（`doi:` / `title:` / `pid:`），避免“必须匹配样本内论文”导致中文引用丢失。
  - 支持过滤策略：默认把 `disruption_index==0` 视为脏数据，在期刊聚合前剔除（由 `disrupt_config.parameters.exclude_zero_papers` 控制）。
  - 支持诊断输出与“参与聚合论文数”导出。
- 输入
  - DataFrame（至少需要列）：`doi/journal/citing/title/category`（列名可由 `disrupt_config.columns` 映射）
  - 参数：`disrupt_config.parameters`（top_k、volume_weight、诊断开关、动态高频 token 过滤阈值等）
- 输出
  - 返回：期刊级 DataFrame（含 `journal/enhanced_score/percent_score/n_papers` 等）
  - 文件：默认输出到 `outputs/disrupt/participating_paper_counts.csv`
  - 控制台：可选打印建网诊断

### 2.7 `interdisciplinary_032.py`

- 功能
  - 计算跨学科性（TD）：
    - 先基于论文的分类信息构建“分类相似度矩阵”
    - 再按“论文引用的参考文献所属分类”计算 Rao-Stirling 多样性并映射到 TD
    - 期刊聚合输出均值与百分制分数
- 输入
  - DataFrame 必要列（默认）：
    - `doi`（id）
    - `journal`
    - `target`（category）
    - `citing`（refs）
- 输出
  - 返回：期刊级 DataFrame（`td_mean/paper_count/percent_score`）
  - 不写文件、不写数据库（上传由 03 统一完成）

### 2.8 `novelty_analyzer_033.py`

- 功能
  - 计算期刊新颖性（Uzzi et al. 2013 思路的工程化变体）：
    - 在背景集里统计关键词二元组首次出现年份
    - 目标集论文按关键词对“越新越高”的规则求平均
    - 期刊聚合均值并映射百分制
- 输入
  - DataFrame：默认使用列 `journal/keywords/publish_date`
  - `background_df` 与 `target_df`：若不传 target，则 target=background
- 输出
  - 返回：期刊级 DataFrame（`novelty_score/paper_count/percent_score`）

### 2.9 `topic_analyzer_036.py`

- 功能
  - 基于作者关键词（`keywords`）做“研究领域分布”映射，并用香农熵衡量主题/学科跨度：
    - 内置 `FOS_dict`（领域→关键词表）
    - 对每篇论文算 entropy，再期刊聚合均值
    - `percent_score = entropy_mean * 100`
- 输入
  - DataFrame：默认列 `doi/journal/keywords`
- 输出
  - 返回：期刊级 DataFrame（`entropy_mean/avg_field_count/paper_count/percent_score`）

### 2.10 `theme_034.py`

- 功能
  - 计算 2021–2025 五年窗口内：
    - `theme_concentration`：主题集中度（基于关键词分布熵的归一化反向量）
    - `hot_response`：热点响应度（期刊关键词命中“全局前50热词”的比例）
    - 并输出每年 Top-5 关键词列表（`top_keywords_2021`…`top_keywords_2025`）
- 输入
  - DataFrame：需要 `journal`、`keywords`，以及可识别年份列（`year/publish_year/publish_date` 之一）
- 输出
  - 返回：期刊级 DataFrame（包含 `theme_concentration/hot_response/top_keywords_2021..2025`）

### 2.11 `upload_journal_metrics_035.py`

- 功能
  - 把 5 个指标结果合并，并写入 MySQL 表 `journal_metrics`（`INSERT ... ON DUPLICATE KEY UPDATE`）。
  - 支持额外写入：
    - `paper_count`（期刊论文数）
    - `category`（若提供论文级 `papers_df`，取每期刊的主导 category 众数）
    - `top_keywords_2021..2025`（以 JSON 字符串写入）
- 输入
  - 指标 DataFrame：
    - disruption/interdisciplinary/novelty/topic：都要求含 `journal/percent_score`
    - theme：要求含 `journal/theme_concentration/hot_response/top_keywords_2021..2025`
  - `year`：年份
  - `db_config`：数据库连接参数
  - 可选：`paper_count_df`、`papers_df`
- 输出
  - MySQL：写入/更新 `journal_metrics`
  - 控制台：打印写入目标与写入条数

### 2.12 `baidu_translator_021.py`

- 功能
  - 百度翻译 API v3 封装（单条/批量翻译），带：
    - 账号池轮换（支持显式传参、环境变量、内置兜底账号）
    - 失败重试 + 指数退避
    - 本地词表（glossary）命中优先，并可回写 JSON 词表
    - 请求频率限制（`rate_limit`）
- 输入
  - 凭证来源优先级：
    1) 初始化参数 `app_id/secret_key`
    2) 环境变量 `BAIDU_APP_ID/BAIDU_SECRET_KEY`
    3) 文件内内置账号（兜底）
  - `glossary_file`：词表 JSON 路径
- 输出
  - 返回翻译结果（字符串或列表）
  - 本地文件：更新 `glossary_file`（JSON）

---

## 3. 常见“读/写”位置速查

- 输入配置：
  - `config/clean_config.yaml`（01/02/03）
  - `config/metrics_db.json`（03）
  - `config/config.json`（02：用于推导 needed_columns）
- 主要数据库表：
  - `papers`：01 写入；02 读取（通常）
  - `cleaned`：02 写回；03 读取
  - `journal_metrics`：03 写入
- 主要输出目录：
  - `data/cleaned/`：02 输出中间 CSV
  - `outputs/disrupt/`：颠覆性诊断/参与论文数
