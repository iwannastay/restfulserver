package com.iws.engineserver.dao.UserModel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.MongoConnecter;
import com.iws.engineserver.pojo.Request;
import com.iws.engineserver.pojo.User;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.cglib.core.MethodWrapper;
import org.springframework.stereotype.Repository;
import static com.mongodb.client.model.Filters.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Filter;

@Repository
public class RequestsImpl extends MongoConnecter implements Requests {


    static private List<Request> requests = null;



    private MongoCollection<Document> defaultCollection=null;

    static {
        requests = new ArrayList<>();
        requests.add(new Request("Xin.Y", "123123", "111@sustech.edu.cn", 0));
        requests.add(new Request("Tang.K", "123123", "222@sustech.edu.cn", 1));
        requests.add(new Request("Peng.Y", "123123", "333@sustech.edu.cn", 2));

    }

    public RequestsImpl() {
        setDefaultCollection();
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Requests");
    }

    public void setDefaultCollection(String databaseName,String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }

    public Request transRequest(Document document){
        return new Request(document);
    }

    public Document transDocument(Request request){
        return request.toDocument();
    }

    public static void main(String[] args) {
        RequestsImpl requestDB = new RequestsImpl();
        List<Request> requestList = requestDB.listRequests();
        boolean b = requestDB.deleteRequestByName("Xin.Y");
        System.out.println(b);

    }


    @Override
    public List<Request> listRequests() {
        FindIterable<Document> findIterable = defaultCollection.find();
        List<Request> requests = new ArrayList<>();
        for (Document document : findIterable) {
            requests.add(transRequest(document));
        }
        return requests;

    }

    @Override
    public Request getRequestByName(String userName) {

        Document document = defaultCollection.find(eq("userName", userName)).first();
        if (document != null) {
            return new Request(document);
        }
        return null;
    }

    @Override
    public Request getRequestByEmail(String email) {

        Document document = defaultCollection.find(eq("email", email)).first();
        if (document != null) {
            return new Request(document);
        }
        return null;
    }

    @Override
    public boolean updateRequest(Request request) {

        UpdateResult updateResult = defaultCollection.updateOne(
                eq("email",request.getEmail()),
                new Document("$set",request.toDocument()));
        return updateResult.getModifiedCount() != 0;
    }

    @Override
    public boolean addRequest(Request request) {

        defaultCollection.insertOne(request.toDocument());
        return true;
    }

    @Override
    public boolean deleteRequestByName(String userName) {

        Document document = defaultCollection.find(eq("userName",userName)).first();
        if(document==null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() !=0;
    }

    @Override
    public boolean deleteRequestByEmail(String email) {

        Document document = defaultCollection.find(eq("email",email)).first();
        if(document==null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() !=0;
    }
}
