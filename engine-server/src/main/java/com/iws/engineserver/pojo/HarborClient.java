package com.iws.engineserver.pojo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HarborClient {
    String authorization;
    String address;

    public HarborClient(String address) {
//        String base64encodedString = Base64.getEncoder().encodeToString("admin:123456".getBytes(StandardCharsets.UTF_8));
//        byte[] base64decodedBytes = Base64.getDecoder().decode(base64encodedString);
        authorization = "Basic "+ Base64.getEncoder().encodeToString("admin:123456".getBytes(StandardCharsets.UTF_8));
        this.address=address;
    }

    public HarborClient(String username, String password) {
        authorization = "Basic "+ Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));
    }



    // user hasArtifact() before this to check
    public boolean deleteArtifact(String imageName){
        String[] split = imageName.split("/");
        if(split.length!=3) return false;
        String address=split[0];
        String project=split[1];
        if(project.equals("library")) return false;
        String[] artifact = split[2].split(":");
        if(artifact.length!=2) return false;
        String repository=artifact[0];
        String tag=artifact[1];
        String url="http://"+address+"/api/v2.0/projects/"+project+"/repositories/"+repository+"/artifacts/"+tag;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("authorization", authorization);
        HttpEntity<JSONObject> request = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> resultEntity = restTemplate.exchange(
//                "http://10.16.97.52:8433/api/v2.0/projects/tmp/repositories/busybox/artifacts/2021.06.20-20.41.50",
                url,
                HttpMethod.DELETE,
                request,
                Map.class);
        return resultEntity.getStatusCode().is2xxSuccessful();
    }

    public boolean hasArtifact(String imageName){
        String[] split = imageName.split("/");
        if(split.length!=3) return false;
        String address=split[0];
        String project=split[1];
        String[] artifact = split[2].split(":");
        if(artifact.length!=2) return false;
        String repository=artifact[0];
        String tag=artifact[1];
        String url="http://"+address+"/api/v2.0/projects/"+project+"/repositories/"+repository+"/artifacts";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("authorization", authorization);
        HttpEntity<JSONObject> request = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ArrayList> resultEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ArrayList.class);
        for(Map one:(List<Map>)resultEntity.getBody()){
            if(((List<Map>)one.get("tags")).get(0).get("name").equals(tag))return true;
        }


        return false;
    }

    public List<JSONObject> getArtifacts(String project,String repository){
        String url="http://"+address+"/api/v2.0/projects/"+project+"/repositories/"+repository+"/artifacts?page=1&page_size=10&with_tag=true";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("authorization", authorization);
        HttpEntity<JSONObject> request = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ArrayList> resultEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ArrayList.class);

        List<LinkedHashMap> artifacts=resultEntity.getBody();
        List<JSONObject> objects=new LinkedList<>();
        for(LinkedHashMap artifact:artifacts){
            objects.add( new JSONObject(){{
                put("name", project + "/" + repository);
                if(((ArrayList)artifact.get("tags"))==null)
                    put("tag", "<none>");
                else
                    put("tag",((LinkedHashMap) ((ArrayList)artifact.get("tags")).get(0)).get("name").toString());
                put("imageID", ((String)artifact.get("digest")).substring(7, 19));
                put("created", ((LinkedHashMap) artifact.get("extra_attrs")).get("created").toString());
                put("size", (new BigInteger(artifact.get("size").toString())).divide(new BigInteger("1048576"))+"MB");

            }}
            );
        }

        return objects;
    }

    public List<JSONObject> listImage(String project, int page, int page_size){
        ArrayList<JSONObject> images = new ArrayList<>();

        String url="http://"+address+"/api/v2.0/projects/"+project+"/repositories/"+"?page="+page+"&page_size="+page_size;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("authorization", authorization);
        HttpEntity<JSONObject> request = new HttpEntity<>(httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ArrayList> resultEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                ArrayList.class);

        List<LinkedHashMap> body = resultEntity.getBody();
        Iterator<LinkedHashMap> iterator = body.iterator();
        while (iterator.hasNext()){
            LinkedHashMap repository = iterator.next();
            String name = (String) repository.get("name");
            List<JSONObject> artifacts = getArtifacts(project, name.substring(project.length() + 1));
            images.addAll(artifacts);
        }

        return images;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String imageName="10.16.97.52:8433/tmp/dev1:2021.06.23-01.43.50";

        HarborClient harborClient = new HarborClient("10.16.97.52:8433");
        VisualTool.showList(harborClient.listImage("public",1,100),new ArrayList<>(Arrays.asList("name","tag","imageID","created","size")));
    }
}
