package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Request {
    static SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd hh:mm:ss");

    private String userName;
    private String password;
    private String email;
    private String createOn;
    private int status;
    private int role;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }


    public Request() {
    }

    public Request(String userName, String password, String email, int role) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.createOn = ft.format(new Date());
        this.role = role;
        this.status=0;
    }

    public Request(String userName, String password, String email, String createOn, int status, int role) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.createOn = createOn;
        this.status = status;
        this.role = role;
    }

    public  Request(JSONObject jsonObject) {
        //TODO
        this(
                jsonObject.getString("user_name"),
                jsonObject.getString("password"),
                jsonObject.getString("email"),
                jsonObject.getInteger("role")
        );
    }

    public  Request(Document document) {
        this(document.getString("userName"),
                document.getString("password"),
                document.getString("email"),
                document.getString("createOn"),
                document.getInteger("status"),
                document.getInteger("role"));

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreateOn() {
        return createOn;
    }

    public void setCreateOn(String requestTime) {
        this.createOn = createOn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Request{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", createOn='" + createOn + '\'' +
                ", status=" + status +
                ", role=" + role +
                '}';
    }
}
