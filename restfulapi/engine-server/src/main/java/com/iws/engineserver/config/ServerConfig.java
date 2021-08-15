package com.iws.engineserver.config;

import com.iws.engineserver.EngineServerApplication;
import com.iws.engineserver.pojo.Cluster;
import com.iws.engineserver.pojo.NfsManager;
import com.iws.engineserver.service.ClusterModel.DevelopmentManager;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;

@Configuration
public class ServerConfig {

    @Value("${k8s.devPath}")
    String configPath;

    @Bean
    Cluster getCluster() throws IOException, ApiException { return new Cluster(configPath);}

    @Bean
    NfsManager getNFSManager(){
        return new NfsManager();
    }

    @Bean
    Logger getLogger(){
        return  org.slf4j.LoggerFactory.getLogger(EngineServerApplication.class);
    }

//    @Bean
//    UserManagerImpl userManagerImpl(){ return new UserManagerImpl();}
//
//    @Bean
//    UsersImpl userImpl(){return new UsersImpl();}
//
//
//    @Bean
//    RequestsImpl requestsImpl(){return new RequestsImpl();}
}
