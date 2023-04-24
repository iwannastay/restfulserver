package com.iws.engineserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.Cluster;
import com.iws.engineserver.pojo.Development;
import com.iws.engineserver.service.ClusterModel.DevelopmentManager;
import com.iws.engineserver.service.ClusterModel.ImageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/development")
public class DevelopmentController {



    static Map<String, Boolean> saved =new HashMap<>();

    @Autowired
    Cluster cluster;

    @Autowired
    DevelopmentManager developmentManager;

    @Autowired
    ImageManager imageManager;

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public JSONObject createContainer(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 9) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        Development development = JSONObject.toJavaObject(jsonObject, Development.class);

        Development devContainer = developmentManager.createDevContainer(development);

        if (null!=devContainer) {
            info.put("code", "200");
            info.put("msg", "容器创建成功");
            saved.put(info.getString("name"),false);
        } else {
            info.put("code", "500");
            info.put("msg", "容器创建失败");
        }

        return info;
    }

    @RequestMapping(value = "/remove",method = RequestMethod.POST)
    public JSONObject removeContainer(@RequestBody JSONObject jsonObject){
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 2) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        String name=jsonObject.getString("name");
        String user=jsonObject.getString("user");

        Development development = developmentManager.getDevelopmentImp().getDevelopment(name);
        if(null!=development&&user.equals(development.getUser())){
            boolean success = developmentManager.deleteDevContainer(name);

            if (success) {
                info.put("code", "200");
                info.put("msg", "容器删除成功");
                return info;
            }
        }

        info.put("code", "500");
        info.put("msg", "容器删除失败");
        return info;
    }

    @RequestMapping(value = "/start",method = RequestMethod.POST)
    public boolean startContainer(@RequestBody JSONObject jsonObject){
        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        boolean running=developmentManager.startDevContainer(user,name);
        if(running){
            saved.put(jsonObject.getString("name"),false);
        }
        return running;
    }

    @RequestMapping(value = "/stop",method = RequestMethod.POST)
    public String stopContainer(@RequestBody JSONObject jsonObject){
        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        boolean save = (Boolean) jsonObject.getOrDefault("save",false);
        if (save && !saved.getOrDefault(name, false)) {
            if (developmentManager.makeCheckpoint(user, name) != null) {
                saved.put(name, true);
            }
        }

        String msg=null;
        int stopped = developmentManager.stopDevContainer(user, name);
        switch (stopped){
            case 0:
                msg="Container "+name+" Stopped.";
                break;
            case 1:
                msg="Failed to stop container "+name;
                break;
            case 2:
                msg="Container "+name+" is terminating";
                break;
        }
        return msg;
    }

    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public String saveContainer(@RequestBody JSONObject jsonObject){
        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        return developmentManager.makeCheckpoint(user,name);
    }

    @RequestMapping(value = "/drop",method = RequestMethod.POST)
    public boolean dropCheckpoint(@RequestBody JSONObject jsonObject){
        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        String image = jsonObject.getString("image");
        return developmentManager.dropCheckpoint(user,name,image);
    }

    @RequestMapping(value = "/setdefault",method = RequestMethod.POST)
    public boolean setDefaultImage(@RequestBody JSONObject jsonObject){
        String user = jsonObject.getString("user");
        String name = jsonObject.getString("name");
        String image = jsonObject.getString("image");
        return developmentManager.setDefaultCheckpoint(user,name,image);
    }




    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public List<JSONObject> listContainer(@RequestParam(value = "user") String user,
                                           @RequestParam(value = "page") int page,
                                           @RequestParam(value = "pageSize") int pageSize){
        List<Development> developments = developmentManager.listDevContainer(user);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        int n = page * pageSize;
        Iterator<Development> iterator = developments.iterator();
        while(iterator.hasNext()&&jsonObjects.size()<n){
            jsonObjects.add(JSONObject.parseObject(JSONObject.toJSONString(iterator.next())));
        }
        return jsonObjects;
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public JSONObject getContainer(@RequestParam String user,@RequestParam String name){
        return JSONObject.parseObject(JSONObject.toJSONString(developmentManager.getDevContainer(user,name)));
    }


    @RequestMapping(value = "/connect",method = RequestMethod.GET)
    public Map<String, String> connectContainer(@RequestParam String user, @RequestParam String name){
        return developmentManager.connectDevContainer(user,name);
    }

    public void Fuck(){}

}
