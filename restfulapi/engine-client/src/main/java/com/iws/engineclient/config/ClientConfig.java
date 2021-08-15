/*
 * Copyright 2021 LS All right reserved. This software is the
 * confidential and proprietary information of LS ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with LS
 */

package com.iws.engineclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 59699
 * @date 2021/3/28 14:01
 */
@Configuration
public class ClientConfig {

    @Bean
    String getUrl(){
        return "http://localhost:8001/testofserver";
    }


    @Bean
    RestTemplate restTemplate() {return new RestTemplate();}
}
