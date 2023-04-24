package com.iws.engineserver.service.UserModel;

import com.iws.engineserver.dao.UserModel.RequestsImpl;
import com.iws.engineserver.dao.UserModel.UsersImpl;
import com.iws.engineserver.pojo.Request;
import com.iws.engineserver.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class UserManagerImpl implements UserManager, RequestManager {
    @Autowired
    org.slf4j.Logger logger;

    @Autowired
    UsersImpl users;

    @Autowired
    RequestsImpl requests;

    @Override
    public boolean signUp(String userName, String password, String email, int role) {
        Request request = new Request(userName, password, email, role);
        return submitRequest(request);
    }

    @Override
    public User signIn(String userName, String password) {
        User user = users.getUserByNamePassword(userName, password);
        if (null != user) {
            user.setToken(UUID.randomUUID().toString());
            users.updateUser(user);
            return user;
        }
        return new User();
    }

    @Override
    public boolean signOut(String userName, String token) {
        User user = users.getUserByToken(token);
        if (null != user) {
            user.setToken("null");
            users.updateUser(user);
            return true;
        }
        return false;
    }

    @Override
    public User isAuthenticated(String token) {
        //TODO: if necessary

        return users.getUserByToken(token);
    }

    @Override
    public List<Request> listRequests(int page, int pageSize) {
        //TODO: page&pagesize
        return requests.listRequests();
    }

    @Override
    public boolean approveRequest(String email) {
        Request requestByEmail = requests.getRequestByEmail(email);
        if (null != requestByEmail) {
            if (requestByEmail.getStatus() == 0) {
                requestByEmail.setStatus(1);
                requests.updateRequest(requestByEmail);
                return createUser(new User(requestByEmail));
            }
        }
        return false;
    }

    @Override
    public boolean rejectRequest(String email) {
        Request requestByEmail = requests.getRequestByEmail(email);
        if (null != requestByEmail) {
            requestByEmail.setStatus(2);
            requests.updateRequest(requestByEmail);
            return true;
        }
        return false;
    }

    @Override
    public boolean submitRequest(Request request) {
        if (requests.getRequestByEmail(request.getEmail()) == null) {
            return requests.addRequest(request);
        }
        return requests.updateRequest(request);
    }

    @Override
    public boolean cancelRequest(String userName) {
        //TODO: if necessary
        return false;
    }


    @Override
    public boolean createUser(User user) {
        return users.addUser(user);
    }

    @Override
    public boolean deleteUser(String userName) {
        return users.deleteUserByName(userName);
    }

    @Override
    public boolean deleteUser(int uid) {
        return users.deleteUserById(uid);
    }

    @Override
    public boolean updateUser(int uid, User user) {
        //TODO
        return users.updateUser(user);
    }


    @Override
    public boolean updateRole(int uid, int role) {
        User userByID = users.getUserByID(uid);
        if (null != userByID) {
            userByID.setRole(role);
            users.updateUser(userByID);
        }
        return false;
    }

    @Override
    public List<User> listUsers(int page, int pageSize) {
        //TODO: page&pagesize
        return users.listUsers();
    }
}
