package com.iws.engineserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iws.engineserver.pojo.ResponseMessage;
import com.iws.engineserver.pojo.Request;
import com.iws.engineserver.pojo.User;
import com.iws.engineserver.service.UserModel.UserManagerImpl;
import com.sun.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserManagerImpl userManager;


    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseMessage signUp(@RequestBody JSONObject signupMessage) {
        Request request = new Request(signupMessage);
        boolean success=userManager.submitRequest(request);

        ResponseMessage responseMessage=new ResponseMessage();
        if (success){
            responseMessage.setCode(800);
            responseMessage.setMsg("Waiting for approving.");
            return responseMessage;
        }
        responseMessage.setCode(900);
        responseMessage.setMsg("Fail to signup.");
        return responseMessage;
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> signIn(@RequestBody JSONObject jsonObject) {

        String userName= jsonObject.getString("user_name");
        String password= jsonObject.getString("password");

        User user=userManager.signIn(userName, password);

        ResponseMessage responseMessage=new ResponseMessage();
        if(user.getToken()!=null){
            responseMessage.setCode(800);
            responseMessage.setMsg("Signin success.");
            responseMessage.setData(JSONObject.parseObject("{\"token\": \""+user.getToken()+"\"}"));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }


        responseMessage.setCode(900);
        responseMessage.setMsg("Signin failed.");
        return new ResponseEntity<>(responseMessage, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/signout", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> signOut(){
        return new ResponseEntity<>(new ResponseMessage(800,"Signout success."), HttpStatus.OK);
    }

    @RequestMapping(value = "/requests",method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> listRequests(@RequestParam(value = "page") int page,
                                                        @RequestParam(value = "page_size") int pageSize){

        JSONObject list = new JSONObject();
        list.put("list",userManager.listRequests(page, pageSize));

        return new ResponseEntity<>(new ResponseMessage(800, "requests list.", list),HttpStatus.OK);
    }


    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> approveRequest(@RequestParam(value = "email") String email){
        boolean success = userManager.approveRequest(email);
        return success?
                new ResponseEntity<>(new ResponseMessage(800, "Request from "+email+" approved."),HttpStatus.OK):
                new ResponseEntity<>(new ResponseMessage(900, "Fail to approved request from "+email+"."),HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/reject", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> rejectRequest(@RequestParam(value = "user_name") String userName){
        boolean success = userManager.rejectRequest(userName);
        return new ResponseEntity<>(new ResponseMessage(800, "Request from "+userName+" rejected."),HttpStatus.OK);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseMessage addUser(@RequestBody JSONObject userInfo) {
        Request request = new Request(userInfo);
        boolean success = userManager.createUser(new User(request));
        ResponseMessage responseMessage=new ResponseMessage();
        if (success){
            responseMessage.setCode(800);
            responseMessage.setMsg("Add user success: "+request.getUserName());
            return responseMessage;
        }
        responseMessage.setCode(900);
        responseMessage.setMsg("Fail to add user: "+request.getUserName());
        return responseMessage;
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public ResponseMessage removeUser(@RequestBody JSONObject userInfo) {
        String username=userInfo.getString("user_name");
        boolean success = userManager.deleteUser(username);
        ResponseMessage responseMessage=new ResponseMessage();
        if (success){
            responseMessage.setCode(800);
            responseMessage.setMsg("Remove user success: "+username);
            return responseMessage;
        }
        responseMessage.setCode(900);
        responseMessage.setMsg("Fail to remove user: "+username);
        return responseMessage;
    }

}
