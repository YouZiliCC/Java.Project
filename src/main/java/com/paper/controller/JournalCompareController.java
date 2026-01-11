package com.paper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.dao.JournalMetricsDAO;
import com.paper.model.JournalMetrics;
import com.paper.model.UserSurvey;
import com.paper.service.JournalService;
import com.paper.utils.AIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 期刊对比和推荐详情控制器
 */
@Controller
@RequestMapping("/journal")
public class JournalCompareController {
    
    private static final String SURVEY_COOKIE_NAME = "pm_survey";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final JournalService service;
    private final AIClient aiClient;
    
    @Autowired
    public JournalCompareController(JournalService service, AIClient aiClient) {
        this.service = service;
        this.aiClient = aiClient;
    }
    
    /**
     * 推荐详情页面
     */
    @GetMapping("/recommend/{journal}")
    public String recommendDetailPage(@PathVariable String journal, 
                                     HttpServletRequest request, Model model) {
        UserSurvey survey = loadSurvey(request);
        if (survey == null) {
            return "redirect:/journal/survey";
        }
        
        try {
            JournalMetrics row = service.dao.fetchLatestRowForJournal(journal);
            if (row == null) {
                model.addAttribute("error", "未找到该期刊");
                return "error";
            }
            
            Map<String, Object> userRadar = service.buildUserRadar(survey);
            Map<String, Object> journalRadar = service.buildRadarFromRow(row);
            double sim = service.computeSimilarity(userRadar, journalRadar);
            
            // 构建叠加雷达图数据
            Map<String, Object> overlay = new HashMap<>();
            overlay.put("labels", userRadar.get("labels"));
            
            Map<String, Object> aData = new HashMap<>();
            aData.put("name", "你的画像");
            aData.put("values", userRadar.get("values"));
            overlay.put("a", aData);
            
            Map<String, Object> bData = new HashMap<>();
            bData.put("name", journal);
            bData.put("values", journalRadar.get("values"));
            overlay.put("b", bData);
            
            overlay.put("max", 200);
            
            // 关键词命中
            int targetYear = LocalDateTime.now().getYear();
            List<String> jKwYear = service.pickKeywordsForYear(row, targetYear);
            Set<String> jKwNorm = jKwYear.stream()
                .map(service::normalizeKeyword)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
            
            List<String> matchedUserKeywords = survey.getKeywords().stream()
                .filter(k -> jKwNorm.contains(service.normalizeKeyword(k)))
                .collect(Collectors.toList());
            
            // 维度差异
            @SuppressWarnings("unchecked")
            List<Double> uVals = (List<Double>) userRadar.get("values");
            @SuppressWarnings("unchecked")
            List<Double> jVals = (List<Double>) journalRadar.get("values");
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) userRadar.get("labels");
            
            double[] ranges = {200.0, 100.0, 100.0, 100.0, 100.0};
            List<Map<String, Object>> diffs = new ArrayList<>();
            
            for (int i = 0; i < labels.size(); i++) {
                double uv = uVals.get(i);
                double jv = jVals.get(i);
                double gap = Math.abs(uv - jv);
                int dimMatch = (int) Math.round((1.0 - (gap / ranges[i])) * 100);
                dimMatch = Math.max(0, dimMatch);
                
                Map<String, Object> diff = new HashMap<>();
                diff.put("dim", labels.get(i));
                diff.put("user", uv);
                diff.put("journal", jv);
                diff.put("gap", gap);
                diff.put("match", dimMatch);
                diffs.add(diff);
            }
            
            model.addAttribute("survey", survey);
            model.addAttribute("journal", journal);
            model.addAttribute("row", row);
            model.addAttribute("targetYear", targetYear);
            model.addAttribute("matchedUserKeywords", matchedUserKeywords);
            model.addAttribute("overlayJson", objectMapper.writeValueAsString(overlay));
            model.addAttribute("userComments", service.buildCommentsFromRow(
                convertSurveyToMetrics(survey)));
            model.addAttribute("journalComments", service.buildCommentsFromRow(row));
            model.addAttribute("matchPct", (int) Math.round(sim * 100));
            model.addAttribute("diffs", diffs);
            
            return "journal/recommend_detail";
        } catch (Exception e) {
            model.addAttribute("error", "加载详情失败: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * 推荐详情AI分析（异步接口）
     */
    @PostMapping("/recommend/{journal}/ai-analysis")
    @ResponseBody
    public Map<String, Object> recommendAIAnalysis(@PathVariable String journal, 
                                                   HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        UserSurvey survey = loadSurvey(request);
        if (survey == null) {
            result.put("error", "未填写问卷，无法进行 AI 匹配分析");
            return result;
        }
        
        try {
            JournalMetrics row = service.dao.fetchLatestRowForJournal(journal);
            if (row == null) {
                result.put("error", "未找到该期刊");
                return result;
            }
            
            Map<String, Object> userRadar = service.buildUserRadar(survey);
            Map<String, Object> journalRadar = service.buildRadarFromRow(row);
            double sim = service.computeSimilarity(userRadar, journalRadar);
            
            int targetYear = LocalDateTime.now().getYear();
            List<String> jKwYear = service.pickKeywordsForYear(row, targetYear);
            Set<String> jKwNorm = jKwYear.stream()
                .map(service::normalizeKeyword)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
            
            List<String> matchedUserKeywords = survey.getKeywords().stream()
                .filter(k -> jKwNorm.contains(service.normalizeKeyword(k)))
                .collect(Collectors.toList());
            
            // 构建维度差异
            @SuppressWarnings("unchecked")
            List<Double> uVals = (List<Double>) userRadar.get("values");
            @SuppressWarnings("unchecked")
            List<Double> jVals = (List<Double>) journalRadar.get("values");
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) userRadar.get("labels");
            
            double[] ranges = {200.0, 100.0, 100.0, 100.0, 100.0};
            List<Map<String, Object>> diffs = new ArrayList<>();
            
            for (int i = 0; i < labels.size(); i++) {
                Map<String, Object> diff = new HashMap<>();
                diff.put("dim", labels.get(i));
                diff.put("user", uVals.get(i));
                diff.put("journal", jVals.get(i));
                diff.put("gap", Math.abs(uVals.get(i) - jVals.get(i)));
                int dimMatch = (int) Math.round((1.0 - (Math.abs(uVals.get(i) - jVals.get(i)) / ranges[i])) * 100);
                diff.put("match", Math.max(0, dimMatch));
                diffs.add(diff);
            }
            
            // 构建AI请求payload
            Map<String, Object> payload = new HashMap<>();
            
            Map<String, Object> surveyData = new HashMap<>();
            surveyData.put("keywords", survey.getKeywords());
            Map<String, Double> scores = new HashMap<>();
            scores.put("novelty", survey.getNovelty());
            scores.put("disruption", survey.getDisruption());
            scores.put("interdisciplinary", survey.getInterdisciplinary());
            scores.put("theme_concentration", survey.getThemeConcentration());
            scores.put("topic", survey.getTopic());
            scores.put("hot_response", survey.getHotResponse());
            surveyData.put("scores", scores);
            surveyData.put("created_at", survey.getCreatedAt());
            payload.put("survey", surveyData);
            
            Map<String, Object> authorProfile = new HashMap<>();
            authorProfile.put("radar", userRadar);
            authorProfile.put("rule_based_comments", service.buildCommentsFromRow(
                convertSurveyToMetrics(survey)));
            payload.put("author_profile", authorProfile);
            
            Map<String, Object> journalData = new HashMap<>();
            journalData.put("name", journal);
            journalData.put("latest_year", row.getYear());
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("disruption", row.getDisruption());
            metrics.put("novelty", row.getNovelty());
            metrics.put("interdisciplinary", row.getInterdisciplinary());
            metrics.put("theme_concentration", row.getThemeConcentration());
            metrics.put("topic", row.getTopic());
            metrics.put("hot_response", row.getHotResponse());
            metrics.put("paper_count", row.getPaperCount());
            metrics.put("category", row.getCategory());
            journalData.put("metrics", metrics);
            
            journalData.put("radar", journalRadar);
            journalData.put("rule_based_comments", service.buildCommentsFromRow(row));
            journalData.put("top_keywords_2021_2025", JournalMetricsDAO.pickTopKeywords(row));
            journalData.put("target_year", targetYear);
            journalData.put("target_year_keywords", jKwYear);
            payload.put("journal", journalData);
            
            Map<String, Object> match = new HashMap<>();
            match.put("overall_match_pct", (int) Math.round(sim * 100));
            match.put("matched_keywords", matchedUserKeywords);
            match.put("dimension_diffs", diffs);
            payload.put("match", match);
            
            String systemPrompt = aiClient.loadRecommendMatchPrompt();
            String userPrompt = "请基于下面 JSON 输入，对作者画像与该期刊的适配性做一次分析，并给出投稿建议。\n" +
                "注意：不得编造不存在的字段或数据。\n\n" +
                objectMapper.writeValueAsString(payload);
            
            String analysis = aiClient.callChatCompletion(systemPrompt, userPrompt);
            
            result.put("journal", journal);
            result.put("year", row.getYear());
            result.put("analysis", analysis);
            result.put("used_model", aiClient.getConfig().get("model"));
            result.put("match_pct", (int) Math.round(sim * 100));
            
        } catch (Exception e) {
            result.put("error", "AI 匹配分析失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 期刊对比页面
     */
    @GetMapping("/compare")
    public String comparePage(
            @RequestParam(required = false) String j1,
            @RequestParam(required = false) String j2,
            Model model) {
        
        try {
            List<String> allJournals = service.dao.fetchJournalNames();
            model.addAttribute("journals", allJournals);
            model.addAttribute("j1", j1 != null ? j1 : "");
            model.addAttribute("j2", j2 != null ? j2 : "");
            
            if (j1 != null && j2 != null && !j1.isEmpty() && !j2.isEmpty()) {
                Map<String, JournalMetrics> rowMap = service.dao.fetchLatestRowsForTwoJournals(j1, j2);
                
                JournalMetrics aRow = rowMap.get(j1);
                JournalMetrics bRow = rowMap.get(j2);
                
                if (aRow != null && bRow != null) {
                    Map<String, Object> aRadar = service.buildRadarFromRow(aRow);
                    Map<String, Object> bRadar = service.buildRadarFromRow(bRow);
                    
                    // 构建叠加雷达图
                    Map<String, Object> overlay = new HashMap<>();
                    overlay.put("labels", aRadar.get("labels"));
                    
                    Map<String, Object> aData = new HashMap<>();
                    aData.put("name", j1);
                    aData.put("values", aRadar.get("values"));
                    overlay.put("a", aData);
                    
                    Map<String, Object> bData = new HashMap<>();
                    bData.put("name", j2);
                    bData.put("values", bRadar.get("values"));
                    overlay.put("b", bData);
                    
                    overlay.put("max", 200);
                    
                    // 构建柱状图数据
                    String[] barLabels = {"paper_count", "novelty", "disruption", "interdisciplinary", 
                        "topic", "theme_concentration", "hot_response"};
                    Map<String, Object> bar = new HashMap<>();
                    bar.put("labels", List.of("论文数量", "新颖性", "颠覆性", "跨学科性", 
                        "主题多样性", "主题集中度", "热点响应度"));
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> aRaw = (Map<String, Object>) aRadar.get("raw");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bRaw = (Map<String, Object>) bRadar.get("raw");
                    
                    List<Double> aValues = new ArrayList<>();
                    List<Double> bValues = new ArrayList<>();
                    for (String label : barLabels) {
                        aValues.add(((Number) aRaw.get(label)).doubleValue());
                        bValues.add(((Number) bRaw.get(label)).doubleValue());
                    }
                    
                    Map<String, Object> aBarData = new HashMap<>();
                    aBarData.put("name", j1);
                    aBarData.put("values", aValues);
                    bar.put("a", aBarData);
                    
                    Map<String, Object> bBarData = new HashMap<>();
                    bBarData.put("name", j2);
                    bBarData.put("values", bValues);
                    bar.put("b", bBarData);
                    
                    // 分析对比
                    @SuppressWarnings("unchecked")
                    List<String> dims = (List<String>) overlay.get("labels");
                    @SuppressWarnings("unchecked")
                    List<Double> aVals = (List<Double>) aData.get("values");
                    @SuppressWarnings("unchecked")
                    List<Double> bVals = (List<Double>) bData.get("values");
                    
                    List<String> analysisLines = new ArrayList<>();
                    for (int i = 0; i < dims.size(); i++) {
                        String dim = dims.get(i);
                        double av = aVals.get(i);
                        double bv = bVals.get(i);
                        
                        if (Math.abs(av - bv) < 0.01) {
                            analysisLines.add(String.format("%s：两者相当（%.2f）。", dim, av));
                        } else {
                            String win = av > bv ? j1 : j2;
                            double diff = Math.abs(av - bv);
                            analysisLines.add(String.format("%s：%s 更高（差值 %.2f）。", dim, win, diff));
                        }
                    }
                    
                    model.addAttribute("aRow", aRow);
                    model.addAttribute("bRow", bRow);
                    model.addAttribute("aKeywords", JournalMetricsDAO.pickTopKeywords(aRow));
                    model.addAttribute("bKeywords", JournalMetricsDAO.pickTopKeywords(bRow));
                    model.addAttribute("aComments", service.buildCommentsFromRow(aRow));
                    model.addAttribute("bComments", service.buildCommentsFromRow(bRow));
                    model.addAttribute("overlayJson", objectMapper.writeValueAsString(overlay));
                    model.addAttribute("aRadarJson", objectMapper.writeValueAsString(aRadar));
                    model.addAttribute("bRadarJson", objectMapper.writeValueAsString(bRadar));
                    model.addAttribute("barJson", objectMapper.writeValueAsString(bar));
                    model.addAttribute("analysisLines", analysisLines);
                } else {
                    model.addAttribute("aRow", null);
                    model.addAttribute("bRow", null);
                }
            } else {
                model.addAttribute("aRow", null);
                model.addAttribute("bRow", null);
            }
            
            return "journal/compare";
        } catch (Exception e) {
            model.addAttribute("error", "加载对比页面失败: " + e.getMessage());
            return "error";
        }
    }
    
    // 辅助方法
    
    private UserSurvey loadSurvey(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        
        for (Cookie cookie : cookies) {
            if (SURVEY_COOKIE_NAME.equals(cookie.getName())) {
                try {
                    return decodeSurvey(cookie.getValue());
                } catch (Exception e) {
                    System.err.println("解析问卷Cookie失败: " + e.getMessage());
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private UserSurvey decodeSurvey(String token) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(token);
        String json = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> data = objectMapper.readValue(json, Map.class);
        
        UserSurvey survey = new UserSurvey();
        survey.setKeywordsRaw((String) data.get("keywords_raw"));
        survey.setKeywords((List<String>) data.get("keywords"));
        
        Map<String, Object> scores = (Map<String, Object>) data.get("scores");
        survey.setNovelty(((Number) scores.get("novelty")).doubleValue());
        survey.setDisruption(((Number) scores.get("disruption")).doubleValue());
        survey.setInterdisciplinary(((Number) scores.get("interdisciplinary")).doubleValue());
        survey.setThemeConcentration(((Number) scores.get("theme_concentration")).doubleValue());
        survey.setTopic(((Number) scores.get("topic")).doubleValue());
        survey.setHotResponse(((Number) scores.get("hot_response")).doubleValue());
        survey.setCreatedAt((String) data.get("created_at"));
        
        return survey;
    }
    
    private JournalMetrics convertSurveyToMetrics(UserSurvey survey) {
        JournalMetrics metrics = new JournalMetrics();
        metrics.setNovelty(survey.getNovelty());
        metrics.setDisruption(survey.getDisruption());
        metrics.setInterdisciplinary(survey.getInterdisciplinary());
        metrics.setThemeConcentration(survey.getThemeConcentration());
        metrics.setTopic(survey.getTopic());
        metrics.setHotResponse(survey.getHotResponse());
        return metrics;
    }
}
