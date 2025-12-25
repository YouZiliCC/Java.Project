# -*- coding: utf-8 -*-
"""
novelty_analyzer.py
期刊新颖性计算（Uzzi et al. 2013）
工程版 / 服务版
保证：target_df 中出现的期刊一定有结果（无值则为 0）
"""

import pandas as pd
import numpy as np
import ast
from itertools import combinations
from collections import defaultdict


def _uniform_rank_to_1_100(values: pd.Series, *, tie_breaker: pd.Series | None = None) -> pd.Series:
    """把任意数值序列按排序位置均匀映射到 1~100（仅调整最终分布，不改算法）。"""
    s = pd.to_numeric(values, errors="coerce")
    mask = s.notna()
    if mask.sum() == 0:
        return pd.Series([np.nan] * len(values), index=values.index, dtype=float)

    sub = pd.DataFrame({"v": s[mask].astype(float)})
    if tie_breaker is not None:
        sub["tie"] = tie_breaker[mask].astype(str)
        sub = sub.sort_values(["v", "tie"], ascending=[False, True])
    else:
        sub = sub.sort_values(["v"], ascending=[False])

    n = len(sub)
    if n == 1:
        score = pd.Series([100.0], index=sub.index, dtype=float)
    else:
        ranks = np.arange(1, n + 1, dtype=float)
        score = 100.0 - (ranks - 1.0) * (99.0 / (n - 1.0))
        score = pd.Series(score, index=sub.index, dtype=float)

    out = pd.Series([np.nan] * len(values), index=values.index, dtype=float)
    out.loc[sub.index] = score
    return out

# =========================
# 工具函数
# =========================
def clean_keywords(val):
    """统一清洗关键词"""
    # 检查是否为 None/NaN（处理标量和数组情况）
    if val is None or (isinstance(val, float) and pd.isna(val)):
        return []

    stop_words = {
        'review', 'study', 'analysis', 'method',
        'model', 'approach', 'system', 'research'
    }

    try:
        if isinstance(val, str) and val.startswith('['):
            kws = ast.literal_eval(val)
        elif isinstance(val, (list, set)):
            kws = val
        else:
            kws = str(val).replace(';', ',').split(',')
    except Exception:
        return []

    kws = [
        str(k).strip().lower()
        for k in kws
        if str(k).strip().lower() not in stop_words
        and len(str(k).strip()) > 1
    ]
    return sorted(set(kws))


def parse_year(val):
    try:
        y = int(float(val))
        return y if 1900 <= y <= 2100 else None
    except Exception:
        return None


# =========================
# 核心函数
# =========================
def analyze_journal_novelty(
    background_df: pd.DataFrame,
    target_df: pd.DataFrame = None,
    top_n: int | None = None,
    journal_col: str = "journal",
    keywords_col: str = "keywords",
    year_col: str = "publish_date"
) -> pd.DataFrame:
    """
    期刊新颖性计算（连续值）
    """
    if target_df is None:
        target_df = background_df

    all_journals = target_df[journal_col].dropna().astype(str).unique()

    # 构建关键词组合首次出现年份
    pair_first_year = {}
    for _, row in background_df.iterrows():
        kws_raw = row.get(keywords_col)
        kws = clean_keywords(kws_raw)
        year = parse_year(row.get(year_col))
        if len(kws) < 2 or year is None:
            continue
        for pair in combinations(kws, 2):
            pair = tuple(sorted(pair))
            if pair not in pair_first_year:
                pair_first_year[pair] = year
            else:
                pair_first_year[pair] = min(year, pair_first_year[pair])

    # 当前年份
    valid_years = [parse_year(y) for y in background_df[year_col] if parse_year(y) is not None]
    current_year = max(valid_years) if valid_years else 2025

    # 论文级新颖性
    paper_scores = []
    for _, row in target_df.iterrows():
        kws = clean_keywords(row.get(keywords_col))
        journal = row.get(journal_col, "Unknown")
        if len(kws) < 2:
            paper_scores.append((journal, 0.0))
            continue

        novelty_sum = 0
        count = 0
        for pair in combinations(kws, 2):
            pair = tuple(sorted(pair))
            first_year = pair_first_year.get(pair)
            if first_year is None:
                novelty = 1.0  # 从未出现过
            else:
                delta = current_year - first_year
                novelty = 1.0 / (1.0 + delta)  # 时间越接近，novelty越高
            novelty_sum += novelty
            count += 1
        score = novelty_sum / count if count > 0 else 0.0
        paper_scores.append((journal, score))

    paper_df = pd.DataFrame(paper_scores, columns=["journal", "novelty_score"])

    # 期刊聚合
    journal_df = (
        paper_df.groupby("journal")
        .agg(novelty_score=("novelty_score", "mean"), paper_count=("novelty_score", "count"))
        .reset_index()
    )

    # 补全期刊
    journal_df = pd.DataFrame({"journal": all_journals}).merge(journal_df, on="journal", how="left")
    journal_df["novelty_score"] = journal_df["novelty_score"].fillna(0.0)
    journal_df["paper_count"] = journal_df["paper_count"].fillna(0).astype(int)

    # 百分制映射（raw）
    journal_df["percent_score_raw"] = (journal_df["novelty_score"] * 100).round(2)

    # 均匀化映射：仅对有论文的期刊做 1~100；paper_count==0 保持 0
    journal_df["percent_score"] = 0.0
    valid = journal_df["paper_count"].fillna(0).astype(int) > 0
    journal_df.loc[valid, "percent_score"] = _uniform_rank_to_1_100(
        journal_df.loc[valid, "percent_score_raw"],
        tie_breaker=journal_df.loc[valid, "journal"],
    ).round(2)

    if isinstance(top_n, int) and top_n > 0:
        journal_df = journal_df.head(top_n)

    return journal_df.reset_index(drop=True)

# =========================
# 本地调试
# =========================
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
    csv_path = None
    for p in csv_candidates:
        if p.exists():
            csv_path = p
            break
    if csv_path is None:
        raise FileNotFoundError(f"未找到数据文件，已尝试: {[str(p) for p in csv_candidates]}")
    df = pd.read_csv(csv_path, encoding="utf-8-sig")

    result = analyze_journal_novelty(
        background_df=df,
        target_df=df,
        top_n=None
    )

    print(result)
