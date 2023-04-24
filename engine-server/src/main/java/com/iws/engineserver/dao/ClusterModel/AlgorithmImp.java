package com.iws.engineserver.dao.ClusterModel;

import com.iws.engineserver.dao.MongoConnecter;
import com.iws.engineserver.pojo.Algorithm;
import com.iws.engineserver.pojo.AlgorithmPackage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class AlgorithmImp extends MongoConnecter {

    static private List<Algorithm> algorithms = null;
    private MongoCollection<Document> defaultCollection = null;

    public AlgorithmImp() {
        setDefaultCollection();
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Algorithms");
    }

    public void setDefaultCollection(String databaseName, String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }


    public static void main(String[] args) {
        Algorithm algorithm = new Algorithm();
        algorithm.setAlgorithmNo("100");
        algorithm.setImageName("10.16.97.52:8433/library/ubuntu16.04-ssh:v1");
        algorithm.setCodePath("https://github.com/iwannastay/helloworld.git");
        algorithm.setBranch("master");
        algorithm.setCompileScript("compileScript.sh");
        algorithm.setShellParams("/bin/bash");
        algorithm.setCompileStatus(1);
        algorithm.setAlgorithmStatus(2);
        algorithm.setAlgorithmPackage(new AlgorithmPackage(
                "200m",
                "800Mi",
                null,
                0
        ));

        List<String> tesList= new ArrayList<>();
        tesList.add("101");
        tesList.add("102");
        algorithm.setTestList(tesList);



        AlgorithmImp algorithmImp = new AlgorithmImp();
        //System.out.println(algorithmImp.addAlgorithm(algorithm));
        System.out.println(algorithmImp.deleteAlgorithm("algo2108111733093320001"));
        System.out.println(algorithmImp.deleteAlgorithm("5"));
    }

    public boolean addAlgorithm(Algorithm algorithm){
        Document document = defaultCollection.find(
                eq("algorithmNo", algorithm.getAlgorithmNo())
        ).first();
        if (document != null) {
            return false;
        }

        defaultCollection.insertOne(algorithm.toDocument());
        return true;
    }

    public boolean deleteAlgorithm(String algorithmNo){
        Document document = defaultCollection.find(eq("algorithmNo",algorithmNo)).first();
        if(document==null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() !=0;
    }

    public boolean updateAlgorithm(Algorithm algorithm){

        UpdateResult updateResult = defaultCollection.updateOne(
                eq("algorithmNo",algorithm.getAlgorithmNo()),
                new Document("$set",algorithm.toDocument()));
        return updateResult.getModifiedCount() != 0;
    }

    public Algorithm getAlgorithmByNo(String algorithmNo){
        Document document = defaultCollection.find(
                eq("algorithmNo", algorithmNo)
        ).first();
        if (document != null) {
            return new Algorithm(document);
        }
        return null;
    }

    public List<Algorithm> listAlgorithm(){
        List<Algorithm> algorithms = new ArrayList<>();
        FindIterable<Document> documents = defaultCollection.find();
        for (Document document : documents) {
            algorithms.add(new Algorithm(document));
        }
        return algorithms;

    }
}
