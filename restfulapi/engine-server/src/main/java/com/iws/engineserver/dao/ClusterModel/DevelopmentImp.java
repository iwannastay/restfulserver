package com.iws.engineserver.dao.ClusterModel;

import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.MongoConnecter;

import com.iws.engineserver.pojo.Development;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


@Repository
public class DevelopmentImp extends MongoConnecter {
    static private List<Development> developments = null;
    private MongoCollection<Document> defaultCollection = null;

    public DevelopmentImp() {
        setDefaultCollection();
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Developments");
    }

    public void setDefaultCollection(String databaseName, String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    public boolean addDevelopment(Development development){
        Document document = defaultCollection.find(
                eq("name", development.getName())
        ).first();
        if (document != null) {
            return false;
        }

        defaultCollection.insertOne(development.toDocument());
        return true;
    }

    public boolean deleteDevelopment(String name){
        Document document = defaultCollection.find(eq("name",name)).first();
        if(document==null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() !=0;
    }

    public boolean updateDevelopment(Development development){

        UpdateResult updateResult = defaultCollection.updateOne(
                eq("name",development.getName()),
                new Document("$set",development.toDocument()));
        return updateResult.getModifiedCount() != 0;
    }

    public Development getDevelopment(String name){
        Document document = defaultCollection.find(
                eq("name", name)
        ).first();
        if (document != null) {
            return new Development(document);
        }
        return null;
    }

    public List<Development> listDevelopment(String userName){
        List<Development> developments = new ArrayList<>();
        FindIterable<Document> documents;
        if(null==userName)
            documents = defaultCollection.find();
        else
            documents = defaultCollection.find(eq("user", userName));

        for (Document document : documents) {
            developments.add(new Development(document));
        }
        return developments;
    }


    public static void main(String[] args) {
        Development development = JSONObject.toJavaObject(Development.testExample, Development.class);

        DevelopmentImp developmentImp = new DevelopmentImp();
//        developmentImp.addDevelopment(JSONObject.toJavaObject(development.testExample,development.getClass()));

        System.out.println(developmentImp.deleteDevelopment("dev1"));
    }
}
