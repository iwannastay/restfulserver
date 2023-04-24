package com.iws.engineserver.pojo;


import com.alibaba.fastjson.JSONObject;

public class ResponseMessage {
    private int code;
    private String msg;
    private JSONObject data;

    public int getCode() {
        return code;
    }

    public ResponseMessage setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResponseMessage setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public JSONObject getData() {
        return data;
    }

    public ResponseMessage setData(JSONObject data) {
        this.data = data;
        return this;
    }

    public ResponseMessage() {
        this(800,"success",new JSONObject());
    }

    public ResponseMessage(int code, String msg) {
        this(code,msg, new JSONObject());
    }

    public ResponseMessage(int code, String msg, JSONObject data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
