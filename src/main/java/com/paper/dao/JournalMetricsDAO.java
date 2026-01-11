package com.paper.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.model.JournalMetrics;
import com.paper.repository.JournalMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 期刊指标数据访问对象 - 使用JPA Repository
 * 更安全、更高效、避免SQL注入
 */
@Component
public class JournalMetricsDAO {
    
    private final JournalMetricsRepository repository;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    public JournalMetricsDAO(JournalMetricsRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 获取所有期刊列表（每个期刊取最新年份）
     */
    public List<JournalMetrics> fetchJournals() {
        return repository.findAllLatestYears();
    }
    
    /**
     * 获取指定期刊的所有年份数据
     */
    public List<JournalMetrics> fetchJournalRows(String journal) {
        return repository.findByJournalOrderByYearDesc(journal);
    }
    
    /**
     * 获取所有期刊名称列表
     */
    public List<String> fetchJournalNames() {
        return repository.findAllJournalNames();
    }
    
    /**
     * 获取所有期刊的最新年份数据
     */
    public List<JournalMetrics> fetchLatestJournalRows() {
        return repository.findAllLatestYears();
    }
    
    /**
     * 获取单个期刊的最新年份数据
     */
    public JournalMetrics fetchLatestRowForJournal(String journal) {
        return repository.findLatestByJournal(journal).orElse(null);
    }
    
    /**
     * 获取两个期刊的最新年份数据
     */
    public Map<String, JournalMetrics> fetchLatestRowsForTwoJournals(
            String journalA, String journalB) {
        List<JournalMetrics> rows = repository.findLatestByTwoJournals(journalA, journalB);
        Map<String, JournalMetrics> result = new HashMap<>();
        for (JournalMetrics row : rows) {
            result.put(row.getJournal(), row);
        }
        return result;
    }
    
    /**
     * 检查期刊是否存在
     */
    public boolean journalExists(String journal) {
        return repository.existsByJournal(journal);
    }
    
    /**
     * 获取期刊总数
     */
    public long getJournalCount() {
        return repository.countDistinctJournals();
    }
    
    /**
     * 解析关键词字段（支持JSON数组、Python字符串表示等）
     */
    public static List<String> parseKeywordsValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String s = value.trim();
        
        // 尝试JSON解析
        try {
            JsonNode node = objectMapper.readTree(s);
            if (node.isArray()) {
                List<String> result = new ArrayList<>();
                for (JsonNode item : node) {
                    String keyword = item.asText().trim();
                    if (!keyword.isEmpty()) {
                        result.add(keyword);
                    }
                }
                return result;
            } else if (node.isObject()) {
                // 如果是对象，按值（频次）降序排列
                List<String> result = new ArrayList<>();
                node.fields().forEachRemaining(entry -> {
                    result.add(entry.getKey());
                });
                return result;
            }
        } catch (Exception e) {
            // 不是有效JSON，继续尝试其他方式
        }
        
        // 兜底：按分隔符拆分
        String[] separators = {";", "；", ",", "，", "|", "/"};
        for (String sep : separators) {
            if (s.contains(sep)) {
                String[] parts = s.split(sep);
                List<String> result = new ArrayList<>();
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
                return result;
            }
        }
        
        return List.of(s);
    }
    
    /**
     * 从最新一行中提取各年份的top_keywords
     */
    public static Map<Integer, List<String>> pickTopKeywords(JournalMetrics latestRow) {
        if (latestRow == null) {
            return new HashMap<>();
        }
        
        Map<Integer, List<String>> result = new HashMap<>();
        result.put(2021, parseKeywordsValue(latestRow.getTopKeywords2021()));
        result.put(2022, parseKeywordsValue(latestRow.getTopKeywords2022()));
        result.put(2023, parseKeywordsValue(latestRow.getTopKeywords2023()));
        result.put(2024, parseKeywordsValue(latestRow.getTopKeywords2024()));
        result.put(2025, parseKeywordsValue(latestRow.getTopKeywords2025()));
        return result;
    }
}
