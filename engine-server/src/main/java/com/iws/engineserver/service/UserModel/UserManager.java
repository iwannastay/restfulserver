package com.iws.engineserver.service.UserModel;

import com.iws.engineserver.pojo.Request;
import com.iws.engineserver.pojo.User;

import java.util.List;

public interface UserManager {
    boolean signUp(String userName, String password, String email,int role);

    User signIn(String userName, String password);

    boolean signOut(String userName, String token);

    User isAuthenticated(String token);


    boolean createUser(User user);

    boolean deleteUser(String userName);
    boolean deleteUser(int uid);

    boolean updateUser(int uid, User user);

    boolean updateRole(int uid, int role);

    List<User> listUsers(int page, int pageSize);


}
