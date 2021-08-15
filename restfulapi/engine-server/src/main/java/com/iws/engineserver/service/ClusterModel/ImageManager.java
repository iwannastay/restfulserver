package com.iws.engineserver.service.ClusterModel;

import com.iws.engineserver.pojo.DockerConnecter;
import com.iws.engineserver.pojo.HarborClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ImageManager {

    HarborClient harborClient;

    String dockerHost;

    String username;

    String password;

    String repositoryAddress;

    String persistence;

    String tmp;

    String labelFormat="yyyy.MM.dd-HH.mm.ss";

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

    @Value("${image.dockerhost}")
    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }
    @Value("${image.username}")
    public void setUsername(String username) {
        this.username = username;
    }
    @Value("${image.password}")
    public void setPassword(String password) {
        this.password = password;
    }
    @Value("${image.repositoryAddress}")
    public void setRepositoryAddress(String repositoryAddress) {
        this.repositoryAddress = repositoryAddress;
    }
    @Value("${image.persistence}")
    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }
    @Value("${image.tmp}")
    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    @Autowired
    org.slf4j.Logger logger;

    DockerConnecter dockerConnecter;

    public DockerConnecter getDockerConnecter() {
        if(null==dockerConnecter)
            dockerConnecter =new DockerConnecter(dockerHost,username,password,repositoryAddress);
        return dockerConnecter;
    }

    public DockerConnecter setDockerConnecter(String dockerhost) {
        dockerConnecter =new DockerConnecter(dockerhost,username,password,repositoryAddress);
        return dockerConnecter;
    }

    public ImageManager() {
        harborClient =new HarborClient(repositoryAddress);
    }





    public ImageManager(String dockerHost, String username, String password, String repositoryAddress, String persistence, String tmp) {

        this.dockerHost = dockerHost;
        this.username = username;
        this.password = password;
        this.repositoryAddress = repositoryAddress;
        this.persistence = persistence;
        this.tmp = tmp;

        harborClient =new HarborClient(repositoryAddress);
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
        return harborClient.deleteArtifact(imageName);
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

    public static void main(String[] args) throws InterruptedException {
        ImageManager imageManager = new ImageManager("tcp://10.16.156.137:2375","admin","123456","10.16.17.92:8433","public","tmp");
//        logger.info(imageManager.pullImage("12032481/restfulserver:latest"));
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

        imageManager.commitImageToHarbor("qwe","busybox:"+sdf.format(date), imageManager.tmp);
    }

}
