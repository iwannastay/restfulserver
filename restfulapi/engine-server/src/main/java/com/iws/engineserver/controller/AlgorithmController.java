package com.iws.engineserver.controller;


import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.Algorithm;
import com.iws.engineserver.service.ClusterModel.AlgorithmManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/algorithm")
public class AlgorithmController {
    @Autowired
    org.slf4j.Logger logger;

    @Autowired
    AlgorithmManager algorithmManager;



    @RequestMapping(value = "/compile", method = RequestMethod.POST)
    public JSONObject compile(@RequestBody JSONObject jsonObject) {
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 8) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        Algorithm algorithm = algorithmManager.compile(JSONObject.toJavaObject(jsonObject, Algorithm.class));


        if (null!=algorithm&&algorithm.getCompileStatus() != 3) {
            info.put("code", "200");
            info.put("msg", "算法创建成功");

            //TODO: where to save it. maybe in each algorithm instance.
            algorithmManager.setCallbackURL(jsonObject.getString("callbackURL"));

            RestTemplate restTemplate=new RestTemplate();
            Map<String,String> param=new HashMap<>();
            param.put("no",algorithm.getAlgorithmNo());

            ResponseEntity<String> forEntity = restTemplate.getForEntity(
                    "http://localhost:8001/support/kafka/alg?no={no}",
                    String.class,
                    param
            );

            logger.info("Send msg to kafka: listen to the compileStatus of alg-"+algorithm.getAlgorithmNo());
        } else {
            info.put("code", "500");
            info.put("msg", "算法创建失败");
        }

        return info;
    }

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public JSONObject run(@RequestBody JSONObject jsonObject) {
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 2) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        Map<String, String> map = algorithmManager.run(jsonObject.getString("algorithmNo"), jsonObject.getString("id"));

        if (null != map && "1".equals(map.get("algorithmStatus"))) {
            info.put("code", "200");
            info.put("msg", "启动算法测试");

            JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(map));
            data.put("algorithmStatus", Integer.parseInt(map.get("algorithmStatus")));
            info.put("data", data);
        } else {
            info.put("code", "500");
            info.put("msg", "算法测试失败");
        }
        return info;
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public JSONObject stop(@RequestBody JSONObject jsonObject) {
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 2) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        int algorithmStatus = algorithmManager.stop(jsonObject.getString("algorithmNo"), jsonObject.getString("id"));

        if (algorithmStatus == 2) {
            info.put("code", "200");
            info.put("msg", "停止算法测试");
        } else if (algorithmStatus == 1) {
            info.put("code", "500");
            info.put("msg", "停止算法测试失败");
        } else {
            info.put("code", "500");
            info.put("msg", "没有该算法");
            return info;
        }

        JSONObject data = new JSONObject();
        data.put("algorithmStatus", algorithmStatus);
        info.put("data", data);
        return info;
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public JSONObject remove(@RequestBody JSONObject jsonObject) {
        JSONObject info = new JSONObject();
        if (jsonObject.size() != 1) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        boolean success = algorithmManager.delete(jsonObject.getString("algorithmNo"));

        if (success) {
            info.put("code", "200");
            info.put("msg", "算法删除成功");
        } else {
            info.put("code", "500");
            info.put("msg", "算法删除失败");
        }
        return info;
    }

    @RequestMapping(value = "/compileState", method = RequestMethod.GET)
    public JSONObject compileState(@RequestParam(value = "algorithmNo") String algorithmNo) {
        JSONObject info = new JSONObject();
        if (null == algorithmNo) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }

        Map<String, String> map = algorithmManager.compileState(algorithmNo);

        if (null != map) {
            info.put("code", "200");
            info.put("msg", "请求成功");

            JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(map));
            data.put("compileStatus", Integer.parseInt(map.get("compileStatus")));
            info.put("data", data);
        } else {
            info.put("code", "500");
            info.put("msg", "没有该算法");
        }


        return info;
    }


    @RequestMapping(value = "/algorithmState", method = RequestMethod.GET)
    public JSONObject algorithmState(@RequestParam(value = "algorithmNo") String algorithmNo,
                                     @RequestParam(value = "id") String id) {
        JSONObject info = new JSONObject();
        if (null == algorithmNo || null==id) {
            info.put("code", "401");
            info.put("msg", "请求错误");
            return info;
        }


        Map<String, String> map = algorithmManager.algorithmState(algorithmNo, id);

        if (null != map) {
            info.put("code", "200");
            info.put("msg", "请求成功");

            JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(map));
            data.put("algorithmStatus", Integer.parseInt(map.get("algorithmStatus")));
            info.put("data", data);
        } else {
            info.put("code", "500");
            info.put("msg", "没有该算法");
        }

        return info;
    }

}
