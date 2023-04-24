package com.iws.engineserver.service.ClusterModel;

import com.iws.engineserver.dao.ClusterModel.DevelopmentImp;
import com.iws.engineserver.dao.ClusterModel.PortImp;
import com.iws.engineserver.pojo.Cluster;
import com.iws.engineserver.pojo.Development;
import com.iws.engineserver.pojo.HarborClient;
import com.iws.engineserver.pojo.Interface.StorageManager;
import com.iws.engineserver.pojo.NfsManager;
import io.kubernetes.client.openapi.models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DevelopmentManager {
    @Autowired
    org.slf4j.Logger logger;

    @Value("${dev.namespace}")
    String devNamespace;

    @Autowired
    Cluster cluster;

    @Autowired
    PortImp portImp;

    @Autowired
    DevelopmentImp developmentImp;

    @Autowired
    NfsManager nfsManager;

    @Autowired
    ImageManager imageManager;


    public DevelopmentImp getDevelopmentImp() {
        return developmentImp;
    }

    //NOTES:
    //sed -i 's/^PermitRootLogin.*$/PermitRootLogin Yes/g' /etc/ssh/sshd_config
    //ssh root@10.16.11.109 -p 30791  pod-container bu kai fang 22 duan kou ye ke yi
    //modify .bashrc works with 'docker run' but doesn't with k8s, need to add command/args: "/bin/bash -c 'service ssh start && {sleep func}'"
    public Development createDevContainer(Development development) {
        //DB check
        Development exist = developmentImp.getDevelopment(development.getName());
        if (null != exist) return null;

        //St
        nfsManager.createMenu(development.getUser(), StorageManager.Info.TYPE.USER);

        //k8s
        Integer nodePort = portImp.getPort(0);
        boolean success = cluster.createDeploymentAndService(development, nodePort, devNamespace);

        if (success) {
            developmentImp.addDevelopment(development);
            logger.info("create dev container: " + development.getName() + " of " + development.getUser() + " " + success);
            return development;
        } else {
            //St
            nfsManager.deleteMenu(development.getUser(), StorageManager.Info.TYPE.USER);

            boolean b1 = false, b2 = false, b3 = false;

            //k8s
            String name = development.getName();
            V1Deployment dep = cluster.getDeployment(name, devNamespace);
            if (dep != null) {
                b1 = cluster.deleteDeployment(name, devNamespace);
            }

            V1Service svc = cluster.getSvc(name, devNamespace);
            if (svc == null || cluster.deleteSvc(name, devNamespace)) {
                portImp.releasePort(nodePort);
            }
        }

        return null;
    }

    public boolean deleteDevContainer(String name) {
        //DB check
        Development development = developmentImp.getDevelopment(name);
        if (null == development) return false;

        boolean b1 = false, b2 = false, b3 = false;

        V1Deployment dep = cluster.getDeployment(name, devNamespace);
        if (dep != null) {
            b1 = cluster.deleteDeployment(name, devNamespace);
        } else b1 = true;

        V1Service svc = cluster.getSvc(name, devNamespace);
        if (svc != null) {
            Integer nodePort = svc.getSpec().getPorts().get(0).getNodePort();
            b2 = cluster.deleteSvc(name, devNamespace);
            if (b2) {
                portImp.releasePort(nodePort);
            }
        } else b2 = true;

        //DB
        if (b1 && b2) {
            b3 = developmentImp.deleteDevelopment(name);
            logger.info("delete devContainer DB: " + name + " " + b1);
        }

        //St
        boolean b = nfsManager.deleteMenu(development.getUser(), StorageManager.Info.TYPE.USER);
        logger.info("delete devContainer menu: " + name + " " + b);

        //imageGC
        List<String> checkpoints = development.getCheckpoint();
        for(String checkpoint:checkpoints){
            imageManager.deleteImageFromHarbor(checkpoint);
        }

        return b3;
    }

    public boolean startDevContainer(String user, String name) {
        //TODO: how to start

        //DB check
//        Development exist = developmentImp.getDevelopment(name);
//        if(null==exist) return false;
        Development exist = updateStatus(user, name);
        if (null == exist || !"Stopped".equals(exist.getStatus())) return false;

        logger.info(name + " " + user);
        //St
        nfsManager.createMenu(user, StorageManager.Info.TYPE.USER);

        //k8s
        Integer nodePort = portImp.getPort(0);
        boolean success = cluster.createDeploymentAndService(exist, nodePort, devNamespace);


        if (success) {
            updateStatus(user, name);
            logger.info("start dev container: " + exist.getName() + " of " + user + " " + success);
            return true;
        } else {
            //St
            nfsManager.deleteMenu(user, StorageManager.Info.TYPE.USER);

            //k8s
            V1Deployment dep = cluster.getDeployment(name, devNamespace);
            if (dep != null) {
                cluster.deleteDeployment(name, devNamespace);
            }

            V1Service svc = cluster.getSvc(name, devNamespace);

            if (svc == null || !nodePort.equals(svc.getSpec().getPorts().get(0).getNodePort()) || cluster.deleteSvc(name, devNamespace)) {
                portImp.releasePort(nodePort);
            }
        }

        return false;
    }

    //0-suc;1-fail;2-wait
    public int stopDevContainer(String user, String name) {
        //TODO: how to stop
        //DB check

//        Development development = developmentImp.getDevelopment(name);
//        if(null==development) return false;
        Development exit = updateStatus(user, name);
        if (null == exit) {
            logger.info("DB clear but pod is terminating");
            return 1;
        }
        if ("Stopped".equals(exit.getStatus())) return 0;

        boolean b1 = false, b2 = false, b3 = false;

        V1Deployment dep = cluster.getDeployment(name, devNamespace);
        if (dep != null) {
            b1 = cluster.deleteDeployment(name, devNamespace);
        } else b1 = true;

        V1Service svc = cluster.getSvc(name, devNamespace);
        if (svc != null) {
            Integer nodePort = svc.getSpec().getPorts().get(0).getNodePort();
            b2 = cluster.deleteSvc(name, devNamespace);
            if (b2) {
                portImp.releasePort(nodePort);
            }
        } else b2 = true;

        //DB
        if (b1 && b2) {
            Development status = updateStatus(user, name);
            b3 = "Stopped".equals(status.getStatus());
            logger.info("stop devContainer: " + name + " " + b1);

            //St
            if(b3){
                boolean b = nfsManager.deleteMenu(exit.getUser(), StorageManager.Info.TYPE.USER);
                logger.info("delete devContainer menu: " + name + " " + b);
                return 0;
            }else return 2;

        }else return 1;

    }

    public Map<String,String> connectDevContainer(String user, String name) {
        //TODO: return ip:port+root:123456
        Development development = updateStatus(user, name);
        if (development == null || !"Running".equals(development.getStatus())) return null;

        String ip,port;
        HashMap<String, String> map = new HashMap<>();

        String labelSelector = "user=" + user;
        List<V1Pod> pods = cluster.getPods(devNamespace, labelSelector);
        List<V1Pod> collect = pods.stream().filter(pod -> pod.getMetadata().getName().startsWith(name)).collect(Collectors.toList());

        if(collect.size()!=1) return null;
        V1Pod pod = collect.get(0);
        ip=pod.getStatus().getHostIP();
        V1Service svc = cluster.getSvc(name, devNamespace);
        if(svc!=null){
            port = svc.getSpec().getPorts().get(0).getNodePort().toString();
            map.put("sshLink",ip+":"+port);
            map.put("username","root");
            map.put("password","123456");
            return map;
        }
        return null;
    }

    public List<Development> listDevContainer(String userName) {
        updateStatus(userName, null);
        return developmentImp.listDevelopment(userName);
    }

    //param [name] is enough, [user] will help to accelerate the update process
    //TODO: only if name belongs to user
    public Development getDevContainer(String user, String name) {
        updateStatus(user, name);
        return developmentImp.getDevelopment(name);
    }

    public String makeCheckpoint(String user, String name) {
        Development development = updateStatus(user, name);
        if (development == null || !"Running".equals(development.getStatus())) return null;

        String labelSelector = "user=" + user;
        List<V1Pod> pods = cluster.getPods(devNamespace, labelSelector);
        List<V1Pod> collect = pods.stream().filter(pod -> pod.getMetadata().getName().startsWith(name)).collect(Collectors.toList());
        if (collect.size() != 1) return null;

        V1Pod pod = collect.get(0);
        V1PodStatus status = pod.getStatus();
        if (status != null) {
            List<V1ContainerStatus> containerStatuses = status.getContainerStatuses();
            if (containerStatuses != null && containerStatuses.size() != 0) {
                String containerID = containerStatuses.get(0).getContainerID().substring(9, 21);
                String hostIP = status.getHostIP();
                String imageName = name + ":" + (new SimpleDateFormat(imageManager.getLabelFormat())).format(new Date());
                try {
                    imageManager.setDockerConnecter("tcp://" + hostIP + ":2375");
                    String saveName = imageManager.commitImageToHarbor(containerID, imageName, imageManager.getTmp());
                    imageManager.setDockerConnecter(imageManager.getDockerHost());
                    // TODO: if has image
                    if (imageManager.harborClient.hasArtifact(saveName)) {
                        development.addCheckpoint(saveName);
                        developmentImp.updateDevelopment(development);
                        return saveName;
                    } else return null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public boolean dropCheckpoint(String user, String name, String image) {
        Development development = developmentImp.getDevelopment(name);
        if(development.getCheckpoint().contains(image)&&!image.equals(development.getBaseImage())) {
            development.removeCheckpoint(image);
            if(image.equals(development.getDefaultImage())){
                int size = development.getCheckpoint().size();
                if(size>0)
                    development.setDefaultImage(development.getCheckpoint().get(size-1));
                else development.setDefaultImage(development.getBaseImage());
            }
            developmentImp.updateDevelopment(development);
            if(imageManager.harborClient.hasArtifact(image))
                return imageManager.harborClient.deleteArtifact(image);
        }

        return false;
    }

    public boolean setDefaultCheckpoint(String user, String name, String image){
        Development development = developmentImp.getDevelopment(name);
        if(development.getCheckpoint().contains(image)&&imageManager.harborClient.hasArtifact(image)){
            development.setDefaultImage(image);
            return developmentImp.updateDevelopment(development);
        }

        return false;
    }

    // node status containerID
    public Development updateStatus(String user, String name) {
        String labelSelector = "user=" + user;
        List<V1Pod> pods = cluster.getPods(devNamespace, labelSelector);
        List<List<String>> collect = pods.stream().filter(pod -> pod.getMetadata().getName().startsWith(null == name ? "" : name)).
                map(pod -> (new ArrayList<String>() {
                    {
                        add(pod.getMetadata().getLabels().get("app"));
                        add(pod.getSpec().getNodeName());
                        add(pod.getStatus().getPhase());
                        if (null != pod.getStatus().getContainerStatuses())
                            if (null != pod.getStatus().getContainerStatuses().get(0).getContainerID())
                                add(pod.getStatus().getContainerStatuses().get(0).getContainerID().substring(9, 21));
                            else add("");
                        else add("");
                    }
                })).
                collect(Collectors.toList());

        int count = 0;
        for (List<String> list : collect) {
            Development development = developmentImp.getDevelopment(list.get(0));
            if (null == development) continue;
            development.setNode(list.get(1));
            development.setStatus(list.get(2));
            development.setContainerId(list.get(3));
            developmentImp.updateDevelopment(development);
            ++count;
        }

        int size = collect.size();
        if (size != 1 && name != null) {
            if (size == 0) {
                // delete resource complete
                Development development = developmentImp.getDevelopment(name);
                if (development == null) {
                    logger.info("update: 0 devContainers");
                    return null;
                }
                development.setStatus("Stopped");
                developmentImp.updateDevelopment(development);
                logger.info("update: 1 empty devContainers");
                return development;
            }
            //more than 1 pod, might stay terminating
            logger.info("please try again later");
            return null;
        }

        //TODO: update stopped containers


        logger.info("update: " + count + " devContainers");
        return developmentImp.getDevelopment(name);
    }


    public static void main(String[] args) {
        Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        logger.error("asd{}asffa","QQQ");
    }
}
