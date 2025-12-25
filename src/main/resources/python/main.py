# -*- coding: utf-8 -*-
"""
main.py - 用户数据分析主入口

支持两种运行模式：
1. 用户模式：基于用户上传目录 (uploads/{username}/) 进行分析
2. 全量模式：执行完整的 01/02/03 流水线

用法：
    python main.py --user <username>        # 分析指定用户的数据
    python main.py --user-dir <dir_path>    # 分析指定目录的数据
    python main.py --pipeline               # 执行完整流水线（01/02/03）
"""

from __future__ import annotations

import argparse
import subprocess
import sys
import glob
import json
from pathlib import Path

import pandas as pd

# 全局变量：是否为 JSON 模式（日志输出到 stderr）
_json_mode = False

def log(msg: str):
    """日志输出函数：JSON模式输出到stderr，否则输出到stdout"""
    if _json_mode:
        print(msg, file=sys.stderr)
    else:
        print(msg)


def analyze_user_data(user_dir: Path, output_dir: Path = None) -> dict:
    """
    分析用户目录下的所有CSV文件
    
    Args:
        user_dir: 用户数据目录路径
        output_dir: 输出目录路径（默认为 user_dir/outputs）
    
    Returns:
        分析结果字典
    """
    if not user_dir.exists():
        return {"success": False, "message": f"Directory not found: {user_dir}"}
    
    # 设置输出目录
    if output_dir is None:
        output_dir = user_dir / "outputs"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 收集所有CSV文件
    csv_files = list(user_dir.glob("*.csv"))
    if not csv_files:
        return {"success": False, "message": "No CSV files in directory"}
    
    # 合并所有CSV数据
    dfs = []
    processed_files = []
    for csv_file in csv_files:
        try:
            # 尝试多种编码
            for enc in ("utf-8-sig", "utf-8", "gb18030", "gbk"):
                try:
                    df = pd.read_csv(csv_file, encoding=enc)
                    dfs.append(df)
                    processed_files.append(csv_file.name)
                    log(f"[OK] {csv_file.name} ({len(df)} records, encoding: {enc})")
                    break
                except UnicodeDecodeError:
                    continue
        except Exception as e:
            log(f"[FAIL] {csv_file.name}: {e}")
    
    if not dfs:
        return {"success": False, "message": "Failed to read any CSV files"}
    
    # 合并数据
    combined_df = pd.concat(dfs, ignore_index=True)
    log(f"\n[Merged] {len(combined_df)} records from {len(processed_files)} files")
    
    # 执行分析（传入输出目录）
    result = perform_analysis(combined_df, output_dir)
    result["processed_files"] = processed_files
    result["total_files"] = len(processed_files)
    result["output_dir"] = str(output_dir)
    result["success"] = True
    
    # 将结果写入文件，供 Java 读取
    result_file = output_dir / "analysis_result.json"
    try:
        with open(result_file, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        result["result_file"] = str(result_file)
        log(f"\n[Saved] {result_file}")
    except Exception as e:
        log(f"[WARN] Failed to save result: {e}")
    
    return result


def perform_analysis(df: pd.DataFrame, output_dir: Path = None) -> dict:
    """
    执行数据分析
    
    Args:
        df: 合并后的DataFrame
        output_dir: 输出目录（用于保存各指标的结果文件）
    
    Returns:
        分析结果
    """
    result = {
        "total_records": len(df),
        "columns": list(df.columns),
    }
    
    # 基础统计
    if "journal" in df.columns:
        journal_counts = df["journal"].value_counts()
        result["journal_count"] = len(journal_counts)
        result["top_journals"] = journal_counts.head(10).to_dict()
    
    if "keywords" in df.columns:
        # 统计关键词
        result["has_keywords"] = int(df["keywords"].notna().sum())
    
    if "publish_date" in df.columns or "year" in df.columns:
        year_col = "year" if "year" in df.columns else "publish_date"
        try:
            years = pd.to_numeric(df[year_col], errors="coerce").dropna()
            if len(years) > 0:
                result["year_range"] = {
                    "min": int(years.min()),
                    "max": int(years.max())
                }
        except Exception:
            pass
    
    if "citations" in df.columns:
        try:
            # 尝试提取引用数量
            result["has_citations"] = int(df["citations"].notna().sum())
        except Exception:
            pass
    
    # 尝试调用指标计算模块（如果可用）
    try:
        from disrupt_calculator_031 import analyze_disruption
        from interdisciplinary_032 import analyze_interdisciplinary
        from novelty_analyzer_033 import analyze_journal_novelty
        from topic_analyzer_036 import analyze_topic_entropy
        from theme_034 import ThemeHotnessAnalyzer
        
        log("\n[Metrics Calculation]")
        
        # 创建输出子目录
        if output_dir:
            disrupt_out = output_dir / "disrupt"
            inter_out = output_dir / "interdisciplinary"
            novelty_out = output_dir / "novelty"
            topic_out = output_dir / "topic"
            theme_out = output_dir / "theme"
            for d in [disrupt_out, inter_out, novelty_out, topic_out, theme_out]:
                d.mkdir(parents=True, exist_ok=True)
        
        try:
            log("  Calculating disruption index...")
            disrupt_df = analyze_disruption(df)
            result["disruption"] = disrupt_df.head(10).to_dict(orient="records")
            if output_dir:
                disrupt_df.to_csv(disrupt_out / "disruption.csv", index=False)
                result["disruption_file"] = str(disrupt_out / "disruption.csv")
        except Exception as e:
            log(f"  Disruption index failed: {e}")
        
        try:
            log("  Calculating interdisciplinarity...")
            inter_df = analyze_interdisciplinary(df)
            result["interdisciplinary"] = inter_df.head(10).to_dict(orient="records")
            if output_dir:
                inter_df.to_csv(inter_out / "interdisciplinary.csv", index=False)
                result["interdisciplinary_file"] = str(inter_out / "interdisciplinary.csv")
        except Exception as e:
            log(f"  Interdisciplinarity failed: {e}")
        
        try:
            log("  Calculating novelty...")
            novelty_df = analyze_journal_novelty(df)
            result["novelty"] = novelty_df.head(10).to_dict(orient="records")
            if output_dir:
                novelty_df.to_csv(novelty_out / "novelty.csv", index=False)
                result["novelty_file"] = str(novelty_out / "novelty.csv")
        except Exception as e:
            log(f"  Novelty failed: {e}")
        
        try:
            log("  Calculating topic complexity...")
            topic_df = analyze_topic_entropy(df)
            result["topic"] = topic_df.head(10).to_dict(orient="records")
            if output_dir:
                topic_df.to_csv(topic_out / "topic.csv", index=False)
                result["topic_file"] = str(topic_out / "topic.csv")
        except Exception as e:
            log(f"  Topic complexity failed: {e}")
        
        try:
            log("  Calculating theme hotness...")
            theme_analyzer = ThemeHotnessAnalyzer(df)
            theme_df = theme_analyzer.run(top_n=10)
            result["theme"] = theme_df.to_dict(orient="records")
            if output_dir:
                theme_df.to_csv(theme_out / "theme.csv", index=False)
                result["theme_file"] = str(theme_out / "theme.csv")
        except Exception as e:
            log(f"  Theme hotness failed: {e}")
        
    except ImportError as e:
        log(f"[INFO] Metrics modules not found: {e}")
    
    return result


def _run_step(args: list[str], *, cwd: Path, name: str) -> None:
    """运行子脚本"""
    log(f"\n========== RUN {name} ==========")
    log(" ".join(args))
    proc = subprocess.run(args, cwd=str(cwd))
    if proc.returncode != 0:
        raise SystemExit(f"步骤 {name} 失败，退出码: {proc.returncode}")


def run_pipeline(base_dir: Path, clean_config: str, metrics_db_config: str, year: int | None) -> None:
    """执行完整的 01/02/03 流水线"""
    py = sys.executable

    # 01：upload_01.py
    _run_step([py, "upload_01.py"], cwd=base_dir, name="01 upload_01")

    # 02：translate_keywords_02.py
    _run_step([py, "translate_keywords_02.py", "--config", clean_config], cwd=base_dir, name="02 translate_keywords_02")

    # 03：main_03.py
    cmd3 = [py, "main_03.py", "--clean-config", clean_config, "--metrics-db-config", metrics_db_config]
    if year:
        cmd3 += ["--year", str(year)]
    _run_step(cmd3, cwd=base_dir, name="03 main_03")

    log("\n========== ALL DONE ==========")


def main() -> None:
    base_dir = Path(__file__).resolve().parent
    # 项目根目录（uploads在项目根目录下）
    project_root = base_dir.parent.parent.parent.parent

    parser = argparse.ArgumentParser(description="用户数据分析 / 全量流水线")
    
    # 用户模式参数
    parser.add_argument("--user", type=str, help="用户名，分析 uploads/{username}/ 目录下的数据")
    parser.add_argument("--user-dir", type=str, help="直接指定用户数据目录路径")
    
    # 流水线模式参数
    parser.add_argument("--pipeline", action="store_true", help="执行完整的 01/02/03 流水线")
    parser.add_argument(
        "--clean-config",
        default=str(base_dir / "config" / "clean_config.yaml"),
        help="clean_config.yaml 路径（供 02/03 使用）",
    )
    parser.add_argument(
        "--metrics-db-config",
        default=str(base_dir / "config" / "metrics_db.json"),
        help="指标上传数据库配置 JSON（供 03 使用）",
    )
    parser.add_argument("--year", type=int, default=None, help="指标年份（可选）")
    
    # 输出参数
    parser.add_argument("--output", type=str, help="输出结果到JSON文件")
    parser.add_argument("--json", action="store_true", help="以JSON格式输出结果（用于Java调用）")
    
    args = parser.parse_args()

    # 设置 JSON 模式全局变量（日志输出到 stderr）
    global _json_mode
    _json_mode = args.json

    # 模式选择
    if args.user:
        # 用户模式：分析 uploads/{username}/ 目录
        user_dir = project_root / "uploads" / args.user
        log(f"[User Mode] Dir: {user_dir}")
        result = analyze_user_data(user_dir)
        
    elif args.user_dir:
        # 直接指定目录
        user_dir = Path(args.user_dir)
        log(f"[Dir Mode] Dir: {user_dir}")
        result = analyze_user_data(user_dir)
        
    elif args.pipeline:
        # 流水线模式
        log("[Pipeline Mode] Running 01/02/03")
        run_pipeline(base_dir, args.clean_config, args.metrics_db_config, args.year)
        return
        
    else:
        # 默认：显示帮助
        parser.print_help()
        print("\n示例:")
        print("  python main.py --user admin        # 分析 admin 用户的数据")
        print("  python main.py --user-dir ./data   # 分析指定目录")
        print("  python main.py --pipeline          # 执行完整流水线")
        return
    
    # 输出结果
    if args.json:
        # JSON 模式：单行输出，不带缩进，方便 Java 解析
        print(json.dumps(result, ensure_ascii=False))
    elif args.output:
        output_path = Path(args.output)
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        log(f"\n[Output] Saved to: {output_path}")
    else:
        log("\n========== Analysis Result ==========")
        log(f"Success: {result.get('success', False)}")
        log(f"Total records: {result.get('total_records', 0)}")
        log(f"Files processed: {result.get('total_files', 0)}")
        if "journal_count" in result:
            log(f"Journal count: {result['journal_count']}")
        if "year_range" in result:
            log(f"Year range: {result['year_range']['min']} - {result['year_range']['max']}")


if __name__ == "__main__":
    main()
