package com.iws.engineserver.controller;


import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.Interface.StorageManager;
import com.iws.engineserver.service.ClusterModel.AlgorithmManager;
import com.iws.engineserver.service.ClusterModel.ImageManager;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.net.URL;

import org.apache.commons.io.FileUtils;

@RestController
public class KafkaController {
    @Autowired
    org.slf4j.Logger logger;

    @Autowired
    private KafkaTemplate<String, String> template;

    @Autowired
    AlgorithmManager algorithmManager;

    @Autowired
    ImageManager imageManager;

    @RequestMapping(value = "/kafka/{type}",method = RequestMethod.GET)
    public String sendMsg(@PathVariable String type
            ,@RequestParam(value = "no") String no
    ){
        String msg=type+"-"+no;
        //template.send("A", msg);
        return "success";
    }

    @KafkaListener(topics = "B")
    public void listen(ConsumerRecord<String, String> msg) {

        String algorithmNo=msg.key().substring(4);
        int compileStatus= Integer.parseInt(msg.value());

        int oldStatus = algorithmManager.getAlgorithmImp().getAlgorithmByNo(algorithmNo).getCompileStatus();

        //TODO: how to make a filter
        if(compileStatus!=2 || oldStatus==compileStatus)
            return;

        algorithmManager.onCompileComplete(algorithmNo,compileStatus);

        Map<String, String> map = algorithmManager.compileState(algorithmNo);

        RestTemplate restTemplate=new RestTemplate();
        String url=algorithmManager.getCallbackURL();
//                "http://localhost:8001/api/v1/kafka/test";
//                "http://www.chinanet01.com/sustech/developer/notify/algorithm/compileResult";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject info = new JSONObject();
        if (null != map) {
//            info.put("code", "200");
//            info.put("msg", "success");

            JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(map));
            data.put("compileStatus", Integer.parseInt(map.get("compileStatus")));
            data.put("algorithmNo",algorithmNo);
            info=data;
//            info.put("data", data);
        } else {
//            info.put("code", "500");
//            info.put("msg", "no such instance");
        }
        logger.info("callbackAddress: "+url+"-->"+algorithmNo+" "+compileStatus);
        HttpEntity<JSONObject> request = new HttpEntity<>(info, httpHeaders);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, request, String.class);


        logger.info("Response: " + entity.getBody());
    }

    @RequestMapping(value = "/test",method = RequestMethod.POST)
    public String test(){
        return "success";
    }


    @RequestMapping(value = "/uploadImage",method = RequestMethod.GET)
    public JSONObject uploadImage(
            @RequestParam(value = "filePath")String filePath,
            @RequestParam(value = "imageName")String imageName
//            @RequestBody JSONObject jsonObject
    ){

//        String filePath=(String)jsonObject.getOrDefault("filePath",null);
//        String imageName=(String)jsonObject.getOrDefault("imageName",null);

        logger.info("Param:---------filePath: "+filePath+"---------imageName: "+imageName);

        JSONObject info = new JSONObject();
        if (null == filePath || null==imageName) {
            info.put("code", "401");
            info.put("msg", "bad request");
            return info;
        }


        //download from url
        String imagePath="";
        try {
            URL httpURL = new URL(filePath);

            int index=filePath.lastIndexOf("/");
            assert (index>0);
            String fileName = filePath.substring(index+1);

            String dir = algorithmManager.getNfsManager().getRootPath() + algorithmManager.getNfsManager().getImagePath();

            imagePath=dir + "/" + fileName;
            File f = new File(imagePath);
            FileUtils.copyURLToFile(httpURL, f);
        } catch (Exception e) {
            e.printStackTrace();

            info.put("code", "500");
            info.put("msg", "failed to download from url");
            return info;
        }


        //extract to docker
        try {
            imageManager.getDockerConnecter().loadFromFile(imagePath);
            algorithmManager.getNfsManager().deleteMenu(imagePath, StorageManager.Info.TYPE.NULL);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            info.put("code", "500");
            info.put("msg", "failed to extract to docker");
            return info;
        }

        //upload to harbor

        String newName ="";
        try {

            logger.info("timestap 1:"+new Date() + newName);
            newName = imageManager.getDockerConnecter().pushImage(imageName,imageManager.getRepositoryAddress()+"/"+imageManager.getPersistence());
            logger.info("timestap 2:"+new Date() +newName);

            imageManager.getDockerConnecter().deleteLocalImage(imageName).deleteLocalImage(newName);
        } catch (InterruptedException e) {
            e.printStackTrace();

            info.put("code", "500");
            info.put("msg", "failed to upload to harbor");
            return info;
        }

        info.put("code", "200");
        info.put("msg", "success");

        JSONObject data = new JSONObject();
        data.put("imageName", newName);
        info.put("data", data);

        logger.info("returnValue:---------newName: "+newName);
        return info;

    }
}
