package com.iws.engineserver.dao.UserModel;

import com.iws.engineserver.pojo.Request;
import com.iws.engineserver.pojo.User;

import java.util.List;

public interface Requests {
    List<Request> listRequests();
    Request getRequestByName(String userName);
    Request getRequestByEmail(String email);;
    boolean updateRequest(Request request);
    boolean addRequest(Request request);
    boolean deleteRequestByName(String userName);
    boolean deleteRequestByEmail(String email);
}
