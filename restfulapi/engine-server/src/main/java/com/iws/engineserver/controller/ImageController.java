package com.iws.engineserver.controller;


import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.service.ClusterModel.ImageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/image")
public class ImageController {
    @Autowired
    org.slf4j.Logger logger;

    @Autowired
    ImageManager imageManager;

    @RequestMapping(value = "/pull",method = RequestMethod.POST)
    public String pullImage(@RequestBody JSONObject jsonObject){
//        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");

        try {
            String newName = imageManager.pullImageToHarbor(name);
            return newName;
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info("pull image failed: "+name);
        }
        return null;
    }


    @RequestMapping(value = "/remove",method = RequestMethod.POST)
    public String removeImage(@RequestBody JSONObject jsonObject){
        String name = jsonObject.getString("name");
        if(imageManager.deleteImageFromHarbor(name))
            return "success";
        return "false";
    }

    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public String saveImage(@RequestBody JSONObject jsonObject) throws InterruptedException {
        String containerID = jsonObject.getString("containerID");
        String name = jsonObject.getString("name");

        imageManager.commitImageToHarbor(containerID,name,imageManager.getPersistence());

        return "success?";
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public String getImage(@RequestParam String name){
        return imageManager.getImage(name);
    }

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String getImage(@RequestParam(value = "page") int page,
                           @RequestParam(value = "page_size") int pageSize){
        return imageManager.getImage("name");
    }

}
