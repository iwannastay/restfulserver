package com.iws.engineserver.service.ClusterModel;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.ClusterModel.AlgorithmImp;
import com.iws.engineserver.dao.ClusterModel.DevelopmentImp;
import com.iws.engineserver.pojo.Algorithm;
import com.iws.engineserver.pojo.Development;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Aspect
@Service
public class MonitoringManager {
    private final Logger logger = LoggerFactory.getLogger(MonitoringManager.class);

    @Autowired
    AlgorithmImp algorithmImp;

    @Autowired
    DevelopmentImp developmentImp;

//    int aAlg,sAlg,cAlg,fAlg,tAlg;
//    int aDev,rDev,sDev;
//    int Deploy;

    private final AtomicInteger aAlg,sAlg,cAlg,fAlg,tAlg;
    private final AtomicInteger aDev,rDev,sDev;
    private final Counter compileStateQueryCount;

    public Counter compileStateQueryCount() {
        return compileStateQueryCount;
    }

    @Autowired
    public MonitoringManager(MeterRegistry registry) {
        aAlg=registry.gauge("algorithm_summary", new AtomicInteger(0));
        sAlg=registry.gauge("algorithm_success", new AtomicInteger(0));
        cAlg=registry.gauge("algorithm_compile", new AtomicInteger(0));
        fAlg=registry.gauge("algorithm_failed", new AtomicInteger(0));
        tAlg=registry.gauge("algorithm_test", new AtomicInteger(0));

        aDev=registry.gauge("development_summary", new AtomicInteger(0));
        rDev=registry.gauge("development_running", new AtomicInteger(0));
        sDev=registry.gauge("development_stopped", new AtomicInteger(0));

        compileStateQueryCount = registry.counter("compile_state_query_count", "creator", "iws");

    }

    //ALG
    @Pointcut(value = "execution(public * com.iws.engineserver.controller.AlgorithmController.*(*)) ")
    public void reuquest(){}
    @After(value = "reuquest()")
    public void updateAlg(){
        compileStateQueryCount().increment();

        aAlg.set(0);
        sAlg.set(0);
        cAlg.set(0);
        fAlg.set(0);
        tAlg.set(0);
        List<Algorithm> algorithms = algorithmImp.listAlgorithm();
        for(Algorithm algorithm:algorithms){
            int compileStatus = algorithm.getCompileStatus();
            switch (compileStatus){
                case 1:
                    cAlg.incrementAndGet();
                    break;
                case 2:
                    sAlg.incrementAndGet();
                    break;
                case 3:
                    fAlg.incrementAndGet();
                    break;
            }
            if(algorithm.getTestList()!=null&&algorithm.getTestList().size()!=0)
                tAlg.incrementAndGet();
            aAlg.incrementAndGet();
        }
    }

    //DEV
    @Pointcut(value = "execution(public * com.iws.engineserver.controller.DevelopmentController.createContainer(*)) ")
    public void createContainer(){}
    @Pointcut(value = "execution(public * com.iws.engineserver.controller.DevelopmentController.removeContainer(*)) ")
    public void removeContainer(){}
    @Pointcut(value = "execution(public * com.iws.engineserver.controller.DevelopmentController.startContainer(*)) ")
    public void startContainer(){}
    @Pointcut(value = "execution(public * com.iws.engineserver.controller.DevelopmentController.stopContainer(*)) ")
    public void stopContainer(){}

    @AfterReturning(returning = "ret",value = "createContainer()")
    public void createDev(JoinPoint joinPoint, Object ret){
        JSONObject jsonObject =(JSONObject) ret;
        String code = jsonObject.getString("code");
        if(code!=null&&code.equals("200")) {
            aDev.incrementAndGet();
            rDev.incrementAndGet();
        }
    }

    @Around(value = "removeContainer()")
    public Object removeDev(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = ((JSONObject) joinPoint.getArgs()[0]).getString("name");
        Development development = developmentImp.getDevelopment(name);
        if(development==null) return joinPoint.proceed();

        String status = development.getStatus();

        JSONObject ret = (JSONObject) joinPoint.proceed();

        String code = ret.getString("code");
        if(code!=null&&code.equals("200")) {
            aDev.decrementAndGet();

            if(status!=null&&status.equals("Running"))
                rDev.decrementAndGet();
            else if(status!=null&&status.equals("Stopped"))
                sDev.decrementAndGet();
        }
        return ret;
    }

    @AfterReturning(returning = "ret",value = "startContainer()")
    public void startDev(JoinPoint joinPoint, Boolean ret){
        if(ret!=null&&ret) {
            sDev.decrementAndGet();
            rDev.incrementAndGet();
        }
    }

    @AfterReturning(returning = "ret",value = "stopContainer()")
    public void stopDev(JoinPoint joinPoint, String ret){
        if(ret!=null&&ret.contains("Stopped")) {
            sDev.incrementAndGet();
            rDev.decrementAndGet();
        }
    }

    @Pointcut(value = "execution(public * com.iws.engineserver.controller.DevelopmentController.listContainer(String,int ,int)) ")
    public void listContainer(){}
    @AfterReturning(returning = "ret",value = "listContainer()")
    public void updateDev(JoinPoint joinPoint, List<JSONObject> ret){
        if(ret==null) return;

        String user = (String)Arrays.asList(joinPoint.getArgs()).get(0);
        aDev.set(0);
        rDev.set(0);
        sDev.set(0);
        List<Development> developments = developmentImp.listDevelopment(user);
        for(Development development:developments){
            String status = development.getStatus();
            switch (status){
                case "Running":
                    rDev.incrementAndGet();
                    break;
                case "Stopped":
                    sDev.incrementAndGet();
                    break;
                case "Pending":
                    break;
            }
            aDev.incrementAndGet();
        }
    }
}
