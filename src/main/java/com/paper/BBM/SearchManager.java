package com.paper.BBM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paper.DBM.MySQLHelper;
import com.paper.Entity.Paper;

public class SearchManager {
    public SearchManager() {}
    public List<Paper> SearchByTarget(String keyword) throws ClassNotFoundException,SQLException{
        MySQLHelper mysqlhelper=new MySQLHelper();
        String sqlString="SELECT * FROM paper WHERE target = ?";
        Map<String, Object> map=mysqlhelper.executeSQLWithSelect(sqlString, keyword);
        ResultSet set=null;
        set=(ResultSet) map.get("result");
        List<Paper> paperList=new ArrayList<>();
        try {
            // 解析每条记录到User对象，加入List
            while (set.next()) {
                Paper paper = new Paper();
                paper.setWos_id(set.getString("wos_id"));
                paper.setRefs(set.getInt("refs"));
                paper.setCitations(set.getInt("citations"));
                paper.setConference(set.getString("conference"));
                paper.setTarget(set.getString("target"));
                paper.setAuthor(set.getString("author"));
                paper.setTitle(set.getString("title"));
                paper.setAbstract_text(set.getString("abstract"));
                paper.setPublish_date(set.getDate("publish_data").toLocalDate());
                paper.setJournal(set.getString("journal"));
                paper.setVolume(set.getInt("volume"));
                paper.setIssue(set.getInt("issue"));
                paper.setDoi(set.getString("doi"));
                paper.setCountry(set.getString("country"));
                paperList.add(paper);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (set != null) {
                try {
                    set.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return paperList;
    }
}
