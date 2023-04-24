package com.iws.engineserver.dao.ClusterModel;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.MongoConnecter;
import com.iws.engineserver.pojo.Deployment;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;


@Repository
public class DeploymentImp extends MongoConnecter {

    static private List<String> deployments = null;
    private MongoCollection<Document> defaultCollection = null;

    public DeploymentImp() {
        setDefaultCollection();
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Deployments");
    }

    public void setDefaultCollection(String databaseName, String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }



    public static void main(String[] args) {
        Deployment example=null;
        {
            example = new Deployment();
            example.setName("test"+new Date());
            example.setImages(new ArrayList<>(){{
                add("image1");
                add("image2");
                add("image3");
            }});
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cjq","erzi");
            jsonObject.put("ls","baba");

            JSONObject jsonObject2= new JSONObject();
            jsonObject2.put("cjq","erzi");
            jsonObject2.put("ls","yeye");
            Map<String,JSONObject> map = new HashMap<>();
            map.put("config1",jsonObject);
            map.put("config2",jsonObject2);
            example.setConfigs(map);
        }

        DeploymentImp deploymentImp = new DeploymentImp();

        List<Deployment> deployments = deploymentImp.listDeployment();
        for(Deployment dp: deployments){
            System.out.println(dp.getApplications().get("app1"));
            System.out.println(dp.getApplications().get("app1").getTaskID());
            System.out.println(dp);
            deploymentImp.deleteDeployment(dp.getName());
        }

    }

    public boolean addDeployment(Deployment deployment){
        Document document = defaultCollection.find(
                eq("name", deployment.getName())
        ).first();
        if (document != null) {
            return false;
        }
        defaultCollection.insertOne(deployment.toDocument());
        return true;
    }


    public boolean deleteDeployment(String deploymentName){
        Document document = defaultCollection.find(eq("name",deploymentName)).first();
        if(document==null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() !=0;
    }

    public boolean updateDeployment(Deployment deployment){

        UpdateResult updateResult = defaultCollection.updateOne(
                eq("name",deployment.getName()),
                new Document("$set",deployment.toDocument()));
        return updateResult.getModifiedCount() != 0;
    }

    public Deployment getDeployment(String deploymentName){
        Document document = defaultCollection.find(
                eq("name", deploymentName)
        ).first();
        if (document != null) {
            return new Deployment(document);
        }
        return null;
    }

    public List<Deployment> listDeployment(){
        List<Deployment> deployments = new ArrayList<>();
        FindIterable<Document> documents = defaultCollection.find();
        for (Document document : documents) {
            deployments.add(new Deployment(document));
        }
        return deployments;

    }
}
