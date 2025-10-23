package com.paper.Entity;

public class Author {
    private String name;
    private String affiliation;
    private String email;
    private String research_field;
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getAffiliation(){
        return affiliation;
    }
    public void setAffiliation(String affiliation){
        this.affiliation=affiliation;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public String getResearch_field(){
        return research_field;
    }
    public void setResearch_field(String research_field){
        this.research_field=research_field;
    }
}