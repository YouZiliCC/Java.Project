package com.paper.Entity;

public class User {
    private String uname;
    private String password;
    private String email;
    public String getUname(){
        return uname;
    }
    public void setUname(String uname){
        this.uname=uname;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
}