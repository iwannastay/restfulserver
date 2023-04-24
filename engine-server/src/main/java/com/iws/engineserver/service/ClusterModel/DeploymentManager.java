package com.iws.engineserver.service.ClusterModel;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.ClusterModel.DeploymentImp;
import com.iws.engineserver.pojo.Deployment;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;


import com.iws.engineserver.pojo.Deployment.Application;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DeploymentManager {


    @Autowired
    ImageManager imageManager;

    @Autowired
    DeploymentImp deploymentImp;

    @Value("${deployment.appAddress}")
    String appAddress;
    @Value("${deployment.taskAddress}")
    String taskAddress;

    public boolean exportDeployment(String user, String name, List<String> images) {
        Deployment deployment = deploymentImp.getDeployment(name);
        if(deployment==null)
            return deploymentImp.addDeployment(new Deployment(user, name, images, null,null));
        return false;
    }

    public boolean deleteDeployment(String user, String name) {

        Deployment deployment = deploymentImp.getDeployment(name);
        if(deployment==null
                || !deployment.getApplications().isEmpty()
                || !deployment.getUser().equals(user)
        ) return false;

        return deploymentImp.deleteDeployment(name);
    }


    public boolean uploadConfig(String name, String jsonName, JSONObject jsonFile) {
        Deployment deployment = deploymentImp.getDeployment(name);
        if(deployment==null) return false;
        deployment.addConfig(jsonName, jsonFile);
        return deploymentImp.updateDeployment(deployment);
    }


    public boolean deleteConfig(String name, String jsonName) {
        Deployment deployment = deploymentImp.getDeployment(name);
        if(deployment==null) return false;
        for(Application application :deployment.getApplications().values()){
            if(application.getJsonName().equals(jsonName)) return false;
        }

        deployment.removeConfig(jsonName);
        return deploymentImp.updateDeployment(deployment);
    }

    public List<Deployment> listDeployment() {
        return deploymentImp.listDeployment();
    }


    public boolean runApplication(String deployName, String jsonName, String appName) {
        //TODO: create app, create task, update DB
        Deployment deployment = deploymentImp.getDeployment(deployName);
        if (deployment == null
                || deployment.getApplications().containsKey(appName)
                || deployment.getConfigs()==null
                || deployment.getConfigs().get(jsonName)==null
        ) return false;

        Application application = new Application(appName, jsonName, deployName);
        JSONObject jsonFile = deployment.getConfigs().get(jsonName);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonFile", jsonFile);
        jsonObject.put("appID", application.getAppID());
        JSONObject response1 = sendPOST(appAddress + "/uploadDevAppConfig", jsonObject);
        if (response1 == null || !response1.isEmpty()) return false;

        JSONObject taskObject = new JSONObject();
        taskObject.put("appID", application.getAppID());
        taskObject.put("appName", appName);
        JSONObject resposne2 = sendPOST(taskAddress + "/createDevTask", taskObject);
        if (resposne2 == null) return false;


        String taskID = resposne2.getString("taskID");
        if (taskID == null) return false;
        application.setTaskID(taskID);
        application.setState("RUNNING");
        deployment.getApplications().put(appName, application);
        return deploymentImp.updateDeployment(deployment);
    }

    public boolean startApplication(String deployName, String appName) {
        Deployment deployment = deploymentImp.getDeployment(deployName);
        if (deployment == null
                || !deployment.getApplications().containsKey(appName)
                || deployment.getApplications().get(appName).getState().equals("RUNNING")
        ) return false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appID", deployment.getApplications().get(appName).getAppID());
        jsonObject.put("appName", appName);
        JSONObject response = sendPOST(taskAddress + "/createDevTask", jsonObject);
        if (response == null ) return false;

        String taskID = response.getString("taskID");
        if (taskID == null) return false;
        deployment.getApplications().get(appName).setTaskID(taskID);
        deployment.getApplications().get(appName).setState("RUNNING");
        return deploymentImp.updateDeployment(deployment);
    }


    public boolean stopApplication(String deployName, String appName) {
        Deployment deployment = deploymentImp.getDeployment(deployName);
        if (deployment == null
                || !deployment.getApplications().containsKey(appName)
                || deployment.getApplications().get(appName).getState().equals("STOPPED")
        ) return false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskID", deployment.getApplications().get(appName).getTaskID());
        JSONObject response = sendPOST(taskAddress + "/stop", jsonObject);
        if (response == null || !response.isEmpty()) return false;

        deployment.getApplications().get(appName).setState("STOPPED");
        return deploymentImp.updateDeployment(deployment);
    }

    public JSONObject getApplication(String deployName, String appName) {
        Deployment deployment = deploymentImp.getDeployment(deployName);
        if (deployment == null
                || !deployment.getApplications().containsKey(appName)
                || deployment.getApplications().get(appName).getState().equals("STOPPED")
        ) return null;
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("taskID",deployment.getApplications().get(appName).getTaskID());
        //TODO:update DB state
        return sendGET(taskAddress + "/runState?taskID={taskID}", jsonObject);
    }

    public boolean deleteApplication(String deployName, String appName) {
        Deployment deployment = deploymentImp.getDeployment(deployName);
        if (deployment == null
                || !deployment.getApplications().containsKey(appName)
        ) return false;

        if(deployment.getApplications().get(appName).getState().equals("RUNNING")){
            stopApplication(deployName,appName); //Just try
        }
        deployment.getApplications().remove(appName);

        return deploymentImp.updateDeployment(deployment);
    }

    public List<Application> listApplication(String deployName) {
        List<Application> list = new ArrayList<>();
        if(deployName==null){
            List<List<Application>> collections = deploymentImp.listDeployment().stream().map(deploy -> new ArrayList<>(deploy.getApplications().values())).collect(Collectors.toList());
            for (List<Application> collection : collections) {
                list.addAll(collection);
            }
        }else if(deploymentImp.getDeployment(deployName)!=null){
            list.addAll(new ArrayList<>(deploymentImp.getDeployment(deployName).getApplications().values()));
        }
        return list;
    }

    public static void main(String[] args) {

        String url="http://10.16.156.137:8001/support/algorithm/compileState?algorithmNo={algorithmNo}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("algorithmNo","algo210803091815900010");
        DeploymentManager deploymentManager = new DeploymentManager();
        JSONObject jsonObject1 = deploymentManager.sendGET(url, jsonObject);
        System.out.println(jsonObject1);

    }

    public JSONObject jsonFileReader(String filePath) {
        StringBuilder json = new StringBuilder();
        File file = new File(filePath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int length = 0;

            while ((length = fileInputStream.read(buf)) != -1) {
                json.append(new String(buf, 0, length));
            }
            fileInputStream.close();
            return JSONObject.parseObject(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject sendPOST(String url, JSONObject data) {
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> request = new HttpEntity<>(data, httpHeaders);

        ResponseEntity<JSONObject> entity = restTemplate.postForEntity(url, request, JSONObject.class);
        return entity.getBody();
    }

    public JSONObject sendGET(String url, JSONObject data) {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<JSONObject> request = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                JSONObject.class,
                data);
        return resultEntity.getBody();
    }

}
