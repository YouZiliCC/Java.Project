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
    private String author;
    private String target;
    private String conference;
    private int citations;
    private int refs;
    private String wos_id;
    public String getWos_id(){
        return wos_id;
    }
    public void setWos_id(String wos_id){
        this.wos_id=wos_id;
    }
    public int getRefs(){
        return refs;
    }
    public void setRefs(int refs){
        this.refs=refs;
    }
    public int getCitations(){
        return citations;
    }
    public void setCitations(int citations){
        this.citations=citations;
    }
    public String getConference(){
        return conference;
    }
    public void setConference(String conference){
        this.conference=conference;
    }
    public String getTarget(){
        return target;
    }
    public void setTarget(String target){
        this.target=target;
    }
    public String getAuthor(){
        return author;
    }
    public void setAuthor(String author){
        this.author=author;
    }
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