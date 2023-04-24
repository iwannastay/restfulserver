package com.iws.engineserver;


import com.iws.engineserver.service.ClusterModel.MonitoringManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class EngineServerApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(EngineServerApplication.class, args);

        MonitoringManager monitoringManager = run.getBean(MonitoringManager.class);
        MeterRegistry registry = run.getBean(MeterRegistry.class);
        monitoringManager.init(registry);

    }
}
