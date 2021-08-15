/*
 * Copyright 2021 LS All right reserved. This software is the
 * confidential and proprietary information of LS ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with LS
 */

package com.iws.engineserver.controller;

import com.iws.engineserver.pojo.Cluster;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author 59699
 * @date 2021/3/28 13:50
 */

@RestController
public class MyController {

    @Autowired
    Cluster cluster;



    @RequestMapping(value = {"/","/test","testofserver"},method = RequestMethod.GET)
    public String connection() throws IOException, ApiException {

        List<String> pods = cluster.getPods();
        for (String podName : pods) {
            System.out.println(podName);
        }

        return pods.toString();
    }
}
