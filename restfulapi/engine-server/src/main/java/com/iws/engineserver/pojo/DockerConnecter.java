package com.iws.engineserver.pojo;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DockerConnecter {
    private String dockerhost;
    private DockerClient client;
    private AuthConfig authConfig;

    public String getDockerhost() {
        return dockerhost;
    }

    public void setDockerhost(String dockerhost) {
        this.dockerhost = dockerhost;
    }

    public DockerClient getClient() {
        return client;
    }

    public void setClient(DockerClient client) {
        this.client = client;
    }

    public void setClient(){
        setClient(dockerhost);
    }

    public void setClient(String dockerhost){
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerhost)
                .withRegistryUrl("https://index.docker.io/v1/")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .build();

        client = DockerClientImpl.getInstance(config, httpClient);
    }

    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    public void setAuthConfig(String username, String password, String repositoryAddress){
        authConfig = new AuthConfig()
                .withUsername(username)
                .withPassword(password)
                .withRegistryAddress(repositoryAddress);
    }

    public DockerConnecter() {
        this.dockerhost = "tcp://localhost:2375";
        setClient();
        setAuthConfig("admin","123456","localhost:8433");
    }

    public DockerConnecter(String dockerhost, DockerClient client, AuthConfig authConfig) {
        this.dockerhost = dockerhost;
        this.client = client;
        this.authConfig = authConfig;
    }

    public DockerConnecter(String dockerhost, String username, String password, String repositoryAddress) {
        this.dockerhost = dockerhost;
        setClient();
        setAuthConfig(username,password,repositoryAddress);

    }


    public String getImage(String imageName){
        InspectImageResponse exec = client.inspectImageCmd(imageName).exec();
        return exec.toString();
    }


    public void pullImage(String imageName) throws InterruptedException {
        //TODO: If image not exists
        client.pullImageCmd(imageName).start().awaitCompletion();

    }

    public String pushImage(String imageName, String repository) throws InterruptedException {
        String[] names = imageName.split("/");

        String newImageName= repository+"/"+names[names.length-1];
        String[] split = newImageName.split(":");
        String label= split[split.length-1];

        client.tagImageCmd(imageName,newImageName,label).exec();

        client.pushImageCmd(newImageName).withAuthConfig(authConfig).start().awaitCompletion();

        return newImageName;
    }

    public DockerConnecter deleteLocalImage(String imageName){
        client.removeImageCmd(imageName).exec();
        return this;
    }

    public void loadFromFile(String fileName) throws FileNotFoundException {

        File dockerImageFile = new File(fileName);
        InputStream in = new FileInputStream(dockerImageFile);
        client.loadImageCmd(in).exec();
    }

    public void saveToFile(String imageName,String fileName) throws IOException {
        if(null==fileName){
            String[] split = imageName.split("/");
            String tail=split[split.length-1];
            fileName= tail.split(":")[0]+".rar";
        }
        InputStream inputStream = client.saveImageCmd(imageName).exec();
        writeToLocal(fileName,inputStream);
    }

    public static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        input.close();
        downloadFile.close();
    }

    public String commitImage(String containerID){
        //id or name is ok
         return client.commitCmd(containerID).exec();
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        //pullImage
        String Img="12032481/restfulserver:latest";
        DockerConnecter dockerConnecter = new DockerConnecter("tcp://10.16.156.137:2375","admin","123456","10.16.17.92:8433");
        System.out.println(new Date());
        System.out.println(dockerConnecter.commitImage("qwe"));
        System.out.println(new Date());

//        //pushToHarbor
//        String Img="ubuntu16.04-ssh:latest";
//        DockerConnecter dockerConnecter = new DockerConnecter("tcp://10.16.11.109:2375","admin","123456","10.16.17.92:8433");
//        String imageName=dockerConnecter.pushToHarbor(Img);
//        logger.info(imageName);


////        DockerClientConfig custom = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
//
//        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
////                .withDockerHost("tcp://10.16.156.137:2375")
//                .withDockerHost("tcp://10.16.17.92:2375")
////            .withDockerTlsVerify(true)
////            .withDockerCertPath("/root/.docker")
////                .withRegistryUsername("admin")
////                .withRegistryPassword("123456")
//                .withRegistryUsername("12032481")
//                .withRegistryPassword("425918Ls")
////                .withRegistryEmail("596992831@qq.com")
//                .withRegistryUrl("https://index.docker.io/v1/")
////                .withRegistryUrl("http://10.16.17.92:8433")
//                .build();
//
//        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
//                .maxConnections(100)
//                .build();
//
//
//        DockerClient docker = DockerClientImpl.getInstance(config, httpClient);
//        //DockerClient docker = DockerClientBuilder.getInstance(config).build();
////        Info info = docker.infoCmd().exec();
////        System.out.print(info);
//
////        List<SearchItem> dockerSearch = docker.searchImagesCmd("hello-world").exec();
////        logger.info("Search returned" + dockerSearch.toString());
//
//        String Img="12032481/hello-world:latest";
//        String newImg="10.16.17.92:8433/library/hello-world:latest";
//
//
//        dockerConnecter.getClient().pullImageCmd(Img).start().awaitCompletion();

//        docker.pullImageCmd("docker.io/12032481/plattest:latest").exec(new ResultCallback<PullResponseItem>() {
//            public void onStart(Closeable closeable) {
//                logger.info("start");
//            }
//
//            public void onNext(PullResponseItem object) {
//                logger.info(object.getStatus());
//            }
//
//            public void onError(Throwable throwable) {
//                throwable.printStackTrace();
//            }
//
//            public void onComplete() {
//                logger.info("pull finished");
//            }
//
//            public void close() throws IOException {
//                logger.info("close");
//            }
//        });

//        AuthConfig authConfig = new AuthConfig()
//                .withUsername("admin")
//                .withPassword("123456")
//                .withRegistryAddress("http://10.16.17.92:8433");
//
//
//        dockerConnecter.getClient().tagImageCmd(Img,newImg,newImg.split(":")[1]).exec();
//
//        dockerConnecter.getClient().pushImageCmd("10.16.17.92:8433/library/hello-world:latest").withAuthConfig(authConfig).start().awaitCompletion();


    }

}
