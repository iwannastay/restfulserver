package com.iws.engineserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.Deployment;
import com.iws.engineserver.service.ClusterModel.DeploymentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/deployment")
@RestController
public class DeploymentController {
    @Autowired
    DeploymentManager deploymentManager;

    @RequestMapping(value = "/export",method = RequestMethod.POST)
    public JSONObject exportDevelopment(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        List<String> images = jsonObject.getObject("images", ArrayList.class);
        if(user==null || name==null|| images==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }
        boolean success=deploymentManager.exportDeployment(user, name,images);
        if (success) {
            info.put("code","200");
            info.put("msg","Export deployment: " + name + " success.");
        }
        else {
            info.put("code","501");
            info.put("msg","Fail to export deployment: " + name);
        }
        return info;
    }

    @RequestMapping(value = "/deleteDeployment",method = RequestMethod.POST)
    public JSONObject deleteDevelopment(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        if(user==null || name==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }
        boolean success=deploymentManager.deleteDeployment(user, name);
        if (success) {
            info.put("code","200");
            info.put("msg","Delete deployment: " + name + " success.");
        }
        else {
            info.put("code","501");
            info.put("msg","Fail to delete deployment: " + name);
        }
        return info;
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public JSONObject uploadConfig(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        String jsonName = jsonObject.getString("jsonName");
        JSONObject jsonFile = jsonObject.getJSONObject("jsonFile");
        if(user==null||name==null||jsonName==null||jsonFile==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }
        boolean success=deploymentManager.uploadConfig(name,jsonName,jsonFile);
        if (success) {
            info.put("code","200");
            info.put("msg","Upload config file: " + jsonName + " to "+name+" success.");
        }
        else {
            info.put("code","501");
            info.put("msg","Fail to upload config file: " + jsonName);
        }
        return info;
    }

    @RequestMapping(value = "/deleteConfig",method = RequestMethod.POST)
    public JSONObject deleteConfig(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String name = jsonObject.getString("name");
        String jsonName = jsonObject.getString("jsonName");
        if(name==null||jsonName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }
        boolean success=deploymentManager.deleteConfig(name,jsonName);
        if (success) {
            info.put("code","200");
            info.put("msg","Delete config file: " + jsonName + " from "+name+" success.");
        }
        else {
            info.put("code","501");
            info.put("msg","Fail to delete config file: " + name);
        }
        return info;
    }

    @RequestMapping(value = "/listDeployment",method = RequestMethod.GET)
    public JSONObject listDeployment(@RequestParam(value = "user") String user){
        JSONObject info = new JSONObject();

        if(user==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }
        List<Deployment> deployments = deploymentManager.listDeployment();
        if (deployments!=null) {
            info.put("code","200");
            info.put("msg","Success");
            info.put("data",deployments);
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/listApplication",method = RequestMethod.GET)
    public JSONObject listApplication(@RequestParam(value = "user") String user,
                                      @RequestParam(value = "deployName", required = false) String deployName){
        JSONObject info = new JSONObject();

        if(user==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        List<Deployment.Application> applications = deploymentManager.listApplication(deployName);
        if (applications!=null) {
            info.put("code","200");
            info.put("msg","Success");
            info.put("data",applications);
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/run",method = RequestMethod.POST)
    public JSONObject runApplication(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String deployName = jsonObject.getString("deployName");
        String jsonName = jsonObject.getString("jsonName");
        String appName = jsonObject.getString("appName");
        if(user==null|| deployName==null|| jsonName==null|| appName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        boolean success = deploymentManager.runApplication(deployName, jsonName, appName);
        if (success) {
            info.put("code","200");
            info.put("msg","Success");
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/start",method = RequestMethod.POST)
    public JSONObject startApplication(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String deployName = jsonObject.getString("deployName");
        String appName = jsonObject.getString("appName");
        if(user==null || deployName==null || appName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        boolean success = deploymentManager.startApplication(deployName, appName);
        if (success) {
            info.put("code","200");
            info.put("msg","Success");
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/stop",method = RequestMethod.POST)
    public JSONObject stopApplication(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String deployName = jsonObject.getString("deployName");
        String appName = jsonObject.getString("appName");
        if(user==null || deployName==null || appName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        boolean success = deploymentManager.stopApplication(deployName, appName);
        if (success) {
            info.put("code","200");
            info.put("msg","Success");
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/deleteApplication",method = RequestMethod.POST)
    public JSONObject deleteApplication(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();

        String user = jsonObject.getString("user");
        String deployName = jsonObject.getString("deployName");
        String appName = jsonObject.getString("appName");
        if(user==null || deployName==null || appName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        boolean success = deploymentManager.deleteApplication(deployName, appName);
        if (success) {
            info.put("code","200");
            info.put("msg","Success");
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

    @RequestMapping(value = "/runState",method = RequestMethod.GET)
    public JSONObject getApplication(@RequestParam(value = "user") String user,
                                     @RequestParam(value = "deployName") String deployName,
                                     @RequestParam(value = "appName") String appName){
        JSONObject info = new JSONObject();


        if(user==null || deployName==null || appName==null){
            info.put("code","401");
            info.put("msg","missing args");
            return info;
        }

        JSONObject application = deploymentManager.getApplication(deployName, appName);
        if (application!=null) {
            info.put("code","200");
            info.put("msg","Success");
            info.put("data",application);
        }
        else {
            info.put("code","501");
            info.put("msg","Failed");
        }
        return info;
    }

}
