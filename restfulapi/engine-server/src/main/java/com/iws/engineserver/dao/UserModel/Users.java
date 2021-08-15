package com.iws.engineserver.dao.UserModel;

import com.iws.engineserver.pojo.User;

import java.util.List;

public interface Users {
    List<User> listUsers();
    User getUserByNamePassword(String userName,String password);
    User getUserByID(int uid);
    User getUserByToken(String token);
    boolean addUser(User user);
    boolean deleteUserByName(String userName);
    boolean deleteUserById(int uid);
    boolean updateUser(User user);
}
