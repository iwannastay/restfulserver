package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

public class User {
    private String userName;
    private String password;
    private int uid;
    private String email;
    private String token;
    private int role;

    private String gitlabName;
    private String gitlabPassword;

    public String getGitlabName() {
        return gitlabName;
    }

    public void setGitlabName(String gitlabName) {
        this.gitlabName = gitlabName;
    }

    public String getGitlabPassword() {
        return gitlabPassword;
    }

    public void setGitlabPassword(String gitlabPassword) {
        this.gitlabPassword = gitlabPassword;
    }



    public User() {
    }

    public User(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public User(String userName, String password, int uid, String email, String token, int role) {
        this.userName = userName;
        this.password = password;
        this.uid = uid;
        this.email = email;
        this.token = token;
        this.role = role;

    }

    public User(Document document){
        this(document.getString("userName"),
                document.getString("password"),
                document.getInteger("uid"),
                document.getString("email"),
                document.getString("token"),
                document.getInteger("role"));
    }

    public User(Request request){
        this(request.getUserName(),request.getPassword(),request.getEmail());
        setRole(request.getRole());
    }

    public Document toDocument(){
        return Document.parse(JSONObject.toJSONString(this));
    }




    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role =role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", role=" + role +
                ", gitlabName='" + gitlabName + '\'' +
                ", gitlabPassword='" + gitlabPassword + '\'' +
                '}';
    }
}
