package com.iws.engineserver.service.ClusterModel;


import com.iws.engineserver.dao.ClusterModel.AlgorithmImp;
import com.iws.engineserver.dao.ClusterModel.PortImp;
import com.iws.engineserver.pojo.Algorithm;
import com.iws.engineserver.pojo.Cluster;
import com.iws.engineserver.pojo.Interface.StorageManager.*;
import com.iws.engineserver.pojo.NfsManager;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlgorithmManager {
    @Autowired
    org.slf4j.Logger logger;

    @Autowired
    Cluster cluster;

    @Autowired
    PortImp portImp;


    @Autowired
    AlgorithmImp algorithmImp;

    @Autowired
    NfsManager nfsManager;

    @Value("${alg.namespace}")
    String algNamespace;

    @Value("${nginx.downloadAddress}")
    String downloadAddress;

    String callbackURL;

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }


    public AlgorithmImp getAlgorithmImp() {
        return algorithmImp;
    }

    public NfsManager getNfsManager() {
        return nfsManager;
    }

    public Algorithm compile(Algorithm algorithm){

        //k8s
        V1Job alg = cluster.getJob("alg-" + algorithm.getAlgorithmNo(), algNamespace);
        if(null!=alg) return null;

        //Storage
        Menu menu = nfsManager.createMenu(algorithm.getAlgorithmNo(), Info.TYPE.ALG);
        logger.info(String.valueOf(menu));

        //k8s
        try{
            boolean success = cluster.createJob(algorithm, algNamespace);
            if(success) {
                algorithm.setCompileStatus(1);
                //TODO: The logic here need to be reconsidered
                algorithm.setAlgorithmStatus(2);
                algorithmImp.addAlgorithm(algorithm);
            }else{
                algorithm.setCompileStatus(3);
                //Storage
                nfsManager.deleteMenu(algorithm.getAlgorithmNo(), Info.TYPE.ALG);

            }
        }catch(Exception e){
            e.printStackTrace();
            algorithm.setCompileStatus(3);
            //Storage
            nfsManager.deleteMenu(algorithm.getAlgorithmNo(), Info.TYPE.ALG);
        }


        return algorithm;
    }

    public Map<String,String> run(String algorithmNo,String id){
        //DB
        Algorithm algorithmByNo = algorithmImp.getAlgorithmByNo(algorithmNo);
        if(null==algorithmByNo || algorithmByNo.getTestList().contains(String.valueOf(id))){
            return null;
        }

        //storage
        nfsManager.createMenu(algorithmNo+"/"+id,Info.TYPE.TEST);

        //k8s
        //TODO
        Integer port = portImp.getPort(
//                Integer.parseInt(algorithmNo)*10+Integer.parseInt(id)
                0
        );
        boolean success = cluster.createPodAndService(algorithmByNo,id,port,algNamespace);
        logger.info("Alg test:"+algorithmNo+"/"+id+""+success);

        Map<String, String> data = new HashMap<>();
        data.put("algorithmStatus","2");
        String sshLink="";

        if(success){
            //DB
            algorithmByNo.getTestList().add(id);
            algorithmByNo.setAlgorithmStatus(1);
            algorithmImp.updateAlgorithm(algorithmByNo);

            //return data
            data.put("algorithmStatus","1");

            V1Service svc = cluster.getSvc("test-" + algorithmNo + "-" + id, algNamespace);
            sshLink = "10.16.97.52:"+svc.getSpec().getPorts().get(0).getNodePort();

        }else{
            V1Pod podByName = cluster.getPod("test-" + algorithmNo + "-" + id, algNamespace);
            if(null!=podByName){
                boolean deletePod = cluster.deletePod("test-"+algorithmNo+"-"+id, algNamespace);
                if(deletePod){
                    algorithmByNo.setAlgorithmStatus(2);
                    algorithmImp.updateAlgorithm(algorithmByNo);
                }
            }
            V1Service svc = cluster.getSvc("test-" + algorithmNo + "-" + id, algNamespace);
            if(null!=svc){
                boolean deleteSvc =  cluster.deleteSvc("test-"+algorithmNo+"-"+id, algNamespace);
                if(deleteSvc)
                    portImp.releasePort(port);
            } else portImp.releasePort(port);



            //storage
            nfsManager.deleteMenu(algorithmNo+"/"+id, Info.TYPE.TEST);




        }

        data.put("sshLink",sshLink);
        data.put("sshAccount","root");
        data.put("sshPassword","123456");

        return data;
    }

    public int stop(String algorithmNo,String id){
        //DB
        Algorithm algorithm = algorithmImp.getAlgorithmByNo(algorithmNo);

        if(null==algorithm || !algorithm.getTestList().contains(id)){
            return 0;
        }

        boolean b1=false,b2=false,b3=false,b4=false;

        //k8s
        V1Service svc = cluster.getSvc("test-" + algorithmNo + "-" + id, algNamespace);
        if(null!=svc){
            Integer nodePort = svc.getSpec().getPorts().get(0).getNodePort();
            b1 =  cluster.deleteSvc("test-"+algorithmNo+"-"+id, algNamespace);
            if(b1){
                portImp.releasePort(nodePort);
            }
        }

        V1Pod podByName = cluster.getPod("test-" + algorithmNo + "-" + id, algNamespace);
        if(null!=podByName){
            b2 = cluster.deletePod("test-"+algorithmNo+"-"+id, algNamespace);
        }

        //storage
        if(b2){
            b3=nfsManager.deleteMenu(algorithmNo+"/"+id, Info.TYPE.TEST);
        }


        if(b3){
            algorithm.getTestList().remove(id);
            algorithm.setAlgorithmStatus(2);
            b4=algorithmImp.updateAlgorithm(algorithm);
        }

        return b4? 2:1;
    }

    public boolean delete(String algorithmNo) {
        String JobName = "alg-" + algorithmNo;

        //DB check
        Algorithm algorithm = algorithmImp.getAlgorithmByNo(algorithmNo);
        if (null==algorithm||!algorithm.getTestList().isEmpty()) return false;

        boolean b1=false,b2=false,b3=false;

        //k8s
        V1Job job = cluster.getJob(JobName, algNamespace);
        if (job != null) {
            b1=cluster.deleteJob(JobName, algNamespace);
        }

        //Storage
        if(b1){
            b2 = nfsManager.deleteMenu(algorithmNo, Info.TYPE.ALG);
            logger.info("deleteMenu: "+algorithmNo+" "+b2);
        }

        //DB
        if(b2){
            b3 = algorithmImp.deleteAlgorithm(algorithmNo);
            logger.info("deleteDB: "+algorithmNo+" "+b3);
        }

        return b3;
    }

    public Map<String,String> compileState(String algorithmNo){
        //DB
        Algorithm algorithm = algorithmImp.getAlgorithmByNo(algorithmNo);

        if(null==algorithm) return null;

        //k8s
        //TODO: This func just need to read from db(db is updated by kafka msg),
        // not to read from k8s which is supposed to be used in kafka func.
//        V1Job alg = cluster.getAlg("alg-"+algorithmNo, algNamespace);
//        String status = JSONObject.parseObject(alg.getMetadata().getAnnotations().get("revisions")).getJSONObject("1").getString("status");
//        logger.info(status);
//
//        int compileState;
//        if("running".equals(status)) compileState=1;
//        else if("completed".equals(status)) compileState=2;
//        else compileState=3;
//
//        //DB
//        algorithm.setCompileStatus(compileState);
//        algorithmImp.updateAlgorithm(algorithm);

        int compileState=algorithm.getCompileStatus();
        Map<String, String> data = nfsManager.getAlgMenuTemplate(algorithmNo,null);
        data.put("compileStatus",String.valueOf(compileState));

        return data;
    }

    public Map<String,String> algorithmState(String algorithmNo,String id){
        //DB
        Algorithm algorithm = algorithmImp.getAlgorithmByNo(algorithmNo);

        if(null==algorithm) return null;

        int algorithmState;
        if(algorithm.getTestList().contains(id)) algorithmState=1;
        else algorithmState=2;

        Map<String, String> data = new HashMap<>();
        data.put("algorithmStatus",String.valueOf(algorithmState));

        //k8s
        String sshLink="";
        if(algorithmState==1){
            V1Service svc = cluster.getSvc("test-" + algorithmNo + "-" + id, algNamespace);
            sshLink = "10.16.97.52:"+svc.getSpec().getPorts().get(0).getNodePort();}

        data.put("sshLink",sshLink);
        data.put("sshAccount","root");
        data.put("sshPassword","123456");

        return data;

    }


    public void onCompileComplete(String algorithmNo,Integer compileStatus){
        //DB
        Algorithm algorithm = algorithmImp.getAlgorithmByNo(algorithmNo);

        if(null!=algorithm){
            algorithm.setCompileStatus(compileStatus);
            algorithmImp.updateAlgorithm(algorithm);
        }
    }


}
