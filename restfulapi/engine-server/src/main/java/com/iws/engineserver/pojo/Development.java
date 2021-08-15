package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Development {
    private String user;
    private String name;
    private String baseImage;
    private String memory;
    private String cpuNumbers;
    private String command;
    private int gpuNumbers;
    private int containerPort;
    private int servicePort;


    //created info
    private String containerId;
    private String status;
    private String node;
    private List<String> checkpoint;
    private String defaultImage;

    public List<String> getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(List<String> checkpoint) {
        this.checkpoint = checkpoint;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public static JSONObject testExample;

    static{
        testExample = new JSONObject();
        testExample.put("user","iws");
        testExample.put("name","dev-iws");
        testExample.put("image","10.16.17.92:8433/library/ubuntu16.04-ssh:latest");

        testExample.put("container_port",80);
        testExample.put("service_port", 80);
        testExample.put("command","/bin/bash");

        testExample.put("memory","100Mi");
        testExample.put("cpu_numbers","200m");
        testExample.put("gpu_numbers",0);
    }

    public Development() {
        checkpoint=new ArrayList<>();
    }


    public Development(Document document) {
        Development dev =JSONObject.toJavaObject(JSONObject.parseObject(document.toJson()), Development.class);
        this.user = dev.getUser();
        this.name = dev.getName();
        this.baseImage = dev.getBaseImage();
        this.memory = dev.getMemory();
        this.cpuNumbers = dev.getCpuNumbers();
        this.gpuNumbers = dev.getGpuNumbers();
        this.containerPort = dev.getContainerPort();
        this.servicePort = dev.getServicePort();
        this.command = dev.getCommand();

        this.containerId = dev.getContainerId();
        this.status = dev.getStatus();
        this.node = dev.getNode();
        this.checkpoint = dev.getCheckpoint();
        this.defaultImage = dev.getDefaultImage();

        if(checkpoint==null)
            checkpoint=new ArrayList<>();
    }

    public Development(JSONObject jsonObject) {
        Development dev =JSONObject.toJavaObject(jsonObject, Development.class);
        this.user = dev.getUser();
        this.name = dev.getName();
        this.baseImage = dev.getBaseImage();
        this.memory = dev.getMemory();
        this.cpuNumbers = dev.getCpuNumbers();
        this.gpuNumbers = dev.getGpuNumbers();
        this.containerPort = dev.getContainerPort();
        this.servicePort = dev.getServicePort();
        this.command = dev.getCommand();

        this.containerId = dev.getContainerId();
        this.status = dev.getStatus();
        this.node = dev.getNode();
        this.checkpoint = dev.getCheckpoint();
        this.defaultImage = dev.getDefaultImage();

        if(checkpoint==null)
            checkpoint=new ArrayList<>();
    }


    public static void main(String[] args) {
        List<String>  a= new ArrayList<>();
        a.add("123");
        a.add("12454");
        a.remove("1142");


    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCpuNumbers() {
        return cpuNumbers;
    }

    public void setCpuNumbers(String cpuNumbers) {
        this.cpuNumbers = cpuNumbers;
    }

    public int getGpuNumbers() {
        return gpuNumbers;
    }

    public void setGpuNumbers(int gpuNumbers) {
        this.gpuNumbers = gpuNumbers;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public Development extractInfo(V1Pod pod){
        V1ObjectMeta metadata = pod.getMetadata();
        setName(metadata.getName());
        setUser(metadata.getLabels().get("user"));

        V1PodStatus status = pod.getStatus();
        setContainerId(status.getContainerStatuses().get(0).getContainerID().substring(9,22));
        setNode(status.getHostIP());
        setStatus(status.getPhase());


        if (pod.getSpec().getContainers().get(0).getResources().getRequests()!=null){
            Map<String, Quantity> requests = pod.getSpec().getContainers().get(0).getResources().getRequests();
            if(requests.get("cpu")!=null)
                setCpuNumbers(requests.get("cpu").getNumber().toString());

            if(requests.get("nvidia.com/gpu")!=null)
                setGpuNumbers(requests.get("nvidia.com/gpu").getNumber().intValue());

            if(requests.get("memory")!=null)
                setMemory(requests.get("memory").getNumber().divide(new BigDecimal(1048576)).toString()+"Mi");

        }
        return this;
    }

    public Document toDocument(){
        return Document.parse(JSONObject.toJSONString(this));
    }


    public void addCheckpoint(String name){
        checkpoint.add(name);
        defaultImage=name;
    }

    public void removeCheckpoint(String name){
        checkpoint.remove(name);
    }

    public void removeAllCheckpoint(boolean clear){
        checkpoint.clear();
        if(!clear&&defaultImage!=null){
            checkpoint.add(defaultImage);
        }else defaultImage=null;
    }

    @Override
    public String toString() {
        return "Development{" +
                "user='" + user + '\'' +
                ", name='" + name + '\'' +
                ", image='" + baseImage + '\'' +
                ", memory='" + memory + '\'' +
                ", cpuNumbers='" + cpuNumbers + '\'' +
                ", command='" + command + '\'' +
                ", gpuNumbers=" + gpuNumbers +
                ", containerPort=" + containerPort +
                ", servicePort=" + servicePort +
                ", containerId='" + containerId + '\'' +
                ", status='" + status + '\'' +
                ", node='" + node + '\'' +
                ", checkpoint=" + checkpoint +
                ", defaultImage='" + defaultImage + '\'' +
                '}';
    }

}
