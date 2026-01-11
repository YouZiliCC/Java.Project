package com.paper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paper.dao.JournalMetricsDAO;
import com.paper.model.JournalMetrics;
import com.paper.model.UserSurvey;
import com.paper.service.JournalService;
import com.paper.utils.AIClient;
import com.paper.utils.JournalDatabaseConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 期刊控制器 - 处理期刊展示、推荐、对比等功能
 */
@Controller
@RequestMapping("/journal")
public class JournalController {
    
    private static final String SURVEY_COOKIE_NAME = "pm_survey";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final JournalService service;
    private final AIClient aiClient;
    
    @Autowired
    public JournalController(JournalService service, AIClient aiClient) {
        this.service = service;
        this.aiClient = aiClient;
    }
    
    /**
     * 期刊列表页面
     */
    @GetMapping({"", "/", "/list"})
    public String journalsPage(Model model) {
        try {
            List<JournalMetrics> journals = service.dao.fetchJournals();
            model.addAttribute("journals", journals);
            return "journal/journals";
        } catch (Exception e) {
            model.addAttribute("error", "数据库查询失败: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * 期刊详情页面
     */
    @GetMapping("/{journal}")
    public String journalDetailPage(@PathVariable String journal, Model model) {
        try {
            List<JournalMetrics> rows = service.dao.fetchJournalRows(journal);
            if (rows.isEmpty()) {
                model.addAttribute("error", "未找到该期刊");
                return "error";
            }
            
            JournalMetrics latestRow = rows.get(0);
            Map<Integer, List<String>> topKeywords = JournalMetricsDAO.pickTopKeywords(latestRow);
            Map<String, Object> radar = service.buildRadarFromRow(latestRow);
            Map<String, String> comments = service.buildCommentsFromRow(latestRow);
            
            model.addAttribute("journal", journal);
            model.addAttribute("rows", rows);
            model.addAttribute("topKeywords", topKeywords);
            model.addAttribute("radarJson", objectMapper.writeValueAsString(radar));
            model.addAttribute("comments", comments);
            
            return "journal/journal_detail";
        } catch (Exception e) {
            model.addAttribute("error", "处理失败: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * 期刊AI分析（异步接口）
     */
    @PostMapping("/{journal}/ai-analysis")
    @ResponseBody
    public Map<String, Object> journalAIAnalysis(@PathVariable String journal) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JournalMetrics> rows = dao.fetchJournalRows(journal);
            if (rows.isEmpty()) {
                result.put("error", "未找到该期刊");
                return result;service.dao.fetchJournalRows(journal);
            if (rows.isEmpty()) {
                result.put("error", "未找到该期刊");
                return result;
            }
            
            JournalMetrics latestRow = rows.get(0);
            String analysis = service.generateJournalAIAnalysis(latestRow);
            
            result.put("journal", journal);
            result.put("year", latestRow.getYear());
            result.put("analysis", analysis);
            result.put("used_model", ai败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 问卷页面
     */
    @GetMapping("/survey")
    public String surveyPage(HttpServletRequest request, Model model) {
        UserSurvey survey = loadSurvey(request);
        model.addAttribute("survey", survey != null ? survey : new UserSurvey());
        return "journal/survey";
    }
    
    /**
     * 提交问卷
     */
    @PostMapping("/survey/analyze")
    public String surveyAnalyze(
            @RequestParam String keywords,
            @RequestParam int qNovelty,
            @RequestParam int qDisruption,
            @RequestParam int qInterdisciplinary,
            @RequestParam int qThemeConcentration,
            @RequestParam int qTopic,
            @RequestParam int qHotResponse,
            HttpServletResponse response) {
        
        List<String> kws = service.parseUserKeywords(keywords);
        if (kws.isEmpty()) {
            return "redirect:/journal/survey?error=keywords_required";
        }
        
        UserSurvey survey = new UserSurvey();
        survey.setKeywordsRaw(keywords.trim());
        survey.setKeywords(kws);
        survey.setNovelty(service.scoresFromLikert(qNovelty));
        survey.setDisruption(service.scoresFromLikert(qDisruption));
        survey.setInterdisciplinary(service.scoresFromLikert(qInterdisciplinary));
        survey.setThemeConcentration(service.scoresFromLikert(qThemeConcentration));
        survey.setTopic(service.scoresFromLikert(qTopic));
        survey.setHotResponse(service.scoresFromLikert(qHotResponse));
        survey.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 保存到Cookie
        try {
            String cookieValue = encodeSurvey(survey);
            Cookie cookie = new Cookie(SURVEY_COOKIE_NAME, cookieValue);
            cookie.setMaxAge(60 * 60 * 24 * 7); // 7天
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            System.err.println("保存问卷Cookie失败: " + e.getMessage());
        }
        
        return "redirect:/journal/recommend";
    }
    
    /**
     * 推荐页面
     */
    @GetMapping("/recommend")
    public String recommendPage(HttpServletRequest request, Model model) {
        UserSurvey survey = loadSurvey(request);
        if (survey == null) {
            return "redirect:/journal/survey";
        }
        
        try {
            List<JournalMetrics> latestRows = dao.fetchLatestJournalRows();
            Map<String, Object> userRadar = service.buildUserRadar(survey);
            Set<String> userNorm = survey.getKeywords().stream()
                .map(service::normalizeKeyword)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());service.
            
            int targetYear = LocalDateTime.now().getYear();
            Map<String, List<String>> kwHits = new HashMap<>();
            for (String kw : survey.getKeywords()) {
                kwHits.put(kw, new ArrayList<>());
            }
            
            List<Map<String, Object>> recs = new ArrayList<>();
            
            for (JournalMetrics row : latestRows) {
                String jname = row.getJournal();
                if (jname == null || jname.trim().isEmpty()) {
                    continue;
                }
                
                List<String> journalKw = service.pickKeywordsForYear(row, targetYear);
                Map<String, String> jNormMap = new HashMap<>();
                for (String k : journalKw) {
                    String norm = service.normalizeKeyword(k);
                    if (!norm.isEmpty()) {
                        jNormMap.put(norm, k);
                    }
                }
                
                List<String> matchedNorm = userNorm.stream()
                    .filter(jNormMap::containsKey)
                    .collect(Collectors.toList());
                List<String> matchedDisplay = matchedNorm.stream()
                    .map(jNormMap::get)
                    .collect(Collectors.toList());
                
                // 记录每个用户关键词命中哪些期刊
                for (String uk : survey.getKeywords()) {
                    if (jNormMap.containsKey(service.normalizeKeyword(uk))) {
                        kwHits.get(uk).add(jname);
                    }
                }
                
                Map<String, Object> journalRadar = service.buildRadarFromRow(row);
                double sim = service.computeSimilarity(userRadar, journalRadar);
                double kwRatio = matchedDisplay.size() / (double) Math.max(1, userNorm.size());
                double score = kwRatio * 60.0 + sim * 40.0;
                
                Map<String, Object> rec = new HashMap<>();
                rec.put("journal", jname);
                rec.put("year", row.getYear());
                rec.put("category", row.getCategory());
                rec.put("matched_keywords", matchedDisplay);
                rec.put("kw_hit", matchedDisplay.size());
                rec.put("match_pct", (int) Math.round(sim * 100));
                rec.put("score", score);
                recs.add(rec);
            }
            
            // 排序并取前10
            recs.sort((a, b) -> {
                int scoreComp = Double.compare((Double) b.get("score"), (Double) a.get("score"));
                if (scoreComp != 0) return scoreComp;
                int kwComp = Integer.compare((Integer) b.get("kw_hit"), (Integer) a.get("kw_hit"));
                if (kwComp != 0) return kwComp;
                return Integer.compare((Integer) b.get("match_pct"), (Integer) a.get("match_pct"));
            });
            
            List<Map<String, Object>> topRecs = recs.stream().limit(10).collect(Collectors.toList());
            
            model.addAttribute("survey", survey);
            model.addAttribute("targetYear", targetYear);
            model.addAttribute("kwHits", kwHits);
            model.addAttribute("recs", topRecs);
            model.addAttribute("userRadarJson", objectMapper.writeValueAsString(userRadar));
            model.addAttribute("userComments", service.buildCommentsFromRow(
                convertSurveyToMetrics(survey)));
            
            return "journal/recommend";
        } catch (Exception e) {
            model.addAttribute("error", "生成推荐失败: " + e.getMessage());
            return "error";
        }
    }
    
    // 辅助方法：从Cookie加载问卷
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
    
    // 辅助方法：编码问卷为Base64
    private String encodeSurvey(UserSurvey survey) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("keywords_raw", survey.getKeywordsRaw());
        data.put("keywords", survey.getKeywords());
        Map<String, Double> scores = new HashMap<>();
        scores.put("novelty", survey.getNovelty());
        scores.put("disruption", survey.getDisruption());
        scores.put("interdisciplinary", survey.getInterdisciplinary());
        scores.put("theme_concentration", survey.getThemeConcentration());
        scores.put("topic", survey.getTopic());
        scores.put("hot_response", survey.getHotResponse());
        data.put("scores", scores);
        data.put("created_at", survey.getCreatedAt());
        
        String json = objectMapper.writeValueAsString(data);
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    // 辅助方法：从Base64解码问卷
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
    
    // 辅助方法：将Survey转换为Metrics格式以复用评语生成逻辑
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
