package com.paper.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Paper 模型类单元测试
 */
public class PaperTest {
    
    @Test
    @DisplayName("Paper对象创建和基本属性测试")
    void testPaperBasicProperties() {
        Paper paper = new Paper();
        
        paper.setWosId("WOS:123456");
        paper.setTitle("Test Paper Title");
        paper.setAbstractText("This is a test abstract.");
        paper.setPublishDate(LocalDate.of(2024, 1, 15));
        paper.setJournal("Test Journal");
        paper.setAuthor("Author Name");
        
        assertEquals("WOS:123456", paper.getWosId());
        assertEquals("Test Paper Title", paper.getTitle());
        assertEquals("This is a test abstract.", paper.getAbstractText());
        assertEquals(LocalDate.of(2024, 1, 15), paper.getPublishDate());
        assertEquals("Test Journal", paper.getJournal());
        assertEquals("Author Name", paper.getAuthor());
    }
    
    @Test
    @DisplayName("Paper数值属性测试")
    void testPaperNumericProperties() {
        Paper paper = new Paper();
        
        paper.setVolume(10);
        paper.setIssue(5);
        paper.setPages(100);
        paper.setCitations(50);
        paper.setRefs(30);
        
        assertEquals(10, paper.getVolume());
        assertEquals(5, paper.getIssue());
        assertEquals(100, paper.getPages());
        assertEquals(50, paper.getCitations());
        assertEquals(30, paper.getRefs());
    }
    
    @Test
    @DisplayName("Paper其他属性测试")
    void testPaperOtherProperties() {
        Paper paper = new Paper();
        
        paper.setDoi("10.1234/test.doi");
        paper.setCountry("China");
        paper.setTarget("deep learning");
        paper.setConference("ICML 2024");
        paper.setKeywords("AI;ML;Deep Learning");
        
        assertEquals("10.1234/test.doi", paper.getDoi());
        assertEquals("China", paper.getCountry());
        assertEquals("deep learning", paper.getTarget());
        assertEquals("ICML 2024", paper.getConference());
        assertEquals("AI;ML;Deep Learning", paper.getKeywords());
    }
    
    @Test
    @DisplayName("Paper默认值测试")
    void testPaperDefaultValues() {
        Paper paper = new Paper();
        
        // 数值类型默认为0
        assertEquals(0, paper.getVolume());
        assertEquals(0, paper.getIssue());
        assertEquals(0, paper.getPages());
        assertEquals(0, paper.getCitations());
        assertEquals(0, paper.getRefs());
        
        // 引用类型默认为null
        assertNull(paper.getWosId());
        assertNull(paper.getTitle());
        assertNull(paper.getAbstractText());
        assertNull(paper.getPublishDate());
    }
}
