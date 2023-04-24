package com.iws.engineserver.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;

public abstract class MongoConnecter {
    static private MongoClient mongoClient=null;

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            try {
                ServerAddress serverAddress = new ServerAddress("10.16.97.52", 30224);
                ArrayList<ServerAddress> serverAddresses = new ArrayList<>();
                serverAddresses.add(serverAddress);


                MongoCredential scramSha1Credential = MongoCredential.createScramSha1Credential("root", "admin", "YFk7AgAjVa".toCharArray());
                ArrayList<MongoCredential> mongoCredentials = new ArrayList<>();
                mongoCredentials.add(scramSha1Credential);

                mongoClient = new MongoClient(serverAddresses, mongoCredentials);

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
        return mongoClient;

    }

    public MongoConnecter() {
        getMongoClient();
    }

    public MongoCollection<Document> getCollection(String databaseName, String collectionName){
        return getMongoClient().getDatabase(databaseName).getCollection(collectionName);
    }

    public abstract void setDefaultCollection();
    public abstract MongoCollection<Document> getDefaultCollection();
}
