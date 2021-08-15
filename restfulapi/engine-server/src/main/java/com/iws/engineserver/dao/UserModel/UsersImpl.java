package com.iws.engineserver.dao.UserModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.dao.MongoConnecter;
import com.iws.engineserver.pojo.User;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import static com.mongodb.client.model.Filters.*;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
public class UsersImpl extends MongoConnecter implements Users {
    static private List<User> users = null;
    private MongoCollection<Document> defaultCollection = null;

    static {
        users = new ArrayList<>();
        users.add(new User("iws", "123456", 123, "555@qq.com", "iws", 0));
        users.add(new User("cjq", "123456", 423, "666@qq.com", "cjq", 0));
        users.add(new User("lgy", "123456", 342, "777@qq.com", "lgy", 0));
        users.add(new User("yp", "123456", 124, "888@qq.com", "yp", 0));
    }

    public static void main(String[] args) {
        UsersImpl userDB = new UsersImpl();

        //userDB.addUser(new User("iws","123456",123,"555@qq.com","iws", 0));

        boolean success = userDB.deleteUserById(10);
        System.out.println(success);

    }


    public UsersImpl() {
        setDefaultCollection();
    }

    @Override
    public MongoCollection<Document> getDefaultCollection() {
        return defaultCollection;
    }

    @Override
    public void setDefaultCollection() {
        setDefaultCollection("admin", "Users");
    }

    public void setDefaultCollection(String databaseName, String collectionName) {
        defaultCollection = getCollection(databaseName, collectionName);
    }

    public int getNewID() {
        MongoCollection<Document> collection = getCollection("admin", "ids");
        int id = collection.find().first().getInteger("count");
        int newID = id + 1;
        collection.updateOne(eq("count", id), new Document("$set", new Document("count", newID)));
        return newID;
    }

    public User transUser(Document document) {
        return new User(document);
    }

    public Document transDocument(User user) {
        return user.toDocument();
    }


    @Override
    public List<User> listUsers() {

        FindIterable<Document> findIterable = defaultCollection.find();
        List<User> users = new ArrayList<>();
        for (Document document : findIterable) {
            users.add(transUser(document));
        }
        return users;
    }

    @Override
    public User getUserByNamePassword(String userName, String password) {

        Document document = defaultCollection.find(and(eq("userName", userName), eq("password", password))).first();
        if (document != null)
            return transUser(document);
        return null;
    }

    @Override
    public User getUserByID(int uid) {

        Document document = defaultCollection.find(eq("uid", uid)).first();
        if (document != null)
            return transUser(document);
        return null;
    }

    @Override
    public User getUserByToken(String token) {

        Document document = defaultCollection.find(eq("token", token)).first();
        if (document != null)
            return transUser(document);
        return null;
    }

    @Override
    public boolean addUser(User user) {

        Document document = defaultCollection.find(
                and(
                        eq("userName", user.getUserName()),
                        eq("password", user.getPassword())
                ))
                .first();
        if (document != null) {
            return false;
        }
        user.setUid(getNewID());
        defaultCollection.insertOne(transDocument(user));
        return true;
    }

    @Override
    @Deprecated
    public boolean deleteUserByName(String userName) {

        FindIterable<Document> userMatched = defaultCollection.find(eq("userName", userName));
        {
            int count = 0;
            for (Document document : userMatched) ++count;
            assert count == 1;
        }
        Document document = userMatched.first();
        if (document == null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() != 0;
    }

    @Override
    public boolean deleteUserById(int uid) {

        Document document = defaultCollection.find(eq("uid", uid)).first();
        if (document == null) return false;

        DeleteResult deleteResult = defaultCollection.deleteOne(document);
        return deleteResult.getDeletedCount() != 0;
    }

    @Override
    public boolean updateUser(User newOne) {

        UpdateResult updateResult = defaultCollection.updateOne(
                eq("uid", newOne.getUid()),
                new Document("$set", newOne.toDocument()));
        return updateResult.getModifiedCount() != 0;

    }

}
