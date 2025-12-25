# -*- coding: utf-8 -*-
"""
python/interdisciplinary.py

跨学科性（TD）计算核心模块
- 无图表
- 无文件输出
- 配置内置，面向 API / 第三方调用
"""
import ast
import numpy as np
import pandas as pd
from collections import Counter


# ===================== 内置配置 =====================
DEFAULT_CONFIG = {
    "columns": {
        "id": "doi",
        "journal": "journal",
        "category": "target",
        "refs": "citing"
    },
    "parameters": {}
}


# ===================== 核心计算类 =====================
class InterdisciplinaryAnalyzer:
    def __init__(self, config=None):
        self.config = config or DEFAULT_CONFIG
        self.column_config = self.config.get("columns", {})

    # ---------- 工具函数 ----------
    def parse_categories(self, value):
        if pd.isna(value):
            return []

        try:
            if isinstance(value, str):
                value = value.strip()
                if value.startswith("[") and value.endswith("]"):
                    return ast.literal_eval(value)
                for sep in [";", "|", "/"]:
                    if sep in value:
                        return [v.strip() for v in value.split(sep) if v.strip()]
                return [value]
        except Exception:
            pass

        return []

    # ---------- 相似度矩阵 ----------
    def build_similarity_matrix(self, paper_categories: dict):
        all_categories = sorted({c for cats in paper_categories.values() for c in cats})
        n = len(all_categories)
        cat_to_idx = {c: i for i, c in enumerate(all_categories)}
        co_matrix = np.zeros((n, n))

        for cats in paper_categories.values():
            if len(cats) < 2:
                continue
            for i, c1 in enumerate(cats):
                idx1 = cat_to_idx[c1]
                co_matrix[idx1, idx1] += 1
                for c2 in cats[i + 1:]:
                    idx2 = cat_to_idx[c2]
                    co_matrix[idx1, idx2] += 1
                    co_matrix[idx2, idx1] += 1

        sim = np.zeros((n, n))
        for i in range(n):
            for j in range(n):
                if i == j:
                    sim[i, j] = 1.0
                else:
                    denom = np.linalg.norm(co_matrix[i]) * np.linalg.norm(co_matrix[j])
                    sim[i, j] = np.dot(co_matrix[i], co_matrix[j]) / denom if denom > 0 else 0.0

        self.all_categories = all_categories
        self.cat_to_idx = cat_to_idx
        self.similarity_matrix = sim

    # ---------- TD 计算 ----------
    def rao_stirling(self, categories):
        if not categories or len(set(categories)) <= 1:
            return 0.0

        total = len(categories)
        probs = Counter(categories)

        p = np.zeros(len(self.all_categories))
        for cat, cnt in probs.items():
            if cat in self.cat_to_idx:
                p[self.cat_to_idx[cat]] = cnt / total

        diversity = 0.0
        for i in range(len(p)):
            for j in range(len(p)):
                diversity += (1 - self.similarity_matrix[i, j]) * p[i] * p[j]

        return diversity

    def td_index(self, categories):
        d = self.rao_stirling(categories)
        # 平滑压缩：2/(1+d)，将值域压缩到 (0, 2]
        return 2.0 / (1.0 + d) if d > 0 else 1.0


# ===================== 对外统一接口 =====================
def analyze_interdisciplinary(
    df_all: pd.DataFrame,
    *,
    top_n: int | None = None,
    config: dict | None = None
) -> pd.DataFrame:
    """
    跨学科性（TD）分析接口
    完全保留所有期刊，如果论文没有引用或没有数据，td_score 为 0
    """
    if df_all.empty:
        raise ValueError("输入数据为空")

    analyzer = InterdisciplinaryAnalyzer(config)

    # 字段映射
    id_col = analyzer.column_config.get("id", "doi")
    journal_col = analyzer.column_config.get("journal", "journal")
    category_col = analyzer.column_config.get("category", "target")
    refs_col = analyzer.column_config.get("refs", "citing")

    required = {id_col, journal_col, category_col, refs_col}
    missing = required - set(df_all.columns)
    if missing:
        raise ValueError(f"数据缺少列: {missing}")

    # ---------- 构建分类知识库 ----------
    paper_categories = {}
    for _, row in df_all.iterrows():
        pid = str(row[id_col])
        cats = analyzer.parse_categories(row[category_col])
        if cats:
            paper_categories[pid] = cats

    analyzer.build_similarity_matrix(paper_categories)

    # ---------- 论文级 TD ----------
    records = []
    for _, row in df_all.iterrows():
        pid = str(row[id_col])
        journal = row[journal_col]
        refs = row[refs_col]
        try:
            refs = ast.literal_eval(refs) if isinstance(refs, str) else refs
        except Exception:
            refs = []

        ref_cats = []
        for r in refs or []:
            if str(r) in paper_categories:
                ref_cats.extend(paper_categories[str(r)])

        # TD 计算：没有引用或者没有分类也返回 0
        td = analyzer.td_index(ref_cats) if ref_cats else 0.0

        records.append({
            "doi": pid,
            "journal": journal,
            "td_score": td
        })

    paper_df = pd.DataFrame(records)

    # ---------- 期刊聚合 ----------
    # 保证期刊完整
    all_journals = df_all[journal_col].fillna("UNKNOWN").unique()
    journal_df = (
        paper_df
        .groupby("journal")
        .agg(
            td_mean=("td_score", "mean"),
            paper_count=("td_score", "count")
        )
        .reindex(all_journals, fill_value=0)  # 确保每个期刊都有行
        .reset_index()
    )

    # 百分制（线性调节到 0–100）：td_mean ∈ (0,2] → percent ∈ (0,100]
    journal_df["percent_score"] = (journal_df["td_mean"] / 2 * 100).round(1)
    journal_df = journal_df.sort_values("percent_score", ascending=False)

    if isinstance(top_n, int) and top_n > 0:
        journal_df = journal_df.head(top_n)

    return journal_df.reset_index(drop=True)

# ===================== 本地调试示例 =====================
if __name__ == "__main__":
    from pathlib import Path
    base = Path(__file__).parent
    # 优先使用 data 目录下的 CSV
    data_dir = base / "data"
    csv_candidates = [
        data_dir / "alldata_cleaned.csv",
        data_dir / "all_data.csv",
        data_dir / "cleaned_data.csv",
        base / "cleaned_data.csv",
    ]
    data_path = None
    for p in csv_candidates:
        if p.exists():
            data_path = p
            break
    if data_path is None:
        raise FileNotFoundError(f"未找到数据文件，已尝试: {[str(p) for p in csv_candidates]}")
    df = pd.read_csv(data_path, encoding="utf-8-sig")
    result = analyze_interdisciplinary(df)
    print(result)
