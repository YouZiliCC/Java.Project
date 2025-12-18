package com.paper.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paper.dao.MySQLHelper;
import com.paper.model.Paper;

/**
 * 搜索服务类
 * 负责论文搜索相关的业务逻辑
 */
public class SearchService {
    
    public SearchService() {}
    
    /**
     * 根据研究领域搜索论文
     * @param keyword 搜索关键词（研究领域）
     * @return 匹配的论文列表
     */
    public List<Paper> searchByTarget(String keyword) throws ClassNotFoundException, SQLException {
        MySQLHelper mysqlHelper = new MySQLHelper();
        String sql = "SELECT * FROM PAPER WHERE target = ?";
        Map<String, Object> result = mysqlHelper.executeSQLWithSelect(sql, keyword);
        
        List<Paper> paperList = new ArrayList<>();
        ResultSet rs = (ResultSet) result.get("result");
        
        try {
            while (rs != null && rs.next()) {
                Paper paper = new Paper();
                paper.setWosId(rs.getString("wos_id"));
                paper.setRefs(rs.getInt("refs"));
                paper.setCitations(rs.getInt("citations"));
                paper.setConference(rs.getString("conference"));
                paper.setTarget(rs.getString("target"));
                paper.setAuthor(rs.getString("author"));
                paper.setTitle(rs.getString("title"));
                paper.setAbstractText(rs.getString("abstract_text"));
                // 处理可能为null的日期
                java.sql.Date publishDate = rs.getDate("publish_date");
                if (publishDate != null) {
                    paper.setPublishDate(publishDate.toLocalDate());
                }
                paper.setJournal(rs.getString("journal"));
                paper.setVolume(rs.getInt("volume"));
                paper.setIssue(rs.getInt("issue"));
                paper.setDoi(rs.getString("doi"));
                paper.setCountry(rs.getString("country"));
                paper.setKeywords(rs.getString("keywords"));
                paperList.add(paper);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return paperList;
    }
}
