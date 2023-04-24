package com.iws.engineserver.service.ClusterModel;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.DockerConnecter;
import com.iws.engineserver.pojo.HarborClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ImageManager {


    @Value("${image.dockerHost}")
    private String dockerHost;
    @Value("${image.username}")
    private String username;
    @Value("${image.password}")
    private String password;
    @Value("${image.repositoryAddress}")
    private String repositoryAddress;
    @Value("${image.persistence}")
    private String persistence;
    @Value("${image.tmp}")
    private String tmp;

    private final String labelFormat="yyyy.MM.dd-HH.mm.ss";

    public String getDockerHost() {
        return dockerHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRepositoryAddress() {
        return repositoryAddress;
    }

    public String getPersistence() {
        return persistence;
    }

    public String getTmp() {
        return tmp;
    }

    public String getLabelFormat() {
        return labelFormat;
    }



    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRepositoryAddress(String repositoryAddress) {
        this.repositoryAddress = repositoryAddress;
    }

    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    @Autowired
    org.slf4j.Logger logger;

    DockerConnecter dockerConnecter;
    HarborClient harborClient;

    public DockerConnecter getDockerConnecter() {
        if(null==dockerConnecter)
            dockerConnecter =new DockerConnecter(dockerHost,username,password,repositoryAddress);
        return dockerConnecter;
    }

    public void setDockerConnecter(String dockerhost) {
        dockerConnecter =new DockerConnecter(dockerhost,username,password,repositoryAddress);
    }

    public HarborClient getHarborClient() {
        if(null==harborClient)
            harborClient =new HarborClient(repositoryAddress);
        return harborClient;
    }

    public ImageManager() {}

    public ImageManager(String dockerHost, String username, String password, String repositoryAddress, String persistence, String tmp) {

        this.dockerHost = dockerHost;
        this.username = username;
        this.password = password;
        this.repositoryAddress = repositoryAddress;
        this.persistence = persistence;
        this.tmp = tmp;

    }

    public String pullImageToHarbor(String imageName) throws InterruptedException {
        String artifact;
        int pos = imageName.lastIndexOf('/');
        if(pos==-1)
            artifact=imageName;
        else
            artifact=imageName.substring(pos);
        if(-1==artifact.lastIndexOf(':'))
            imageName+=":latest";

        getDockerConnecter().pullImage(imageName);
        String newName = getDockerConnecter().pushImage(imageName,repositoryAddress+"/"+persistence);

        getDockerConnecter().deleteLocalImage(imageName);
        if(!imageName.equals(newName))
            getDockerConnecter().deleteLocalImage(newName);
        return newName;
    }

    public boolean deleteImageFromHarbor(String imageName){
        //getDockerConnecter().deleteLocalImage(imageName);
        if(getHarborClient().hasArtifact(imageName))
            return getHarborClient().deleteArtifact(imageName);
        return false;
    }


    // imageName should be simple like something:label, not a/b/c/sth:label
    // isAuto = persistence or tmp
    // persistence: user commit image
    // tmp: auto save checkpoint
    public String commitImageToHarbor(String containerID, String imageName,String isAuto) throws InterruptedException {
        String imageID = getDockerConnecter().commitImage(containerID);

        // to ensure name is simple
        // -1 + 1 = 0
        imageName=imageName.substring(imageName.lastIndexOf('/')+1);

        String[] split = imageName.split(":");
        String label=split[split.length-1];
        String repository=repositoryAddress+"/"+isAuto;
        String saveName=repository+"/"+imageName;

        getDockerConnecter().getClient().tagImageCmd(imageID,saveName,label).exec();

        String newImage = getDockerConnecter().pushImage(saveName, repository);
        getDockerConnecter().deleteLocalImage(saveName);

        return saveName;
    }

    public String getImage(String name){
        //TODO: from harbor
        return getDockerConnecter().getImage(name);
    }

    public List<JSONObject> listImage(String project,int page,int page_size){
        return getHarborClient().listImage(project,page,page_size);
    }

    public static void main(String[] args) throws InterruptedException {
        ImageManager imageManager = new ImageManager("tcp://10.16.156.137:2375","admin","123456","10.16.97.52:8433","public","tmp");
//        logger.info(imageManager.pullImage("12032481/restfulserver:latest"));
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

        imageManager.commitImageToHarbor("qwe","busybox:"+sdf.format(date), imageManager.tmp);
    }

}
