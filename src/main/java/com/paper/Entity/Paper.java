package com.paper.Entity;

import java.time.LocalDate;

public class Paper {
    private String title;
    private String abstract_text;
    private LocalDate publish_date;
    private String journal;
    private int volume;
    private int issue;
    private int pages;
    private String doi;
    private String country;
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }
    public String getAbstract_text(){
        return abstract_text;
    }
    public void setAbstract_text(String abstract_text){
        this.abstract_text=abstract_text;
    }
    public LocalDate getPublish_date(){
        return publish_date;
    }
    public void setPublish_date(LocalDate publish_date){
        this.publish_date=publish_date;
    }
    public String getJournal(){
        return journal;
    }
    public void setJournal(String journal){
        this.journal=journal;
    }
    public int getVolume(){
        return volume;
    }
    public void setVolume(int volume){
        this.volume=volume;
    }
    public int getIssue(){
        return issue;
    }
    public void setIssue(int issue){
        this.issue=issue;
    }
    public int getPages(){
        return pages;
    }
    public void setPages(int pages){
        this.pages=pages;
    }
    public String getDoi(){
        return doi;
    }
    public void setDoi(String doi){
        this.doi=doi;
    }
    public String getCountry(){
        return country;
    }
    public void setCountry(String country){
        this.country=country;
    }
}