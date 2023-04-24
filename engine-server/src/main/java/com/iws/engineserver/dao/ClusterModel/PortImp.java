package com.iws.engineserver.dao.ClusterModel;

import com.iws.engineserver.dao.MongoConnecter;
import com.iws.engineserver.pojo.Cluster;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class PortImp extends MongoConnecter {
    static int base = 31500;
    static int size = 500;
    private MongoCollection<Document> defaultCollection = null;
    private static Logger logger = LoggerFactory.getLogger(PortImp.class);

    public PortImp() {
        setDefaultCollection();
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Ports");
    }

    public void setDefaultCollection(String databaseName, String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }

    public Integer getPort(Integer seed) {
        seed = seed % size +base;

        List<Integer> ports = (ArrayList<Integer>) defaultCollection.find(eq("name", "ports")).first().get("busy");

        while (true) {
            if (!ports.contains(seed)) {
                ports.add(seed);
                break;
            }
            seed = (seed+1) % size +base;
        }
        defaultCollection.updateOne(eq("name", "ports"), new Document("$set", new Document("busy", ports)));
        logger.info("allocate port: "+seed);
        return seed;
    }

    public void releasePort(Integer port) {
        List<Integer> ports = (ArrayList<Integer>) defaultCollection.find(eq("name", "ports")).first().get("busy");
        ports.remove(port);
        defaultCollection.updateOne(eq("name", "ports"), new Document("$set", new Document("busy", ports)));
        logger.info("release port: "+port);
    }

    public static void main(String[] args) {
        PortImp portImp = new PortImp();
        for(int i=31500;i!=31501;++i)
            portImp.releasePort(i);

    }
}
