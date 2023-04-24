package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class Deployment {
    String user;
    String name;
    List<String> images;
    Map<String,JSONObject> configs;
    Map<String,Application> applications;

    public static class Application{
        static SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddhhmmss");
        String name;
        String appID;
        String taskID;
        String jsonName;
        String deployName;
        String state;



        public void genID(){
            if(appID!=null) return;
            appID= "app"+ft.format(new Date())+new Random().nextInt(10);
        }

        public Application() {
        }

        public Application(String name, String jsonName, String deployName) {
            this.name = name;
            this.jsonName = jsonName;
            this.deployName = deployName;
            genID();
        }

        public Application(String name, String appID, String taskID, String jsonName, String deployName, String state) {
            this.name = name;
            this.appID = appID;
            this.taskID = taskID;
            this.jsonName = jsonName;
            this.deployName = deployName;
            this.state = state;
        }

        public static SimpleDateFormat getFt() {
            return ft;
        }

        public static void setFt(SimpleDateFormat ft) {
            Application.ft = ft;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAppID() {
            return appID;
        }

        public void setAppID(String appID) {
            this.appID = appID;
        }

        public String getTaskID() {
            return taskID;
        }

        public void setTaskID(String taskID) {
            this.taskID = taskID;
        }

        public String getJsonName() {
            return jsonName;
        }

        public void setJsonName(String jsonName) {
            this.jsonName = jsonName;
        }

        public String getDeployName() {
            return deployName;
        }

        public void setDeployName(String deployName) {
            this.deployName = deployName;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Application{" +
                    "name='" + name + '\'' +
                    ", appID='" + appID + '\'' +
                    ", taskID='" + taskID + '\'' +
                    ", jsonName='" + jsonName + '\'' +
                    ", deployName='" + deployName + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }


    public Deployment() {
    }

    public Deployment(String user, String name, List<String> images, Map<String, JSONObject> configs,Map<String,Application> applications) {
        this.user=user;
        this.name = name;
        this.images = images;
        this.configs = configs;
        this.applications=applications;
    }

    public Deployment(Document document){

            this(
                    document.getString("user"),
                    document.getString("name"),
                    document.getList("images", String.class),
                    document.get("configs", Document.class)==null? null:JSONObject.parseObject(document.get("configs", Document.class).toJson()).toJavaObject(Map.class),
//                    document.get("applications", Document.class)==null? new HashMap<>():JSONObject.parseObject(document.get("applications", Document.class).toJson()).toJavaObject(Map.class)
                    document.get("applications", Document.class)==null? new HashMap<>():
                    new HashMap<>(){{
                        Document applications = document.get("applications", Document.class);
                        for(String appName:applications.keySet()){
                            Application application=JSONObject.parseObject(applications.get(appName,Document.class).toJson()).toJavaObject(Application.class);
                            put(appName,application);
                        }
                    }}
            );
    }

    public Document toDocument(){
        return Document.parse(JSONObject.toJSONString(this));
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Map<String, JSONObject> getConfigs() {
        return configs;
    }

    public Map<String, Application> getApplications() {
        return applications;
    }

    public void setApplications(Map<String, Application> applications) {
        this.applications = applications;
    }

    public void setConfigs(Map<String, JSONObject> configs) {
        this.configs = configs;
    }

    public void addConfig(String name, JSONObject jsonObject){
        if(configs==null)
            configs =new HashMap<>();
        configs.put(name,jsonObject);
    }

    public void removeConfig(String name){
        if(configs==null) return;
        configs.remove(name);
//        if(configs.size()==0)
//            configs =null;
    }

    @Override
    public String toString() {
        return "Deployment{" +
                "user='" + user + '\'' +
                ", name='" + name + '\'' +
                ", images=" + images +
                ", configs=" + configs +
                ", applications=" + applications +
                '}';
    }

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test","exist");
        System.out.println(jsonObject.get("tet"));
    }
}
