/*
 * Copyright 2021 LS All right reserved. This software is the
 * confidential and proprietary information of LS ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with LS
 */

package com.iws.engineclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author 59699
 * @date 2021/3/28 14:05
 */
@RestController
public class ClientController {
    @Autowired
    String url;

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/testofclient",method = RequestMethod.GET)
    public String buildConnection(){
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        return forEntity.getBody();
    }
}
