package com.iws.engineserver.service.UserModel;

import com.iws.engineserver.pojo.Request;

import java.util.List;

public interface RequestManager {

    public List<Request> listRequests(int page, int pageSize);

    public boolean approveRequest(String email) ;

    public boolean rejectRequest(String email);

    public boolean submitRequest(Request request) ;

    public boolean cancelRequest(String userName);
}
